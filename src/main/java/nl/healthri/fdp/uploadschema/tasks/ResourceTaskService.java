package nl.healthri.fdp.uploadschema.tasks;

import nl.healthri.fdp.uploadschema.Version;
import nl.healthri.fdp.uploadschema.domain.ResourceTask;
import nl.healthri.fdp.uploadschema.dto.response.Resource.ResourceResponse;
import nl.healthri.fdp.uploadschema.dto.response.Schema.SchemaDataResponse;
import nl.healthri.fdp.uploadschema.integration.FdpService;
import nl.healthri.fdp.uploadschema.utils.Properties;
import nl.healthri.fdp.uploadschema.utils.ResourceInfo;
import nl.healthri.fdp.uploadschema.utils.SchemaInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static nl.healthri.fdp.uploadschema.utils.ResourceInfo.createResourceInfoMap;
import static nl.healthri.fdp.uploadschema.utils.SchemaInfo.createSchemaInfoMap;

public class ResourceTaskService implements  ResourceTaskServiceInterface {
    public FdpService fdpService;
    public Properties properties;

    private static final Logger logger = LoggerFactory.getLogger(ResourceTaskService.class);

    public ResourceTaskService(FdpService fdpService, Properties properties) {
        this.fdpService = fdpService;
        this.properties = properties;
    }

    public List<ResourceTask> createTasks() {
        List<ResourceResponse> resourceResponseList = this.fdpService.getAllResources();
        Map<String, ResourceInfo> resourceInfoMap = createResourceInfoMap(resourceResponseList);

        List<SchemaDataResponse> schemaDataResponseList = this.fdpService.getAllSchemas();
        Map<String, SchemaInfo> schemaInfoMap = createSchemaInfoMap(schemaDataResponseList);


        // Build and validate ResourceTasks
        return properties.resources.entrySet().stream().map(entry -> {
            String resource = entry.getKey();

            // Gets resource id
            String resourceUuid = resourceInfoMap.get(resource).uuid();

            // Get schema id
            String schema = entry.getValue().schema();
            String name = schema.isBlank() ? resource : schema;
            String schemaUUID = schemaInfoMap.get(name).uuid();

            return new ResourceTask(
                    resource,
                    resourceUuid,
                    schemaUUID
            );
        }).toList();
    }

    public List<ResourceTask> createParentTasks() {
        List<ResourceResponse> resourceResponseList = this.fdpService.getAllResources();
        Map<String, ResourceInfo> resourceInfoMap = createResourceInfoMap(resourceResponseList);

        return this.properties.resources.entrySet().stream().map(entry -> {
            String parentName = entry.getValue().parentResource();

            // Gets resource id
            String resourceUuid = resourceInfoMap.get(parentName).uuid();

            // Get child attributes
            String childName = entry.getKey();
            String childIri = entry.getValue().parentRelationIri();
            String childUuid = resourceInfoMap.get(parentName).uuid();

            return new ResourceTask(
                    parentName,
                    resourceUuid,
                    null,
                    childUuid,
                    childIri,
                    childName
            );
        }).toList();
    }
}
