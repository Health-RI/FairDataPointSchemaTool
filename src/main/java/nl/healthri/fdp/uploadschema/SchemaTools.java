package nl.healthri.fdp.uploadschema;

import nl.healthri.fdp.uploadschema.tasks.ResourceUpdateInsertTask;
import nl.healthri.fdp.uploadschema.tasks.ShapeUpdateInsertTask;
import nl.healthri.fdp.uploadschema.utils.Properties;
import nl.healthri.fdp.uploadschema.utils.RdfUtils;
import org.eclipse.rdf4j.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static java.util.function.Predicate.not;

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

    public static void main(String... args) {
        var cmd = new CommandLine(new SchemaTools());
        System.exit(cmd.execute(args));
    }

    @Override
    public void run() {
        try {
            final Properties p = Properties.load(propertyFile);
            if (command == CommandEnum.TEMPLATE) {
                //1 read all(!) excel files from folder, write shapes in Pieces directory
                logger.info("reading templates from {} ", p.templateDir);
                for (var e : XlsToRdfUtils.getTemplateFiles(p.templateDir).entrySet()) {
                    logger.info("  converting {} ", e.getValue());
                    Path path = p.getPiecesDir().resolve(e.getKey() + ".ttl");
                    String shacl = XlsToRdfUtils.createShacl(e.getValue());
                    Files.write(path, shacl.getBytes());
                }
                //2 merge piece to FairDataPoint dir.
                logger.info("Writing files: {}", p.getFiles().keySet());
                for (var e : p.getFiles(p.getPiecesDir()).entrySet()) {
                    Path path = p.getFairDataPointDir().resolve(RdfUtils.schemaToFile(e.getKey()));
                    Model m = RdfUtils.readFiles(e.getValue());
                    RdfUtils.safeModel(path, m);
                }
                //3 merge all pieces into validation dir.

                Path path = p.getValidationDir().resolve("HRI-Datamodel-shapes.ttl");
                logger.info("Write validation file {} combining {} files", path, p.getAllFiles().size());
                Model m = RdfUtils.readFiles(new ArrayList<>(p.getAllFiles()));
                RdfUtils.safeModel(path, m);
            } else {
                logger.info("Connecting to FDP at {} as {} ", hostname, username);

                final FDP fdp = FDP.connectToFdp(hostname, username, password);

                if (command == CommandEnum.SCHEMA || command == CommandEnum.BOTH) {
                    //Shapes we want to update/insert
                    var shapeTasks = ShapeUpdateInsertTask.createTasks(p, fdp);

//          insert new schemas and keep the UUID, this is needed for the "release step"
                    shapeTasks.stream().filter(ShapeUpdateInsertTask::isInsert).forEach(fdp::insertSchema);

//          update existing shape, will get status draft.
                    shapeTasks.stream().filter(not(ShapeUpdateInsertTask::isInsert)).forEach(fdp::updateSchema);

                    //the draft-schema are released,
                    shapeTasks.forEach(fdp::releaseSchema);
                }

                if (command == CommandEnum.RESOURCE || command == CommandEnum.BOTH) {
                    //add resource-descriptions
                    var resourceTasks = ResourceUpdateInsertTask.createTask(p, fdp);
                    resourceTasks.stream().filter(ResourceUpdateInsertTask::isInsert).forEach(fdp::insertResource);

                    if (resourceTasks.stream().noneMatch(not(ResourceUpdateInsertTask::isInsert))) {
                        logger.warn("Updating resources is not supported yet, but will try to add children if needed)");
                    }

                    //add the previous resources as child to parent.
                    var resourceTasksParents = ResourceUpdateInsertTask.createParentTask(p, fdp);
                    resourceTasksParents.stream().filter(ResourceUpdateInsertTask::hasChild).forEach(fdp::updateResource);
                }
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