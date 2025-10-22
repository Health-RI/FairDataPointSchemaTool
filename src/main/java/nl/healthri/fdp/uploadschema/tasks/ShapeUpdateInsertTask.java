package nl.healthri.fdp.uploadschema.tasks;

import nl.healthri.fdp.uploadschema.FDP;
import nl.healthri.fdp.uploadschema.Version;
import nl.healthri.fdp.uploadschema.requestresponses.SchemaDataResponse;
import nl.healthri.fdp.uploadschema.utils.Properties;
import nl.healthri.fdp.uploadschema.utils.RdfUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ShapeUpdateInsertTask {
    private static final Logger logger = LoggerFactory.getLogger(ShapeUpdateInsertTask.class);

    public final String shape;
    public Version version;
    public String uuid;
    public Set<String> parents; //name of parents for this schema.
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
            var requestedVersion = p.getVersion();
            var ttlFiles = Optional.ofNullable(files.get(r)).orElseThrow(() -> new NoSuchElementException(r + " not present in schema section of yaml-file"));

            logger.debug("loading model {} using turtle files: {} ", r, ttlFiles.stream().map(URI::toString).collect(Collectors.joining(", ")));

            ShapeUpdateInsertTask.model = RdfUtils.modelAsTurtleString(RdfUtils.readFiles(ttlFiles));
            if (shapesOnFdp.isPresent(r)) {
                // todo: add current compare ttl schemas here
                ShapeUpdateInsertTask.version = shapesOnFdp.getVersion(r).get().next(requestedVersion); //next patch version
                ShapeUpdateInsertTask.uuid = shapesOnFdp.getUUID(r).get();
                ShapeUpdateInsertTask.exists = true;
            } else {
                ShapeUpdateInsertTask.version = requestedVersion;
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

    public boolean isSameSchema(SchemaDataResponse schemaData) throws IOException {
        StringReader stringReaderNew;
        if(this.model == null){
            stringReaderNew = new StringReader("");
        } else {
            stringReaderNew = new StringReader(this.model);
        }

        StringReader stringReaderOld;
        if(schemaData.latest() == null){
            stringReaderOld = new StringReader("");
        } else {
            stringReaderOld = new StringReader(schemaData.latest().definition());
        }

        // Parse TTL strings into RDF4J Models
        Model taskModel = Rio.parse(stringReaderNew, "", RDFFormat.TURTLE);
        Model schemaResponseModel = Rio.parse(stringReaderOld, "", RDFFormat.TURTLE);

        // Check if models have equivalent RDF Graphs to know if schema should be updated
        return Models.isomorphic(taskModel, schemaResponseModel);
    }
}
