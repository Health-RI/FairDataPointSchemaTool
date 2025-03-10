package transferdata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@CommandLine.Command(name = "SchemaTools merge multiple shapes files into one.",
        mixinStandardHelpOptions = true, version = "SchemaTool v1.0")
public class SchemaTools implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SchemaTools.class);

    @CommandLine.Option(names = {"-i", "--input"}, required = true, description = "Property file, in yaml format")
    File propertyFile;

    @CommandLine.Option(names = {"-p", "--password"}, required = true, description = "FDP admin password")
    String password;

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

            //fetch exsisting schemas from FDP
            var schemaOnFdp = fdp.fetchSchemaFromFDP();
            logger.info("found following schemas on fdp: {}", schemaOnFdp.keySet());

            //list of ttl used by the resources.
            final var files = p.getFiles();

            //Resources we want to update/insert
            final List<String> resources = p.resourcesToPublish;

            List<Task> tasks = new ArrayList<>();
            for (var r : resources) {
                var task = new Task(r);

                var ttlFiles = Optional.ofNullable(files.get(r)).orElseThrow(() -> new NoSuchElementException(r + " not present in schema section of yaml-file"));

                task.model = RdfUtils.modelAsTurtleString(RdfUtils.readFiles(ttlFiles));
                if (schemaOnFdp.containsKey(r)) {
                    var info = schemaOnFdp.get(r);
                    task.version = info.version.next(); //next patch version
                    task.uuid = info.uuid;
                    task.exists = true;
                } else {
                    task.version = new Version(); //1.0.0
                    task.uuid = "";
                    task.exists = false;
                }
                tasks.add(task);
            }
            //insert new resources and keep the UUID.
            tasks.stream().filter(Task::isInsert).forEach(t ->
                    t.updateUUID(fdp.insertSchema(t)));

            //update existings resource, will get status draft.
//            tasks.stream().forEach(fdp::updateSchema);

            tasks.forEach(fdp::releaseSchema);

            //release resource, we use new version numner for this.


        } catch (IOException io) {
            throw new RuntimeException(io);
        }
    }

    static class Task {
        String resource;
        Version version;
        String uuid;
        String parent;
        String model;
        boolean exists = false;

        public Task(String resource) {
            this.resource = resource;
        }

        public String description() {
            return resource;
        }

        public String url() {
            return resource.toLowerCase();
        }

        public boolean isInsert() {
            return !exists;
        }

        public void updateUUID(String uuid) {
            this.uuid = uuid;
        }
    }
}