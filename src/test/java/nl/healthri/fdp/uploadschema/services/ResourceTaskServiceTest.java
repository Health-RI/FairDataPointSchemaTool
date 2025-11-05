package nl.healthri.fdp.uploadschema.services;

import nl.healthri.fdp.uploadschema.domain.ResourceTask;
import nl.healthri.fdp.uploadschema.domain.Version;
import nl.healthri.fdp.uploadschema.dto.response.Resource.ResourceResponse;
import nl.healthri.fdp.uploadschema.dto.response.Schema.SchemaDataResponse;
import nl.healthri.fdp.uploadschema.utils.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResourceTaskServiceTest {

    private FdpService fdpServiceMock;
    private Properties properties;
    private ResourceTaskService resourceTaskService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        fdpServiceMock = mock(FdpService.class);
        getProperties();
        resourceTaskService = new ResourceTaskService(fdpServiceMock, properties);
    }

    private void getProperties() {
        Properties props = new Properties();

        props.schemas.put("Resource", List.of("Resource.ttl"));
        props.schemas.put("Distribution", List.of("Distribution.ttl", "PeriodOfTime.ttl", "Checksum.ttl"));
        props.schemas.put("Dataset", List.of("Dataset.ttl", "Agent.ttl", "Kind.ttl"));

        props.parentChild.put("Resource", List.of("Dataset", "Catalog", "Data Service"));

        props.resources.put("Sample Distribution",
                new Properties.ResourceProperties("Dataset", "http://www.w3.org/ns/adms#sample", "Distribution"));
        props.resources.put("Dataset Series",
                new Properties.ResourceProperties("Dataset", "http://www.w3.org/ns/dcat#inSeries", "Dataset Series"));
        props.resources.put("Analytics Distribution",
                new Properties.ResourceProperties("Dataset", "http://healthdataportal.eu/ns/health#analytics", "Distribution"));

        props.schemasToPublish = List.of("Resource", "Catalog", "Dataset", "Dataset Series", "Distribution", "Data Service");
        props.schemaVersion = "2.0.0";

        this.properties = props;
    }

    @Test
    void ResourceNotInSchemaInfoMap_WhenCreatingTasks_ReturnResourceWithExistFalse() {
        // given
        List<ResourceResponse> resourceResponseList =  List.of(
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

        when(fdpServiceMock.getAllResources()).thenReturn(resourceResponseList);

        when(fdpServiceMock.getAllSchemas()).thenReturn(schemaDataResponseList);

        // when
        List<ResourceTask> result = resourceTaskService.createTasks();

        // then
        assertEquals(3, result.size()); // 3 resources defined in properties
        ResourceTask sampleDist = result.stream()
                .filter(r -> r.resource.equals("Sample Distribution"))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void ResourceInSchemaInfoMap_WhenCreatingTasks_ReturnResourceWithExistTrue() {

    }

    @Test
    void ParentResourceNotInSchemaInfoMap_WhenCreatingParentTasks_ReturnResourceWithEmptyChildInfo() {


    }

    @Test
    void ParentResourceInSchemaInfoMap_WhenCreatingParentTasks_ReturnResourceWithChildInfo(){

    }
}