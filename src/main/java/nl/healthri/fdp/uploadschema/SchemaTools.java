package nl.healthri.fdp.uploadschema;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.healthri.fdp.uploadschema.integration.FdpClient;
import nl.healthri.fdp.uploadschema.integration.FdpService;
import nl.healthri.fdp.uploadschema.tasks.ResourceTaskService;
import nl.healthri.fdp.uploadschema.tasks.SchemaToolService;
import nl.healthri.fdp.uploadschema.tasks.ShapeTaskService;
import nl.healthri.fdp.uploadschema.utils.FileHandler;
import nl.healthri.fdp.uploadschema.utils.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.time.Duration;

@CommandLine.Command(name = "SchemaTools utility that create FDP ready Shacls and upload them the the FDP.",
        mixinStandardHelpOptions = true, version = "SchemaTool v1.0")
public class SchemaTools implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SchemaTools.class);

    @CommandLine.Option(names = {"-i", "--input"}, defaultValue = "./Properties.yaml", description = "location of the Property.yaml file (default: ${DEFAULT-VALUE})")
    File propertyFile;

    @CommandLine.Option(names = {"-u", "--user"}, defaultValue = "albert.einstein@example.com", description = "FDP admin user (default: ${DEFAULT-VALUE})")
    String username;

    @CommandLine.Option(names = {"-h", "--host"}, defaultValue = "http://localhost:80", converter = UriConverter.class, description = "fdp url (default: ${DEFAULT-VALUE})"
    )
    URI hostname;

    @CommandLine.Option(names = {"-p", "--password"}, defaultValue = "password", description = "FDP admin password (default: ${DEFAULT-VALUE})")
    String password;

    @CommandLine.Option(names = {"-c", "--command"}, defaultValue = "both", description = "Valid values: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE})", converter = CommandEnumConverter.class)
    CommandEnum command;

    @CommandLine.Option(names = {"-f", "--force"}, defaultValue = "false", description = "Force upload even if schema has not changed")
    boolean force;

    public static void main(String... args) {
        var cmd = new CommandLine(new SchemaTools());
        System.exit(cmd.execute(args));
    }

    @Override
    public void run() {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            final ObjectMapper objectMapper = new ObjectMapper();
            final FdpClient fdpClient = new FdpClient(client, this.hostname, objectMapper);
            final FdpService fdpService = new FdpService(fdpClient);
            final Properties properties = Properties.load(propertyFile);
            final FileHandler fileHandler = new FileHandler();
            final ResourceTaskService resourceTaskService = new ResourceTaskService(fdpService, properties);
            final ShapeTaskService shapeTaskService = new ShapeTaskService(fdpService, fileHandler, properties);
            final SchemaToolService schemaToolService = new SchemaToolService(fdpService, resourceTaskService, shapeTaskService, properties, fileHandler);

            fdpService.authenticate(this.username, this.password);

            switch (command) {
                case TEMPLATE -> {
                    schemaToolService.convertTemplatesToShaclShapes();
                    schemaToolService.mergeShapesToFdpSchemas();
                    schemaToolService.mergeShapesForValidation();
                }
                case BOTH -> {
                    schemaToolService.createOrUpdateSchemas(force);
                    schemaToolService.addResourceDescriptions();
                }
                case SCHEMA -> schemaToolService.createOrUpdateSchemas(force);
                case RESOURCE -> schemaToolService.addResourceDescriptions();
            }
        } catch (IOException io) {
            throw new RuntimeException(io);
        }
    }

    public enum CommandEnum {
        SCHEMA, RESOURCE, BOTH, TEMPLATE
    }

    //this class is needed to make the -c option case-insensitive.
    public static class CommandEnumConverter implements CommandLine.ITypeConverter<CommandEnum> {
        @Override
        public CommandEnum convert(String value) {
            return CommandEnum.valueOf(value.toUpperCase());
        }
    }

    public static class UriConverter implements CommandLine.ITypeConverter<URI> {
        @Override
        public URI convert(String value) {
            try {
                var uri = new URI(value);
                if (!uri.isAbsolute()) {
                    throw new CommandLine.TypeConversionException("Invalid URI format: " + value);
                }
                return uri;
            } catch (URISyntaxException e) {
                throw new CommandLine.TypeConversionException("Invalid URI format: " + value);
            }
        }
    }

}