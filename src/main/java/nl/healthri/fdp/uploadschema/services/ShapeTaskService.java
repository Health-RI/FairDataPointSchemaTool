package nl.healthri.fdp.uploadschema.services;

import nl.healthri.fdp.uploadschema.domain.Version;
import nl.healthri.fdp.uploadschema.domain.ShapeTask;
import nl.healthri.fdp.uploadschema.domain.enums.ShapeStatus;
import nl.healthri.fdp.uploadschema.dto.response.Schema.SchemaDataResponse;
import nl.healthri.fdp.uploadschema.utils.*;
import nl.healthri.fdp.uploadschema.utils.Properties;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Models;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.*;
import java.util.List;

import static nl.healthri.fdp.uploadschema.utils.SchemaInfo.createSchemaInfoMap;

@Service
public class ShapeTaskService implements  ShapeTaskServiceInterface {
    public FdpService fdpService;
    public FileHandler fileHandler;
    public Properties properties;

    private static final Logger logger = LoggerFactory.getLogger(ShapeTaskService.class);

    public ShapeTaskService(FdpService fdpService, FileHandler fileHandler, Properties properties) {
        this.fdpService = fdpService;
        this.fileHandler = fileHandler;
        this.properties = properties;
    }

    public List<ShapeTask> createTasks() {
        Map<String, List<URI>> files = this.properties.getFiles();
        List<SchemaDataResponse> schemaDataResponseList = this.fdpService.getAllSchemas();
        Map<String, SchemaInfo> schemaInfoMap = createSchemaInfoMap(schemaDataResponseList);

        //list of the task we have to do for insert/updating shacls
        return this.properties.schemasToPublish.stream().map(schemaTitle -> {
            List<URI> ttlFiles = Optional.ofNullable(files.get(schemaTitle)).orElseThrow(() -> new NoSuchElementException(schemaTitle + " not present in schema section of yaml-file"));
            Model newModel = fileHandler.readFiles(ttlFiles);
            String model = RdfUtils.modelAsTurtleString(newModel);
            Version requestedVersion = this.properties.getVersion();
            Set<String> parents = this.properties.getParents(schemaTitle);

            if (schemaInfoMap.containsKey(schemaTitle)) {
                SchemaInfo matchingFdpSchema = schemaInfoMap.get(schemaTitle);
                Version version = matchingFdpSchema.version().next(requestedVersion);
                String uuid = matchingFdpSchema.uuid();
                Model fdpSchemaModel = RdfUtils.fromTurtleString(matchingFdpSchema.definition());
                ShapeStatus status  = Models.isomorphic(fdpSchemaModel, newModel) ? ShapeStatus.SAME : ShapeStatus.UPDATE;

                return new ShapeTask(
                        schemaTitle,
                        version,
                        uuid,
                        parents,
                        model,
                        status
                );
            } else {
                return new ShapeTask(
                        schemaTitle,
                        requestedVersion,
                        "",
                        parents,
                        model,
                        ShapeStatus.INSERT
                );
            }
        }).toList();
    }
}

