package nl.healthri.fdp.uploadschema.tasks;

import nl.healthri.fdp.uploadschema.dto.response.Schema.SchemaDataResponse;
import nl.healthri.fdp.uploadschema.integration.FdpClient;
import nl.healthri.fdp.uploadschema.Version;
import nl.healthri.fdp.uploadschema.integration.FdpService;
import nl.healthri.fdp.uploadschema.utils.FileHandler;
import nl.healthri.fdp.uploadschema.utils.Properties;
import nl.healthri.fdp.uploadschema.utils.RdfUtils;
import nl.healthri.fdp.uploadschema.utils.ShapesMap;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.Models;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public class ShapeUpdateInsertTask {
    private static final Logger logger = LoggerFactory.getLogger(ShapeUpdateInsertTask.class);

    public final String shape;
    public Version version;
    public String uuid;
    public Set<String> parents; //name of parents for this schema.
    public String model;
    public ShapeStatus status;

    public enum ShapeStatus {
        INSERT, UPDATE, SAME
    }

    public ShapeUpdateInsertTask(String shape) {
        this.shape = shape;
    }

    public static List<ShapeUpdateInsertTask> createTasks(FdpService fdpService, Properties p, FileHandler fileHandler){
        final List<String> Shapes = p.schemasToPublish;
        final var files = p.getFiles();
        var shapesOnFdp = fdpService.getAllSchemas();

        logger.info("found following shapes on fdp: {}", shapesOnFdp.keySet());

        //list of the task we have to do for insert/updating shacls
        return Shapes.stream().map(r -> {
            var ShapeUpdateInsertTask = new ShapeUpdateInsertTask(r);
            var requestedVersion = p.getVersion();
            var ttlFiles = Optional.ofNullable(files.get(r)).orElseThrow(() -> new NoSuchElementException(r + " not present in schema section of yaml-file"));

            logger.debug("loading model {} using turtle files: {} ", r, ttlFiles.stream().map(URI::toString).collect(Collectors.joining(", ")));

            Model newModel = fileHandler.readFiles(ttlFiles);
            ShapeUpdateInsertTask.model = RdfUtils.modelAsTurtleString(newModel);
            if (shapesOnFdp.isPresent(r)) {
                Model onFdp = shapesOnFdp.getDefinition(r).map(RdfUtils::fromTurtleString).orElse(new LinkedHashModel());
                ShapeUpdateInsertTask.version = shapesOnFdp.getVersion(r).map(v -> v.next(requestedVersion)).orElseThrow(); //next patch version
                ShapeUpdateInsertTask.uuid = shapesOnFdp.getUUID(r).orElseThrow();
                ShapeUpdateInsertTask.status = Models.isomorphic(onFdp, newModel) ? ShapeStatus.SAME : ShapeStatus.UPDATE;
            } else {
                ShapeUpdateInsertTask.version = requestedVersion;
                ShapeUpdateInsertTask.uuid = "";
                ShapeUpdateInsertTask.status = ShapeStatus.INSERT;
            }
            ShapeUpdateInsertTask.parents = p.getParents(r);
            return ShapeUpdateInsertTask;
        }).toList();
    }

    public Set<String> getParentUID(ShapesMap shapesMap) {
        if (this.parents.isEmpty()) {
            return Collections.emptySet();
        }

        return this.parents.stream()
                .map(shapesMap::getUUID).flatMap(Optional::stream)
                .collect(Collectors.toSet());
    }

    public String description() {
        return shape;
    }

    public String url() {
        return shape.toLowerCase().replaceAll(" ", "");
    }

    public ShapeStatus status() {
        return status;
    }
}

