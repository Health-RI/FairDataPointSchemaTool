package nl.healthri.fdp.uploadschema;

import nl.healthri.fdp.uploadschema.requestresponses.SchemaDataResponse;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            final Properties properties = Properties.load(propertyFile);
            final FDP fdp = FDP.connectToFdp(hostname, username, password);

            switch (command){
                case TEMPLATE -> {
                        convertTemplatesToShaclShapes(properties);
                        mergeShapesToFdpSchemas(properties);
                        mergeShapesForValidation(properties);
                }
                case BOTH -> {
                        createOrUpdateSchemas(fdp, properties);
                        addResourceDescriptions(fdp, properties);
                }
                case SCHEMA ->
                        createOrUpdateSchemas(fdp, properties);
                case RESOURCE ->
                        addResourceDescriptions(fdp, properties);
            }
        } catch (IOException io) {
            throw new RuntimeException(io);
        }
    }

    public void convertTemplatesToShaclShapes(Properties properties) throws IOException {
        logger.info("reading templates from {} ", properties.templateDir);

        for (var e : XlsToRdfUtils.getTemplateFiles(properties.templateDir).entrySet()) {
            logger.info("  converting {} ", e.getValue());
            Path path = properties.getPiecesDir().resolve(e.getKey() + ".ttl");
            String shacl = XlsToRdfUtils.createShacl(e.getValue());
            Files.write(path, shacl.getBytes());
        }
    }

    public void mergeShapesToFdpSchemas(Properties properties) throws IOException {
        logger.info("Writing files: {}", properties.getFiles().keySet());

        for (var e : properties.getFiles(properties.getPiecesDir()).entrySet()) {
            Path path = properties.getFairDataPointDir().resolve(RdfUtils.schemaToFile(e.getKey()));
            Model m = RdfUtils.readFiles(e.getValue());
            RdfUtils.safeModel(path, m);
        }
    }

    public void mergeShapesForValidation(Properties properties) throws IOException {
        logger.info("Merging files: {}", properties.getValidationDir());

        Path path = properties.getValidationDir().resolve("HRI-Datamodel-shapes.ttl");
        logger.info("Write validation file {} combining {} files", path, properties.getAllFiles().size());
        Model m = RdfUtils.readFiles(new ArrayList<>(properties.getAllFiles()));
        RdfUtils.safeModel(path, m);
    }

    public void createOrUpdateSchemas(FDP fdp, Properties properties) throws IOException {
        logger.info("Creating/updating schemas from tasks to FDP");

        var shapeTasks = ShapeUpdateInsertTask.createTasks(properties, fdp);
        List<ShapeUpdateInsertTask> schemasToUpdate = filterSchemasToUpdate(fdp, shapeTasks);

        schemasToUpdate.forEach(task -> {
            if (task.isInsert()) {
                fdp.insertSchema(task);
            } else {
                fdp.updateSchema(task);
            }

            fdp.releaseSchema(task);
        });
    }

    public void addResourceDescriptions(FDP fdp, Properties properties){
        logger.info("Adding resource descriptions from resource tasks to FDP");

        var resourceTasks = ResourceUpdateInsertTask.createTask(properties, fdp);
        resourceTasks.stream().filter(ResourceUpdateInsertTask::isInsert).forEach(fdp::insertResource);

        if (resourceTasks.stream().noneMatch(not(ResourceUpdateInsertTask::isInsert))) {
            logger.warn("Updating resources is not supported yet, but will try to add children if needed)");
        }

        //add the previous resources as child to parent.
        var resourceTasksParents = ResourceUpdateInsertTask.createParentTask(properties, fdp);
        resourceTasksParents.stream().filter(ResourceUpdateInsertTask::hasChild).forEach(fdp::updateResource);
    }

    public List<ShapeUpdateInsertTask> filterSchemasToUpdate(FDP fdp, List<ShapeUpdateInsertTask> shapeUpdateInsertTaskList) throws IOException {
        logger.info("Getting all schemas with schema changes");

        // Maps all schemas found with schema name as key for easy lookup.
        Map<String, SchemaDataResponse> existingSchemaMap = fdp.GetAllSchemas().stream()
                .collect(Collectors.toMap(
                        SchemaDataResponse::name,
                        schema -> schema
                ));

        List<ShapeUpdateInsertTask> schemasToUpdate = new ArrayList<>();
        for (ShapeUpdateInsertTask task : shapeUpdateInsertTaskList) {
            SchemaDataResponse existingSchema = existingSchemaMap.get(task.shape);
            if (existingSchema == null) {
                continue;
            }

            if (!task.isSameSchema(existingSchema)){
                schemasToUpdate.add(task);
            }
        }

        return schemasToUpdate;
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