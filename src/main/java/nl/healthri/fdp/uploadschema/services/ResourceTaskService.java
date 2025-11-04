package nl.healthri.fdp.uploadschema.services;

import nl.healthri.fdp.uploadschema.domain.ResourceTask;
import nl.healthri.fdp.uploadschema.dto.response.Resource.ResourceResponse;
import nl.healthri.fdp.uploadschema.dto.response.Schema.SchemaDataResponse;
import nl.healthri.fdp.uploadschema.integration.FdpService;
import nl.healthri.fdp.uploadschema.utils.Properties;
import nl.healthri.fdp.uploadschema.utils.ResourceInfo;
import nl.healthri.fdp.uploadschema.utils.SchemaInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

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
            String resourceName = entry.getKey();
            String resourceUuid = "";
            boolean exists = false;

            ResourceInfo fdpResourceInfo = resourceInfoMap.get(resourceName);
            if(fdpResourceInfo != null){
                resourceUuid = fdpResourceInfo.uuid();
                exists = true;
            }

            String schema = entry.getValue().schema();
            String name = schema.isBlank() ? resourceName : schema;
            String schemaUUID = schemaInfoMap.get(name).uuid();

            return new ResourceTask(
                    resourceName,
                    resourceUuid,
                    schemaUUID,
                    exists
            );
        }).toList();
    }

    public List<ResourceTask> createParentTasks() {
        List<ResourceResponse> resourceResponseList = this.fdpService.getAllResources();
        Map<String, ResourceInfo> resourceInfoMap = createResourceInfoMap(resourceResponseList);

        return this.properties.resources.entrySet().stream().map(entry -> {
            String parentResourceName = entry.getValue().parentResource();
            String parentResourceUuid = "";
            String childName = "";
            String childIri = "";
            String childUuid = "";
            boolean exists = false;

            ResourceInfo fdpResourceInfo = resourceInfoMap.get(parentResourceName);
            if(fdpResourceInfo != null){
                parentResourceUuid = fdpResourceInfo.uuid();
                childName = entry.getKey();
                childIri = entry.getValue().parentRelationIri();
                childUuid = resourceInfoMap.get(parentResourceName).uuid();
            }

            ResourceTask resourceTask =  new ResourceTask(
                    parentResourceName,
                    parentResourceUuid,
                    null,
                    exists
            );
            resourceTask.addChildInfo(childUuid, childIri, childName);
            return resourceTask;
        }).toList();
    }
}
