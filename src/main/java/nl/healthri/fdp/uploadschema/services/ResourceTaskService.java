package nl.healthri.fdp.uploadschema.services;

import nl.healthri.fdp.uploadschema.domain.ResourceTask;
import nl.healthri.fdp.uploadschema.dto.response.Resource.ResourceResponse;
import nl.healthri.fdp.uploadschema.dto.response.Schema.SchemaDataResponse;
import nl.healthri.fdp.uploadschema.utils.Properties;
import nl.healthri.fdp.uploadschema.utils.ResourceInfo;
import nl.healthri.fdp.uploadschema.utils.SchemaInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static nl.healthri.fdp.uploadschema.utils.ResourceInfo.createResourceInfoMap;
import static nl.healthri.fdp.uploadschema.utils.SchemaInfo.createSchemaInfoMap;

@Service
public class ResourceTaskService implements  ResourceTaskServiceInterface {
    public FdpServiceInterface fdpService;
    public Properties properties;

    private static final Logger logger = LoggerFactory.getLogger(ResourceTaskService.class);

    public ResourceTaskService(FdpServiceInterface fdpService, Properties properties) {
        this.fdpService = fdpService;
        this.properties = properties;
    }

    public List<ResourceTask> createTasks() {
        List<ResourceResponse> fdpResourceResponseList = this.fdpService.getAllResources();
        Map<String, ResourceInfo> fdpResourceInfoMap = createResourceInfoMap(fdpResourceResponseList);

        List<SchemaDataResponse> fdpSchemaDataResponseList = this.fdpService.getAllSchemas();
        Map<String, SchemaInfo> fdpSchemaInfoMap = createSchemaInfoMap(fdpSchemaDataResponseList);


        // Build and validate ResourceTasks
        return properties.resources.entrySet().stream().map(entry -> {
            String resourceName = entry.getKey();
            String resourceUuid = "";
            String schemaUUID = "";
            boolean exists = false;

            ResourceInfo fdpResourceInfo = fdpResourceInfoMap.get(resourceName);
            if(fdpResourceInfo != null){
                resourceUuid = fdpResourceInfo.uuid();
                exists = true;
            }

            String schema = entry.getValue().schema();
            String name = schema.isBlank() ? resourceName : schema;

            SchemaInfo schemaInfo = fdpSchemaInfoMap.get(name);
            if(schemaInfo != null){
                schemaUUID = fdpSchemaInfoMap.get(name).uuid();
            }

            return new ResourceTask(
                    resourceName,
                    resourceUuid,
                    schemaUUID,
                    exists
            );
        }).toList();
    }

    public List<ResourceTask> createParentTasks() {
        List<ResourceResponse> fdpResourceResponseList = this.fdpService.getAllResources();
        Map<String, ResourceInfo> fdpResourceInfoMap = createResourceInfoMap(fdpResourceResponseList);

        return this.properties.resources.entrySet().stream().map(entry -> {
            String parentResourceName = entry.getValue().parentResource();
            String parentResourceUuid = "";
            String childName = null;
            String childIri = null;
            String childUuid = null;
            boolean exists = false;

            ResourceInfo fdpResourceInfo = fdpResourceInfoMap.get(parentResourceName);
            if(fdpResourceInfo != null){
                parentResourceUuid = fdpResourceInfo.uuid();
                childName = entry.getKey();
                childIri = entry.getValue().parentRelationIri();
                childUuid = fdpResourceInfoMap.get(parentResourceName).uuid();
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
