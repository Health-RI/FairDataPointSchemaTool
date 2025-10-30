package nl.healthri.fdp.uploadschema.tasks;

import nl.healthri.fdp.uploadschema.Version;
import nl.healthri.fdp.uploadschema.dto.response.Resource.ResourceResponse;
import nl.healthri.fdp.uploadschema.dto.response.Schema.SchemaDataResponse;
import nl.healthri.fdp.uploadschema.integration.FdpService;
import nl.healthri.fdp.uploadschema.utils.*;
import nl.healthri.fdp.uploadschema.utils.Properties;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.Models;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URL;
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
        final List<String> schemaList = p.schemasToPublish;
        final Map<String, List<URI>> files = p.getFiles();
        final List<SchemaDataResponse> schemaDataResponseList = fdpService.getAllSchemas();

        Map<String, SchemaInfo> schemaMap = new HashMap<>();
        for(SchemaDataResponse schemaDataResponse : schemaDataResponseList) {
            Version version = new Version(schemaDataResponse.latest().version());
            SchemaInfo schemaInfo = new SchemaInfo(version, schemaDataResponse.uuid());
            schemaMap.put(schemaDataResponse.name(), schemaInfo);
        }



        logger.info("found following shapes on fdp: {}", fdpSchemaMap.keySet());

        //list of the task we have to do for insert/updating shacls
        return schemaList.stream().map(schema -> {
            ShapeUpdateInsertTask shapeUpdateInsertTask = new ShapeUpdateInsertTask(schema);

            Version requestedVersion = p.getVersion();
            var ttlFiles = Optional.ofNullable(files.get(schema)).orElseThrow(() -> new NoSuchElementException(r + " not present in schema section of yaml-file"));

            logger.debug("loading model {} using turtle files: {} ", r, ttlFiles.stream().map(URI::toString).collect(Collectors.joining(", ")));

            Model newModel = fileHandler.readFiles(ttlFiles);
            shapeUpdateInsertTask.model = RdfUtils.modelAsTurtleString(newModel);
            if (fdpSchemaMap.containsKey(r)) {
                Model onFdp = shapesMapFdp.getDefinition(r).map(RdfUtils::fromTurtleString).orElse(new LinkedHashModel());
                shapeUpdateInsertTask.version = shapesMapFdp.getVersion(r).map(v -> v.next(requestedVersion)).orElseThrow(); //next patch version
                shapeUpdateInsertTask.uuid = shapesMapFdp.getUUID(r).orElseThrow();
                shapeUpdateInsertTask.status = Models.isomorphic(onFdp, newModel) ? ShapeStatus.SAME : ShapeStatus.UPDATE;
            } else {
                shapeUpdateInsertTask.version = requestedVersion;
                shapeUpdateInsertTask.uuid = "";
                shapeUpdateInsertTask.status = ShapeStatus.INSERT;
            }
            shapeUpdateInsertTask.parents = p.getParents(r);
            return shapeUpdateInsertTask;
        }).toList();
    }

    public Set<String> getParentUID(SchemaInfo shapesMap) {
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

