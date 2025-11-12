package nl.healthri.fdp.uploadschema.services;

import jakarta.annotation.Resource;
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

    protected record ResourceData(
            String resourceUUID,
            boolean exists) {}

    protected record ParentResourceData(
            String parentResourceName,
            String parentResourceUuid,
            String childUuid,
            String childIri,
            String childName,
            boolean exists) {}


    public List<ResourceTask> createTasks() {
        List<ResourceResponse> fdpResourceResponseList = this.fdpService.getAllResources();
        Map<String, ResourceInfo> fdpResourceInfoMap = createResourceInfoMap(fdpResourceResponseList);

        List<SchemaDataResponse> fdpSchemaDataResponseList = this.fdpService.getAllSchemas();
        Map<String, SchemaInfo> fdpSchemaInfoMap = createSchemaInfoMap(fdpSchemaDataResponseList);


        // Build and validate ResourceTasks
        return properties.resources.entrySet().stream().map(propertyResource -> {
            String resourceName = propertyResource.getKey();
            ResourceData resourceData = getResourceInfo(resourceName, fdpResourceInfoMap);
            String schemaUUID = getSchemaUUID(resourceName, propertyResource.getValue().schema(), fdpSchemaInfoMap);

            return new ResourceTask(
                    resourceName,
                    resourceData.resourceUUID,
                    schemaUUID,
                    resourceData.exists
            );
        }).toList();
    }

    public List<ResourceTask> createParentTasks() {
        List<ResourceResponse> fdpResourceResponseList = this.fdpService.getAllResources();
        Map<String, ResourceInfo> fdpResourceInfoMap = createResourceInfoMap(fdpResourceResponseList);

        return this.properties.resources.entrySet().stream().map(propertyResource -> {
            ParentResourceData parentResourceData = getParentResourceInfo(propertyResource, fdpResourceInfoMap);

            return new ResourceTask(
                    parentResourceData.parentResourceName,
                    parentResourceData.parentResourceUuid,
                    null,
                    parentResourceData.childUuid,
                    parentResourceData.childIri,
                    parentResourceData.childName,
                    parentResourceData.exists
            );
        }).toList();
    }

    // Gets information from property resource parent and creates a new parent resource with parent information.
    protected ParentResourceData getParentResourceInfo(Map.Entry<String, Properties.ResourceProperties> propertyResource, Map<String, ResourceInfo> fdpResourceInfoMap) {
        String propertyResourceParentName = propertyResource.getValue().parentResource();

        ResourceInfo fdpResourceInfo = fdpResourceInfoMap.get(propertyResourceParentName);
        if (fdpResourceInfo == null) {
            return new ParentResourceData(propertyResourceParentName, null, null, null, null, false);
        }

        String parentResourceUuid = fdpResourceInfo.uuid();
        String childName = propertyResource.getKey();
        String childIri =  propertyResource.getValue().parentRelationIri();
        String childUuid = fdpResourceInfoMap.get(childName).uuid();
        boolean exists = true;

        return new ParentResourceData(
                propertyResourceParentName,
                parentResourceUuid,
                childUuid,
                childIri,
                childName,
                exists);
    }

    protected ResourceData getResourceInfo(String resourceName, Map<String, ResourceInfo> fdpResourceInfoMap) {
        ResourceInfo fdpResourceInfo = fdpResourceInfoMap.get(resourceName);

        if (fdpResourceInfo == null) {
            return new ResourceData("", false);
        }

        return new ResourceData(fdpResourceInfo.uuid(), true);
    }

    protected String getSchemaUUID(String resourceName, String schema, Map<String, SchemaInfo> fdpSchemaInfoMap) {
        String name = (schema == null || schema.isBlank()) ? resourceName : schema;

        SchemaInfo schemaInfo = fdpSchemaInfoMap.get(name);
        if (schemaInfo == null) {
            return "";
        }

        return schemaInfo.uuid();
    }
}
