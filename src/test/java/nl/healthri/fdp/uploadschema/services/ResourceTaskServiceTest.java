package nl.healthri.fdp.uploadschema.services;

import nl.healthri.fdp.uploadschema.domain.ResourceTask;
import nl.healthri.fdp.uploadschema.dto.response.Schema.SchemaDataResponse;
import nl.healthri.fdp.uploadschema.utils.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

public class ResourceTaskServiceTest {

    @Mock
    FdpService fdpService;
    @Mock
    Properties properties;

    @InjectMocks
    private ResourceTaskService resourceTaskService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void ResourceNotInSchemaInfoMap_WhenCreatingTasks_ReturnResourceWithExistFalse(){
        // given
        String resourceName = "nonexistent-resource";
        Properties.ResourceProperties resourceProperty = new Properties.ResourceProperties("schema1", null, null);
        when(properties.getResourceProperties()).thenReturn(Map.of(resourceName, resourceProperty));

        // FDP returns no matching resources
        when(fdpService.getAllResources()).thenReturn(List.of());

        // FDP returns schemas
        when(fdpService.getAllSchemas()).thenReturn(List.of(
                new SchemaDataResponse("schema1", "schema-uuid-1", null, null, null, null, null)
        ));

        // when
        List<ResourceTask> result = resourceTaskService.createTasks();

        // then
        assertEquals(1, result.size());
        ResourceTask task = result.getFirst();
        assertEquals(resourceName, task.resource);
        assertFalse(task.exists);
        assertEquals("schema-uuid-1", task.shapeUUUID);
    }
    void ResourceInSchemaInfoMap_WhenCreatingTasks_ReturnResourceWithExistTrue(){

    }

    void ParentResourceNotInSchemaInfoMap_WhenCreatingParentTasks_ReturnResourceWithEmptyChildInfo(){

    }
    void ParentResourceInSchemaInfoMap_WhenCreatingParentTasks_ReturnResourceWithChildInfo(){

    }
}
