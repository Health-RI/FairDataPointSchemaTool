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

import javax.xml.validation.Schema;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class ShapeUpdateInsertTask {
    private static final Logger logger = LoggerFactory.getLogger(ShapeUpdateInsertTask.class);



    public ShapeUpdateInsertTask(String shape) {
        this.shape = shape;
    }

    public static List<ShapeUpdateInsertTask> createTasks(FdpService fdpService, Properties p, FileHandler fileHandler){
        final List<String> schemaList = p.schemasToPublish;
        final Map<String, List<URI>> files = p.getFiles();

        final List<SchemaDataResponse> schemaDataResponseList = fdpService.getAllSchemas();

        Map<String, SchemaInfo> schemaInfoMap = new HashMap<>();
        for(SchemaDataResponse schemaDataResponse : schemaDataResponseList) {
            Version version = new Version(schemaDataResponse.latest().version());
            SchemaInfo schemaInfo = new SchemaInfo(
                    version,
                    schemaDataResponse.uuid(),
                    schemaDataResponse.latest().definition());
            schemaInfoMap.put(schemaDataResponse.name(), schemaInfo);
        }

        logger.info("found following shapes on fdp: {}", schemaInfoMap.keySet());

        //list of the task we have to do for insert/updating shacls
        return schemaList.stream().map(schemaTitle -> {
            ShapeUpdateInsertTask shapeUpdateInsertTask = new ShapeUpdateInsertTask(schemaTitle);
            Version requestedVersion = p.getVersion();
            var ttlFiles = Optional.ofNullable(files.get(schemaTitle)).orElseThrow(() -> new NoSuchElementException(schemaTitle + " not present in schema section of yaml-file"));

            logger.debug("loading model {} using turtle files: {} ", schemaTitle, ttlFiles.stream().map(URI::toString).collect(Collectors.joining(", ")));

            Model newModel = fileHandler.readFiles(ttlFiles);
            shapeUpdateInsertTask.model = RdfUtils.modelAsTurtleString(newModel);

            if (schemaInfoMap.containsKey(schemaTitle)) {
                // Gets matching schema title from map
                SchemaInfo matchingFdpSchema = schemaInfoMap.get(schemaTitle);

                // Gets schema model as .ttl format
                Model fdpSchemaModel = RdfUtils.fromTurtleString(matchingFdpSchema.definition());

                // Sets task variables
                shapeUpdateInsertTask.version = matchingFdpSchema.version().next(requestedVersion);
                shapeUpdateInsertTask.uuid = matchingFdpSchema.uuid();
                shapeUpdateInsertTask.status = Models.isomorphic(fdpSchemaModel, newModel) ? ShapeStatus.SAME : ShapeStatus.UPDATE;
            } else {

                shapeUpdateInsertTask.version = requestedVersion;
                shapeUpdateInsertTask.uuid = "";
                shapeUpdateInsertTask.status = ShapeStatus.INSERT;
            }

            shapeUpdateInsertTask.parents = p.getParents(schemaTitle);
            return shapeUpdateInsertTask;
        }).toList();
    }

    public Set<String> getParentUID(Map<String, SchemaInfo> schemaMap) {
        if (this.parents.isEmpty()) {
            return Collections.emptySet();
        }

        return this.parents.stream()
                .map(schemaMap::get) // SchemaInfo
                .map(SchemaInfo::uuid) // SchemaInfo.UUID
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

