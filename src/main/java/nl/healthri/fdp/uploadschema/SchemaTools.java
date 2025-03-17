package nl.healthri.fdp.uploadschema;

import nl.healthri.fdp.uploadschema.tasks.ResourceUpdateInsertTask;
import nl.healthri.fdp.uploadschema.tasks.ShapeUpdateInsertTask;
import nl.healthri.fdp.uploadschema.utils.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;

import static java.util.function.Predicate.not;

@CommandLine.Command(name = "SchemaTools utility that helps woth Shacls and Fair Datapoints",
        mixinStandardHelpOptions = true, version = "SchemaTool v1.0")
public class SchemaTools implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SchemaTools.class);

    @CommandLine.Option(names = {"-i", "--input"}, required = true, description = "Property file, in yaml format")
    File propertyFile;

    @CommandLine.Option(names = {"-p", "--password"}, required = true, description = "FDP admin password")
    String password;

    @CommandLine.Option(names = {"-c", "--command"}, required = true, description = "Valid values: ${COMPLETION-CANDIDATES}")
    CommandEnum command;

    public static void main(String... args) {
        var cmd = new CommandLine(new SchemaTools());
        if (args.length == 0) {
            cmd.usage(System.out);
        } else {
            System.exit(cmd.execute(args));
        }
    }

    @Override
    public void run() {
        try {
            final Properties p = Properties.load(propertyFile);

            final FDP fdp = FDP.connectToFdp(p.fdpUrl, p.fdpUsername, password);

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
                    logger.warn("Updating resources is not supported yet, but will try to add childeren if needed)");
                }

                //add the previous resources as child to parent.
                var resourceTasksParents = ResourceUpdateInsertTask.createParentTask(p, fdp);
                resourceTasksParents.stream().filter(ResourceUpdateInsertTask::hasChild).forEach(fdp::updateResource);

//          insert new resource and keep the UUID.
                resourceTasks.stream().filter(ResourceUpdateInsertTask::isInsert)
                        .forEach(fdp::insertResource);
            }

        } catch (IOException io) {
            throw new RuntimeException(io);
        }
    }

    public enum CommandEnum {
        SCHEMA, RESOURCE, BOTH//add FILES option to save combine files.
    }

}