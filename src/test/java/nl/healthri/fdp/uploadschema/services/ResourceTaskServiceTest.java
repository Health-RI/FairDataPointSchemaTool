package nl.healthri.fdp.uploadschema.services;

import nl.healthri.fdp.uploadschema.domain.ResourceTask;
import nl.healthri.fdp.uploadschema.domain.Version;
import nl.healthri.fdp.uploadschema.dto.response.Resource.ResourceResponse;
import nl.healthri.fdp.uploadschema.dto.response.Schema.SchemaDataResponse;
import nl.healthri.fdp.uploadschema.utils.Properties;
import nl.healthri.fdp.uploadschema.utils.ResourceInfo;
import nl.healthri.fdp.uploadschema.utils.SchemaInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static nl.healthri.fdp.uploadschema.utils.ResourceInfo.createResourceInfoMap;
import static nl.healthri.fdp.uploadschema.utils.SchemaInfo.createSchemaInfoMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResourceTaskServiceTest {

    @Mock
    private FdpService fdpServiceMock;
    private ResourceTaskService resourceTaskService;

    @BeforeEach
    void setUp() {
        fdpServiceMock = mock(FdpService.class);
        Properties properties = getRealProperties();
        resourceTaskService = new ResourceTaskService(fdpServiceMock, properties);
    }

    private Properties getTestProperties() {
        Properties properties = new Properties();

        properties.schemas.put("Catalog", List.of("Catalog.ttl", "Agent.ttl", "Kind.ttl"));
        properties.schemas.put("Dataset", List.of("Dataset.ttl", "Agent.ttl", "Kind.ttl","PeriodOfTime.ttl","Attribution.ttl","Identifier.ttl","QualityCertificate.ttl","Relationship.ttl"));
        properties.schemas.put("Resource", List.of("Resource.ttl"));

        properties.parentChild.put("Resource", List.of("Dataset", "Catalog", "Data Service"));

        properties.resources.put("Sample Distribution",
                new Properties.ResourceProperties("Dataset", "http://www.w3.org/ns/adms#sample", "Catalog"));
        properties.resources.put("Dataset Series",
                new Properties.ResourceProperties("Dataset", "http://www.w3.org/ns/dcat#inSeries", "Dataset"));
        properties.resources.put("Analytics Distribution",
                new Properties.ResourceProperties("Dataset", "http://healthdataportal.eu/ns/health#analytics", "Resource"));

        properties.schemasToPublish = List.of("Resource", "Catalog", "Dataset", "Dataset Series", "Distribution", "Data Service");
        properties.schemaVersion = "2.0.0";

        return properties;
    }

    private Properties getRealProperties() {
        Properties properties = new Properties();

        properties.schemas.put("Catalog", List.of("Catalog.ttl", "Agent.ttl", "Kind.ttl", "PeriodOfTime.ttl"));
        properties.schemas.put("Dataset", List.of("Dataset.ttl", "Agent.ttl", "Kind.ttl", "PeriodOfTime.ttl", "Attribution.ttl", "Identifier.ttl", "QualityCertificate.ttl", "Relationship.ttl"));
        properties.schemas.put("Dataset Series", List.of("DatasetSeries.ttl", "Agent.ttl", "PeriodOfTime.ttl", "Kind.ttl"));
        properties.schemas.put("Resource", List.of("Resource.ttl"));
        properties.schemas.put("Distribution", List.of("Distribution.ttl", "PeriodOfTime.ttl", "Checksum.ttl"));
        properties.schemas.put("Data Service", List.of("DataService.ttl", "Agent.ttl", "Kind.ttl", "Identifier.ttl"));

        properties.parentChild.put("Resource", List.of("Dataset", "Catalog", "Data Service"));

        properties.resources.put("Sample Distribution",
                new Properties.ResourceProperties("Dataset", "http://www.w3.org/ns/adms#sample", "Distribution"));
        properties.resources.put("Dataset Series",
                new Properties.ResourceProperties("Dataset", "http://www.w3.org/ns/dcat#inSeries", "Dataset Series"));
        properties.resources.put("Analytics Distribution",
                new Properties.ResourceProperties("Dataset", "http://healthdataportal.eu/ns/health#analytics", "Distribution"));

        properties.schemasToPublish = List.of(
                "Resource",
                "Catalog",
                "Dataset",
                "Dataset Series",
                "Distribution",
                "Data Service"
        );

        properties.schemaVersion = "2.0.0";
        properties.inputDir = "https://raw.githubusercontent.com/Health-RI/health-ri-metadata/master/Formalisation(shacl)/Core/PiecesShape/";
        properties.templateDir = "C:\\Users\\PatrickDekker(Health\\templates\\";
        properties.outputRoot = "C:\\Users\\PatrickDekker(Health\\IdeaProjects\\health-ri-metadata\\Formalisation(shacl)\\Core\\";
        properties.piecesDir = "PiecesShape";
        properties.fairDataPointDir = "FairDataPointShape";
        properties.validationDir = "ValidationShape";

        return properties;
    }

    List<ResourceResponse> getResourceResponseList(String name1, String name2, String name3) {
        return List.of(
                new ResourceResponse(
                        "1",
                        name1,
                        null, null, null, null, null
                ),
                new ResourceResponse(
                        "2",
                        name2,
                        null, null, null, null, null

                ),
                new ResourceResponse(
                        "3",
                        name3,
                        null, null, null, null, null

                )
        );
    }

    List<ResourceResponse> getResourceResponseListWithParent(String name1, String name2, String name3, String name4) {
        return List.of(
                new ResourceResponse(
                        "1",
                        name1,
                        null, null, null, null, null
                ),
                new ResourceResponse(
                        "2",
                        name2,
                        null, null, null, null, null

                ),
                new ResourceResponse(
                        "3",
                        name3,
                        null, null, null, null, null
                ),
        new ResourceResponse(
                "4",
                name4,
                null, null, null, null, null
        )
        );
    }

    List<SchemaDataResponse> getSchemaDataResponseList(String name1, String name2, String name3) {
        return List.of(
                new SchemaDataResponse(
                        "1",
                        name1,
                        new SchemaDataResponse.Latest(
                                null,
                                new Version(1,  0, 0).toString(),
                                null,
                                null,
                                null,
                                false,
                                false,
                                true,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null
                        ),
                        null,
                        new ArrayList<>(List.of(new Version(1, 0, 0).toString())),
                        null,
                        null
                ),
                new SchemaDataResponse(
                        "2",
                        name2,
                        new SchemaDataResponse.Latest(
                                null,
                                new Version(1,  0, 0).toString(),
                                null,
                                null,
                                null,
                                false,
                                false,
                                true,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null
                        ),
                        null,
                        new ArrayList<>(List.of(new Version(1, 0, 0).toString())),
                        null,
                        null
                ),
                new SchemaDataResponse(
                        "3",
                        name3,
                        new SchemaDataResponse.Latest(
                                null,
                                new Version(1,  0, 0).toString(),
                                null,
                                null,
                                null,
                                false,
                                false,
                                true,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null
                        ),
                        null,
                        new ArrayList<>(List.of(new Version(1, 0, 0).toString())),
                        null,
                        null
                )
        );
    }


    @Test
    void PropertyResourceNotFoundInFdpResourceInfoMap_WhenGettingResourceInfo_ResourceDataEmptyIdAndExistsFalse() {
        // Arrange
        Properties properties = getTestProperties();
        List<ResourceResponse> fdpResourceResponseList = getResourceResponseList("resource-not-in-fdp-1", "resource-not-in-fdp-2", "resource-not-in-fdp-3");
        Map<String, ResourceInfo> fdpResourceInfoMap = createResourceInfoMap(fdpResourceResponseList);

        // Act & Assert
        properties.resources.entrySet().forEach(propertyResource -> {
            ResourceTaskService.ResourceData resourceData =
                    this.resourceTaskService.getResourceInfo(propertyResource.getKey(), fdpResourceInfoMap);

            // Assert
            assertEquals("", resourceData.resourceUUID());
            assertFalse(resourceData.exists());
        });
    }

    @Test
    void PropertyResourceFoundInFdpResourceInfoMap_WhenGettingResourceInfo_ResourceDataIdIsFdpUuidIdAndExistsTrue() {
        // Arrange
        Properties properties = getTestProperties();
        List<ResourceResponse> fdpResourceResponseList = getResourceResponseList("Sample Distribution", "Dataset Series", "Analytics Distribution");
        Map<String, ResourceInfo> fdpResourceInfoMap = createResourceInfoMap(fdpResourceResponseList);

        // Act & Assert
        properties.resources.entrySet().forEach(propertyResource -> {
            ResourceTaskService.ResourceData resourceData =
                    this.resourceTaskService.getResourceInfo(propertyResource.getKey(), fdpResourceInfoMap);

            // Assert
            String fdpResourceUuid = fdpResourceInfoMap.get(propertyResource.getKey()).uuid();
            assertEquals(fdpResourceUuid, resourceData.resourceUUID());
            assertTrue(resourceData.exists());
        });
    }

    @Test
    void PropertyResourceNotFoundInFdpSchemaInfoMap_WhenGettingSchemaUuid_ReturnsEmptyId() {
        // Arrange
        Properties properties = getTestProperties();
        List<SchemaDataResponse> fdpSchemaDataResponseList =  getSchemaDataResponseList("resource-not-in-fdp-1", "resource-not-in-fdp-2", "resource-not-in-fdp-3");
        Map<String, SchemaInfo> fdpSchemaInfoMap = createSchemaInfoMap(fdpSchemaDataResponseList);

        when(fdpServiceMock.getAllSchemas()).thenReturn(fdpSchemaDataResponseList);

        // Act & Assert
        properties.resources.entrySet().forEach(propertyResource -> {
            // Act
            String resourceSchemaId = this.resourceTaskService.getSchemaUUID(propertyResource.getKey(), propertyResource.getValue().schema(), fdpSchemaInfoMap);

            // Assert
            assertEquals("", resourceSchemaId);
        });
    }

    @Test
    void PropertyResourceFoundInFdpSchemaInfoMap_WhenGettingSchemaUuid_ReturnsSchemaIdFromFdpSchema() {
        // Arrange
        Properties properties = getTestProperties();
        List<SchemaDataResponse> fdpSchemaDataResponseList =  getSchemaDataResponseList("Catalog", "Dataset", "Resource");
        Map<String, SchemaInfo> fdpSchemaInfoMap = createSchemaInfoMap(fdpSchemaDataResponseList);

        when(fdpServiceMock.getAllSchemas()).thenReturn(fdpSchemaDataResponseList);

        // Act & Assert
        properties.resources.entrySet().forEach(propertyResource -> {
            String propertyResourceName = propertyResource.getKey();
            String propertyResourceSchema = propertyResource.getValue().schema();

            // Act
            String resourceSchemaId = this.resourceTaskService.getSchemaUUID(propertyResourceName, propertyResourceSchema, fdpSchemaInfoMap);

            // Assert
            String expectedSchemaId = fdpSchemaInfoMap.get(propertyResource.getValue().schema()).uuid();
            assertEquals(expectedSchemaId, resourceSchemaId);
        });
    }

    @Test
    void PropertyParentResourceNotFoundInFdpResourceInfoMap_WhenGettingParentResourceInfo_ReturnEmptyParentResourceData(){
        // Arrange
        Properties properties = getTestProperties();
        List<ResourceResponse> fdpResourceResponseList = getResourceResponseListWithParent("parent-resource-not-in-fdp", "resource-not-in-fdp-1", "resource-not-in-fdp-2", "resource-not-in-fdp-3");
        Map<String, ResourceInfo> fdpResourceInfoMap = createResourceInfoMap(fdpResourceResponseList);

        // Act & Assert
        properties.resources.entrySet().forEach(propertyResource -> {
            ResourceTaskService.ParentResourceData resourceData =
                    this.resourceTaskService.getParentResourceInfo(propertyResource, fdpResourceInfoMap);

            // Assert
            assertEquals(propertyResource.getValue().parentResource(), resourceData.parentResourceName());
            assertNull(resourceData.parentResourceUuid());
            assertNull(resourceData.childUuid());
            assertNull(resourceData.childIri());
            assertNull(resourceData.childName());
            assertFalse(resourceData.exists());
        });
    }

    // TODO:
    @Test
    void PropertyParentResourceFoundInFdpResourceInfoMap_WhenGettingParentResourceInfo_ReturnParentResourceDataWithFilledChildInfo(){
        // Arrange
        Properties properties = getTestProperties();
        List<ResourceResponse> fdpResourceResponseList = getResourceResponseListWithParent("Dataset", "Sample Distribution", "Dataset Series", "Analytics Distribution");
        Map<String, ResourceInfo> fdpResourceInfoMap = createResourceInfoMap(fdpResourceResponseList);

        // Act & Assert
        properties.resources.entrySet().forEach(propertyResource -> {
            ResourceTaskService.ParentResourceData resourceData =
                    this.resourceTaskService.getParentResourceInfo(propertyResource, fdpResourceInfoMap);

            // Assert
            String parentResource = propertyResource.getValue().parentResource();
            assertEquals(parentResource, resourceData.parentResourceName());
            assertNotNull(resourceData.parentResourceUuid());
            assertNotNull(resourceData.childUuid());
            assertEquals(propertyResource.getValue().parentRelationIri(), resourceData.childIri());
            assertEquals(propertyResource.getKey(), resourceData.childName());
            assertTrue(resourceData.exists());
        });
    }

    // TODO:
    // createParentsgood
    // createParentsntogood
    // createtasksgood
    // createtasksnotgood

    // todo:
    @Test
    void AllPropertyResourcesFoundInFdpResourceInfoMap_WhenCreatingTasks_ReturnsTasksThatExist() {
        // Arrange
        Properties properties = getRealProperties();
        resourceTaskService = new ResourceTaskService(fdpServiceMock, properties);

        List<ResourceResponse> fdpResourceResponseList = getResourceResponseList("Sample Distribution", "Dataset Series", "Analytics Distribution");
        Map<String, ResourceInfo> fdpResourceMap = createResourceInfoMap(fdpResourceResponseList);
        when(fdpServiceMock.getAllResources()).thenReturn(fdpResourceResponseList);

        List<SchemaDataResponse> fdpSchemaDataResponseList = getSchemaDataResponseList("Distribution", "Dataset Series", "Distribution");
        Map<String, SchemaInfo> schemaInfoMap = createSchemaInfoMap(fdpSchemaDataResponseList);
        when(fdpServiceMock.getAllSchemas()).thenReturn(fdpSchemaDataResponseList);

        // Act
        List<ResourceTask> result = resourceTaskService.createTasks();

        // Assert
        assertEquals(properties.resources.size(), result.size());
        for (ResourceTask task : result) {
            Properties.ResourceProperties resourceProperty = properties.resources.get(task.resource);
            String expectedResourceName = fdpResourceMap.get(task.resource).name();
            String expectedResourceId = fdpResourceMap.get(task.resource).uuid();
            String expectedResourceSchemaUuid = schemaInfoMap.get(resourceProperty.schema()).uuid();

            assertEquals(expectedResourceName, task.resource);
            assertEquals(expectedResourceId, task.UUID);
            assertEquals(expectedResourceSchemaUuid, task.shapeUUUID);
            assertTrue(task.exists);
        }
    }

    @Test
    void AllPropertyResourcesNotFoundInFdpResourceInfoMap_WhenCreatingTasks_ReturnsTasksThatDoNotExist() {
        // Arrange
        Properties properties = getRealProperties();
        resourceTaskService = new ResourceTaskService(fdpServiceMock, properties);

        List<ResourceResponse> fdpResourceResponseList = getResourceResponseList("not-in-fdp-1", "not-in-fdp-2", "not-in-fdp-3");
        Map<String, ResourceInfo> fdpResourceMap = createResourceInfoMap(fdpResourceResponseList);
        when(fdpServiceMock.getAllResources()).thenReturn(fdpResourceResponseList);

        List<SchemaDataResponse> fdpSchemaDataResponseList = getSchemaDataResponseList("Distribution", "Dataset Series", "Distribution");
        Map<String, SchemaInfo> schemaInfoMap = createSchemaInfoMap(fdpSchemaDataResponseList);
        when(fdpServiceMock.getAllSchemas()).thenReturn(fdpSchemaDataResponseList);

        // Act
        List<ResourceTask> result = resourceTaskService.createTasks();

        // Assert
        assertEquals(properties.resources.size(), result.size());
        for (ResourceTask task : result) {
            assertEquals("", task.UUID);
            assertFalse(task.exists);
        }
    }

    @Test
    void AllPropertyResourcesFoundInFdpResourceInfoMap_WhenCreatingParentTasks_ReturnsTasksWithFilledChildDataAndExistsIsTrue() {
        // Arrange
        Properties properties = getRealProperties();
        resourceTaskService = new ResourceTaskService(fdpServiceMock, properties);

        List<ResourceResponse> fdpResourceResponseList = getResourceResponseListWithParent("Dataset", "Sample Distribution", "Dataset Series", "Analytics Distribution");
        Map<String, ResourceInfo> resourceInfoMap = createResourceInfoMap(fdpResourceResponseList);
        when(fdpServiceMock.getAllResources()).thenReturn(fdpResourceResponseList);

        // Act
        List<ResourceTask> result = resourceTaskService.createParentTasks();

        assertEquals(properties.resources.size(), result.size());
        for (ResourceTask task : result) {
            Properties.ResourceProperties resourceProperty = properties.resources.get(task.childName);
            String expectedParentResourceName = resourceProperty.parentResource();
            String expectedUuid = resourceInfoMap.get(resourceProperty.parentResource()).uuid();
            String expectedChildUuid = resourceInfoMap.get(task.childName).uuid();
            String expectedChildIri = resourceProperty.parentRelationIri();

            // Assert
            assertEquals(expectedParentResourceName, task.resource);
            assertEquals(expectedUuid, task.UUID);
            assertEquals(expectedChildUuid, task.childUUuid);
            assertEquals(expectedChildIri, task.childRelationIri);
            assertTrue(task.exists);
        }
    }

    @Test
    void AllPropertyResourcesNotFoundInFdpResourceInfoMap_WhenCreatingParentTasks_ReturnsTasksWithEmptyChildDataAndExistsIsFalse() {
        // Arrange
        Properties properties = getRealProperties();
        resourceTaskService = new ResourceTaskService(fdpServiceMock, properties);

        List<ResourceResponse> fdpResourceResponseList = getResourceResponseListWithParent("not-in-fdp-parent", "not-in-fdp-1", "not-in-fdp-2", "not-in-fdp-3");
        when(fdpServiceMock.getAllResources()).thenReturn(fdpResourceResponseList);

        // Act
        List<ResourceTask> result = resourceTaskService.createParentTasks();

        // Assert
        assertEquals(properties.resources.size(), result.size());
        for (ResourceTask task : result) {
            assertNull(task.childUUuid);
            assertNull(task.childRelationIri);
            assertFalse(task.exists);
        }
    }

}