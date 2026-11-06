package nl.healthri.fdp.uploadschema.services;

import nl.healthri.fdp.uploadschema.config.fdp.Properties;
import nl.healthri.fdp.uploadschema.domain.ResourceTask;
import nl.healthri.fdp.uploadschema.dto.resource.ResourceResponseDto;
import nl.healthri.fdp.uploadschema.dto.schema.SchemaDataResponseDto;
import nl.healthri.fdp.uploadschema.utils.ResourceInfo;
import nl.healthri.fdp.uploadschema.utils.SchemaInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static nl.healthri.fdp.uploadschema.utils.ResourceInfo.createResourceInfoMap;
import static nl.healthri.fdp.uploadschema.utils.SchemaInfo.createSchemaInfoMap;

public class ResourceTaskService implements  ResourceTaskServiceInterface {
    public FdpServiceInterface fdpService;
    public Properties properties;

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
        List<ResourceResponseDto> fdpResourceResponseList = this.fdpService.getAllResources();
        Map<String, ResourceInfo> fdpResourceInfoMap = createResourceInfoMap(fdpResourceResponseList);

        List<SchemaDataResponseDto> fdpSchemaDataResponseList = this.fdpService.getAllSchemas();
        Map<String, SchemaInfo> fdpSchemaInfoMap = createSchemaInfoMap(fdpSchemaDataResponseList);

        return properties.resources.entrySet().stream().map(propertyResource -> {
            String resourceName = propertyResource.getKey();

            // Gets all needed resource information from fdpResourceInfoMap to set to new resourceTask
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
        List<ResourceResponseDto> fdpResourceResponseList = this.fdpService.getAllResources();
        Map<String, ResourceInfo> fdpResourceInfoMap = createResourceInfoMap(fdpResourceResponseList);

        return this.properties.resources.entrySet().stream()
                .flatMap(propertyResource -> getParentNames(propertyResource).stream()
                        .map(parentName -> {
                            // Gets all needed parent resource information from fdpResourceInfoMap to set to new resourceTask
                            ParentResourceData parentResourceData = getParentResourceInfo(
                                    propertyResource.getKey(),
                                    parentName,
                                    propertyResource.getValue().parentRelationIri(),
                                    fdpResourceInfoMap);

                            return new ResourceTask(
                                    parentResourceData.parentResourceName,
                                    parentResourceData.parentResourceUuid,
                                    null,
                                    parentResourceData.childUuid,
                                    parentResourceData.childIri,
                                    parentResourceData.childName,
                                    parentResourceData.exists
                            );
                        }))
                .toList();
    }

    // Children are defined per resource definition in the FDP and are not inherited. So a child of a parent
    // (e.g. Distribution) is also added to every resource that uses that parent as schema
    // (e.g. Sample Distribution and Analytics Distribution).
    protected List<String> getParentNames(Map.Entry<String, Properties.ResourceProperties> propertyResource) {
        String childName = propertyResource.getKey();
        String parentName = propertyResource.getValue().parentResource();

        List<String> parentNames = new ArrayList<>();
        parentNames.add(parentName);
        if (parentName != null) {
            this.properties.resources.entrySet().stream()
                    .filter(resource -> !resource.getKey().equals(childName) && !resource.getKey().equals(parentName))
                    .filter(resource -> parentName.equals(getSchemaName(resource.getKey(), resource.getValue().schema())))
                    .map(Map.Entry::getKey)
                    .sorted()
                    .forEach(parentNames::add);
        }

        return parentNames;
    }

    // Gets information from property resource parent and creates a new parent resource with parent information.
    protected ParentResourceData getParentResourceInfo(Map.Entry<String, Properties.ResourceProperties> propertyResource, Map<String, ResourceInfo> fdpResourceInfoMap) {
        return getParentResourceInfo(
                propertyResource.getKey(),
                propertyResource.getValue().parentResource(),
                propertyResource.getValue().parentRelationIri(),
                fdpResourceInfoMap);
    }

    protected ParentResourceData getParentResourceInfo(String childName, String propertyResourceParentName, String parentRelationIri, Map<String, ResourceInfo> fdpResourceInfoMap) {
        ResourceInfo fdpResourceInfo = fdpResourceInfoMap.get(propertyResourceParentName);
        if (fdpResourceInfo == null) {
            return new ParentResourceData(propertyResourceParentName, null, null, null, null, false);
        }

        String parentResourceUuid = fdpResourceInfo.uuid();
        String childIri =  parentRelationIri;
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

    protected String getSchemaName(String resourceName, String schema) {
        return (schema == null || schema.isBlank()) ? resourceName : schema;
    }

    protected String getSchemaUUID(String resourceName, String schema, Map<String, SchemaInfo> fdpSchemaInfoMap) {
        String name = getSchemaName(resourceName, schema);
        SchemaInfo schemaInfo = fdpSchemaInfoMap.get(name);
        if (schemaInfo == null) {
            return "";
        }

        return schemaInfo.uuid();
    }
}
