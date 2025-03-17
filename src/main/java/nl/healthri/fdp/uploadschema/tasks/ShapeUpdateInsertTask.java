package nl.healthri.fdp.uploadschema.tasks;

import nl.healthri.fdp.uploadschema.FDP;
import nl.healthri.fdp.uploadschema.Version;
import nl.healthri.fdp.uploadschema.utils.Properties;
import nl.healthri.fdp.uploadschema.utils.RdfUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

public class ShapeUpdateInsertTask {
    private static final Logger logger = LoggerFactory.getLogger(ShapeUpdateInsertTask.class);

    public final String shape;
    public Version version;
    public String uuid;
    //name of parents for this schema.
    public Set<String> parents;
    public String model;
    public boolean exists = false;

    public ShapeUpdateInsertTask(String shape) {
        this.shape = shape;
    }

    public static List<ShapeUpdateInsertTask> createTasks(Properties p, FDP fdp) {
        final List<String> Shapes = p.schemasToPublish;
        final var files = p.getFiles();
        var shapesOnFdp = fdp.fetchSchemaFromFDP();
        logger.info("found following shapes on fdp: {}", shapesOnFdp.keySet());


        //list of the task we have to do for insert/updating shacls
        return Shapes.stream().map(r -> {
            var ShapeUpdateInsertTask = new ShapeUpdateInsertTask(r);
            int requestedMajorVersion = p.schemaVersion;
            var ttlFiles = Optional.ofNullable(files.get(r)).orElseThrow(() -> new NoSuchElementException(r + " not present in schema section of yaml-file"));

            ShapeUpdateInsertTask.model = RdfUtils.modelAsTurtleString(RdfUtils.readFiles(ttlFiles));
            if (shapesOnFdp.isPresent(r)) {
                ShapeUpdateInsertTask.version = shapesOnFdp.getVersion(r).get().next(requestedMajorVersion); //next patch version
                ShapeUpdateInsertTask.uuid = shapesOnFdp.getUUID(r).get();
                ShapeUpdateInsertTask.exists = true;
            } else {
                ShapeUpdateInsertTask.version = new Version(requestedMajorVersion, 0, 0);
                ShapeUpdateInsertTask.uuid = "";
                ShapeUpdateInsertTask.exists = false;
            }
            ShapeUpdateInsertTask.parents = p.getParents(r);
            return ShapeUpdateInsertTask;
        }).toList();
    }

    public String description() {
        return shape;
    }

    public String url() {
        return shape.toLowerCase().replaceAll(" ", "");
    }

    public boolean isInsert() {
        return !exists;
    }
}
