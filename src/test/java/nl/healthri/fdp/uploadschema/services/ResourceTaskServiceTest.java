package nl.healthri.fdp.uploadschema.services;

import nl.healthri.fdp.uploadschema.domain.ResourceTask;
import nl.healthri.fdp.uploadschema.domain.Version;
import nl.healthri.fdp.uploadschema.dto.response.Resource.ResourceResponse;
import nl.healthri.fdp.uploadschema.dto.response.Schema.SchemaDataResponse;
import nl.healthri.fdp.uploadschema.utils.Properties;
import nl.healthri.fdp.uploadschema.utils.ResourceInfo;
import nl.healthri.fdp.uploadschema.utils.SchemaInfo;
import org.apache.poi.poifs.property.Parent;
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
    private Properties properties;
    private ResourceTaskService resourceTaskService;

    @BeforeEach
    void setUp() {
        fdpServiceMock = mock(FdpService.class);
        getProperties();
        resourceTaskService = new ResourceTaskService(fdpServiceMock, properties);
    }

    private void getProperties() {
        Properties properties = new Properties();

        properties.schemas.put("Catalog", List.of("Catalog.ttl", "Agent.ttl", "Kind.ttl"));
        properties.schemas.put("Dataset", List.of("Dataset.ttl", "Agent.ttl", "Kind.ttl","PeriodOfTime.ttl","Attribution.ttl","Identifier.ttl","QualityCertificate.ttl","Relationship.ttl"));
        properties.schemas.put("Resource", List.of("Resource.ttl"));

        properties.parentChild.put("Resource", List.of("Dataset", "Catalog", "Data Service"));

        properties.resources.put("Sample Distribution",
                new Properties.ResourceProperties("Dataset", "http://www.w3.org/ns/adms#sample", "Distribution"));
        properties.resources.put("Dataset Series",
                new Properties.ResourceProperties("Dataset", "http://www.w3.org/ns/dcat#inSeries", "Dataset Series"));
        properties.resources.put("Analytics Distribution",
                new Properties.ResourceProperties("Dataset", "http://healthdataportal.eu/ns/health#analytics", "Distribution"));

        properties.schemasToPublish = List.of("Resource", "Catalog", "Dataset", "Dataset Series", "Distribution", "Data Service");
        properties.schemaVersion = "2.0.0";

        this.properties = properties;
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
        List<ResourceResponse> fdpResourceResponseList = getResourceResponseList("resource-not-in-fdp-1", "resource-not-in-fdp-2", "resource-not-in-fdp-3");
        Map<String, ResourceInfo> fdpResourceInfoMap = createResourceInfoMap(fdpResourceResponseList);

        // Act & Assert
        this.properties.resources.entrySet().forEach(propertyResource -> {
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
        List<ResourceResponse> fdpResourceResponseList = getResourceResponseList("Sample Distribution", "Dataset Series", "Analytics Distribution");
        Map<String, ResourceInfo> fdpResourceInfoMap = createResourceInfoMap(fdpResourceResponseList);

        // Act & Assert
        this.properties.resources.entrySet().forEach(propertyResource -> {
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
        List<SchemaDataResponse> fdpSchemaDataResponseList =  getSchemaDataResponseList("resource-not-in-fdp-1", "resource-not-in-fdp-2", "resource-not-in-fdp-3");
        Map<String, SchemaInfo> fdpSchemaInfoMap = createSchemaInfoMap(fdpSchemaDataResponseList);

        when(fdpServiceMock.getAllSchemas()).thenReturn(fdpSchemaDataResponseList);

        // Act & Assert
        this.properties.resources.entrySet().forEach(propertyResource -> {
            // Act
            String resourceSchemaId = this.resourceTaskService.getSchemaUUID(propertyResource.getKey(), propertyResource.getValue().schema(), fdpSchemaInfoMap);

            // Assert
            assertEquals("", resourceSchemaId);
        });
    }

    @Test
    void PropertyResourceFoundInFdpSchemaInfoMap_WhenGettingSchemaUuid_ReturnsSchemaIdFromFdpSchema() {
        // Arrange
        List<SchemaDataResponse> fdpSchemaDataResponseList =  getSchemaDataResponseList("Catalog", "Dataset", "Resource");
        Map<String, SchemaInfo> fdpSchemaInfoMap = createSchemaInfoMap(fdpSchemaDataResponseList);

        when(fdpServiceMock.getAllSchemas()).thenReturn(fdpSchemaDataResponseList);

        // Act & Assert
        this.properties.resources.entrySet().forEach(propertyResource -> {
            // Act
            String resourceSchemaId = this.resourceTaskService.getSchemaUUID(propertyResource.getKey(), propertyResource.getValue().schema(), fdpSchemaInfoMap);

            // Assert
            String expectedSchemaId = fdpSchemaInfoMap.get(propertyResource.getValue().schema()).uuid();
            assertEquals(expectedSchemaId, resourceSchemaId);
        });
    }

    @Test
    void PropertyParentResourceNotFoundInFdpResourceInfoMap_WhenGettingParentResourceInfo_ReturnEmptyParentResourceData(){
        // Arrange
        List<ResourceResponse> fdpResourceResponseList = getResourceResponseListWithParent("parent-resource-not-in-fdp", "resource-not-in-fdp-1", "resource-not-in-fdp-2", "resource-not-in-fdp-3");
        Map<String, ResourceInfo> fdpResourceInfoMap = createResourceInfoMap(fdpResourceResponseList);

        // Act & Assert
        this.properties.resources.entrySet().forEach(propertyResource -> {
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
        List<ResourceResponse> fdpResourceResponseList = getResourceResponseListWithParent("Dataset", "Sample Distribution", "Dataset Series", "Analytics Distribution");
        Map<String, ResourceInfo> fdpResourceInfoMap = createResourceInfoMap(fdpResourceResponseList);

        // Act & Assert
        this.properties.resources.entrySet().forEach(propertyResource -> {
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

    @Test
    void PropertyParentResourceNotFoundInFdpSchemaInfoMap_WhenCreatingParentTasks_ReturnResourceWithEmptyChildInfo() {
        // Arrange
        List<ResourceResponse> fdpResourceResponseList = getResourceResponseList("Sample Distribution", "Dataset Series", "Analytics Distribution");

        when(fdpServiceMock.getAllResources()).thenReturn(fdpResourceResponseList);

        // Act
        List<ResourceTask> result = resourceTaskService.createParentTasks();

        // Assert
        assertEquals(3, result.size());
        for(ResourceTask task : result){
            assertFalse(task.exists);
        }
    }

    @Test
    void PropertyParentResourceFoundInFdpSchemaInfoMap_WhenCreatingParentTasks_ReturnResourceWithChildInfo(){
        // Arrange
        List<ResourceResponse> fdpResourceResponseList = getResourceResponseList("Sample Distribution", "Dataset Series", "Analytics Distribution");
        List<SchemaDataResponse> schemaDataResponseList =  getSchemaDataResponseList("Resource", "Distribution", "Dataset");

        when(fdpServiceMock.getAllResources()).thenReturn(fdpResourceResponseList);
        when(fdpServiceMock.getAllSchemas()).thenReturn(schemaDataResponseList);

        // Act
        List<ResourceTask> result = resourceTaskService.createTasks();

        // Assert
        assertEquals(3, result.size());
        for(ResourceTask task : result){
            assertTrue(task.exists);
        }
    }

    @Test
    void TestResourceTaskService_RealScenario() {
        // Arrange
        List<ResourceResponse> fdpResourceResponseList =  List.of(
                new ResourceResponse(
                        "2f08228e-1789-40f8-84cd-28e3288c3604",
                        "Dataset",
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                new ResourceResponse(
                        "02c649de-c579-43bb-b470-306abdc808c7",
                        "Distribution",
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                new ResourceResponse(
                        "77aaad6a-0136-4c6e-88b9-07ffccd0ee4c",
                        "FAIR Data Point",
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                new ResourceResponse(
                        "fc089ccc-c06d-4090-bf46-74b9192e5d04",
                        "Dataset Series",
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                new ResourceResponse(
                        "2da98613-5673-4741-b131-a1410953c3f0",
                        "Analytics Distribution",
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                new ResourceResponse(
                        "b117e67a-937c-4115-be6d-d79ef5ddadf4",
                        "Sample Distribution",
                        null,
                        null,
                        null,
                        null,
                        null
                )
        );

        List<SchemaDataResponse> schemaDataResponseList =  List.of(
                new SchemaDataResponse(
                        "6f7a5a76-6185-4bd0-9fe9-62ecc90c9bad",
                        "Metadata Service",
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
                        "a92958ab-a414-47e6-8e17-68ba96ba3a2b",
                        "FAIR Data Point",
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
                        "6a668323-3936-4b53-8380-a4fd2ed082ee",
                        "Resource",
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
                        "866d7fb8-5982-4215-9c7c-18d0ed1bd5f3",
                        "Dataset",
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
                        "0bc517a8-79e5-427a-b0a5-100aa32d58ee",
                        "Dataset Series",
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
                        "ebacbf83-cd4f-4113-8738-d73c0735b0ab",
                        "Distribution",
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
                        "89d94c1b-f6ff-4545-ba9b-120b2d1921d0",
                        "Data Service",
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
                        "2aa7ba63-d27a-4c0e-bfa6-3a4e250f4660",
                        "Catalog",
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
                        "ebacbf83-cd4f-4113-8738-d73c0735b0ab",
                        "Distribution",
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
                        "ebacbf83-cd4f-4113-8738-d73c0735b0ab",
                        "Distribution",
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
                        null,
                        null,
                        null
                )
        );

        when(fdpServiceMock.getAllResources()).thenReturn(fdpResourceResponseList);

        when(fdpServiceMock.getAllSchemas()).thenReturn(schemaDataResponseList);

        // Act
        List<ResourceTask> result = resourceTaskService.createTasks();

        // Assert
        assertEquals(3, result.size());
        for(ResourceTask task : result){
            assertEquals(true, task.exists);
        }
    }
}