package nl.healthri.fdp.uploadschema.utils;

import nl.healthri.fdp.uploadschema.FDP;
import nl.healthri.fdp.uploadschema.SchemaTools;
import nl.healthri.fdp.uploadschema.requestresponses.SchemaDataResponse;
import nl.healthri.fdp.uploadschema.tasks.ShapeUpdateInsertTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class SchemaToolsTest {

    private SchemaTools schemaTools;
    @Mock
    private FDP mockFdp;

    // Initialize Mockito mocks before each test
    @BeforeEach
    void setUp() {
        // Must initialize mocks defined with @Mock
        MockitoAnnotations.openMocks(this);
        schemaTools = new SchemaTools();
    }

    private ShapeUpdateInsertTask createMockTask(String shapeName, boolean isInsert, boolean isSameSchemaResult) throws IOException {
        ShapeUpdateInsertTask realTask = new ShapeUpdateInsertTask(shapeName);
        ShapeUpdateInsertTask spyTask = spy(realTask);
        doReturn(isInsert).when(spyTask).isInsert();

        if (!isInsert) {
            doReturn(isSameSchemaResult).when(spyTask).isSameSchema(any(SchemaDataResponse.class));
        }

        return spyTask;
    }

    // Creates a SchemaDataResponse.
    private SchemaDataResponse createSchemaResponse(String name) {
        SchemaDataResponse mockSchemaDataResponse = mock(SchemaDataResponse.class);

        doReturn(name).when(mockSchemaDataResponse).name();
        return mockSchemaDataResponse;
    }

    /**
     * Condition: Existing schema map is empty.
     * Expected: No updates, as any non-insert task won't find an existing schema.
     */
    @Test
    void filterSchemasToUpdate_NoExistingSchemas_ReturnsEmptyList() throws IOException {
        // Arrange
        when(mockFdp.GetAllSchemas()).thenReturn(Collections.emptyList());

        ShapeUpdateInsertTask taskA = createMockTask("SchemaA", false, false);
        List<ShapeUpdateInsertTask> inputTasks = Collections.singletonList(taskA);

        // Act
        List<ShapeUpdateInsertTask> result = schemaTools.filterSchemasToUpdate(mockFdp, inputTasks);

        // Assert
        assertTrue(result.isEmpty(), "Tasks should be filtered out when no existing schemas are found.");
        verify(taskA, never()).isSameSchema(any());
    }


    /**
     * Condition: The input list of tasks is empty.
     * Expected: Empty result list.
     */
    @Test
    void filterSchemasToUpdate_EmptyInputTaskList_ReturnsEmptyList() throws IOException {
        // Arrange
        List<SchemaDataResponse> existingSchemas = Collections.singletonList(createSchemaResponse("SchemaA"));
        when(mockFdp.GetAllSchemas()).thenReturn(existingSchemas);
        List<ShapeUpdateInsertTask> inputTasks = new ArrayList<>();

        // Act
        List<ShapeUpdateInsertTask> result = schemaTools.filterSchemasToUpdate(mockFdp, inputTasks);

        // Assert
        assertTrue(result.isEmpty(), "Result should be empty when the input task list is empty.");
    }

    /**
     * Condition: A task's 'isInsert()' method returns true.
     * Expected: Insert tasks must be filtered out (ignored).
     */
    @Test
    void filterSchemasToUpdate_TaskIsInsert_TaskIsFilteredOut() throws IOException {
        // Arrange
        List<SchemaDataResponse> existingSchemas = Collections.singletonList(createSchemaResponse("SchemaA"));
        when(mockFdp.GetAllSchemas()).thenReturn(existingSchemas);

        ShapeUpdateInsertTask insertTask = createMockTask("SchemaA", true, false);
        List<ShapeUpdateInsertTask> inputTasks = Collections.singletonList(insertTask);

        // Act
        List<ShapeUpdateInsertTask> result = schemaTools.filterSchemasToUpdate(mockFdp, inputTasks);

        // Assert
        assertTrue(result.isEmpty(), "Insert task should be filtered out.");
        verify(insertTask, times(1)).isSameSchema(any());
    }

    /**
     * Condition: Task is an update, but its shape name is not found in the existing map.
     * Expected: Task must be filtered out.
     */
    @Test
    void filterSchemasToUpdate_ExistingSchemaNotFound_TaskIsFilteredOut() throws IOException {
        // Arrange
        List<SchemaDataResponse> existingSchemas = Collections.singletonList(createSchemaResponse("SchemaA"));
        when(mockFdp.GetAllSchemas()).thenReturn(existingSchemas);

        // Task is for SchemaB (update, but not found)
        ShapeUpdateInsertTask nonExistingTask = createMockTask("SchemaB", false, false);
        List<ShapeUpdateInsertTask> inputTasks = Collections.singletonList(nonExistingTask);

        // Act
        List<ShapeUpdateInsertTask> result = schemaTools.filterSchemasToUpdate(mockFdp, inputTasks);

        // Assert
        assertTrue(result.isEmpty(), "Task targeting a non-existing schema should be filtered out.");
        verify(nonExistingTask, never()).isSameSchema(any());
    }

    /**
     * Condition: Task is an update, schema exists, and 'isSameSchema' returns false.
     * Expected: Task should be included in the result.
     */
    @Test
    void filterSchemasToUpdate_ExistingSchemaIsDifferent_TaskIsIncluded() throws IOException {
        // Arrange
        SchemaDataResponse existingSchemaA = createSchemaResponse("SchemaA");
        when(mockFdp.GetAllSchemas()).thenReturn(Collections.singletonList(existingSchemaA));

        // Task reports *not* being the same schema (false means different/needs update)
        ShapeUpdateInsertTask differentTaskA = createMockTask("SchemaA", false, false);
        List<ShapeUpdateInsertTask> inputTasks = Collections.singletonList(differentTaskA);

        // Act
        List<ShapeUpdateInsertTask> result = schemaTools.filterSchemasToUpdate(mockFdp, inputTasks);

        // Assert
        assertEquals(1, result.size(), "Task for a different schema should be included.");
        assertEquals(differentTaskA, result.getFirst());
        verify(differentTaskA, times(1)).isSameSchema(existingSchemaA);
    }

    /**
     * Condition: Task is an update, schema exists, and 'isSameSchema' returns true.
     * Expected: Task should be filtered out (no update needed).
     */
    @Test
    void filterSchemasToUpdate_ExistingSchemaIsSame_TaskIsFilteredOut() throws IOException {
        // Arrange
        SchemaDataResponse existingSchemaA = createSchemaResponse("SchemaA");
        when(mockFdp.GetAllSchemas()).thenReturn(Collections.singletonList(existingSchemaA));

        // Task reports being the same schema (true means no change)
        ShapeUpdateInsertTask sameTaskA = createMockTask("SchemaA", false, true);
        List<ShapeUpdateInsertTask> inputTasks = Collections.singletonList(sameTaskA);

        // Act
        List<ShapeUpdateInsertTask> result = schemaTools.filterSchemasToUpdate(mockFdp, inputTasks);

        // Assert
        assertTrue(result.isEmpty(), "Task for a schema with the same content should be filtered out.");
        verify(sameTaskA, times(1)).isSameSchema(existingSchemaA);
    }

    /**
     * Condition: Input list contains inserts, non-existent targets, same-content updates, and different-content updates.
     * Expected: Only the tasks requiring updates should be returned.
     */
    @Test
    void filterSchemasToUpdate_MixedTasks_ReturnsOnlyNecessaryUpdates() throws IOException {
        // Arrange
        SchemaDataResponse existingA = createSchemaResponse("SchemaA");
        SchemaDataResponse existingB = createSchemaResponse("SchemaB");
        SchemaDataResponse existingC = createSchemaResponse("SchemaC");

        List<SchemaDataResponse> existingSchemas = Arrays.asList(existingA, existingB, existingC);
        when(mockFdp.GetAllSchemas()).thenReturn(existingSchemas);

        ShapeUpdateInsertTask insertTask = createMockTask("SchemaD", true, false);       // 1. Filtered out (Insert)
        ShapeUpdateInsertTask nonExistingTask = createMockTask("SchemaE", false, false); // 2. Filtered out (Non-existing target)
        ShapeUpdateInsertTask sameTask = createMockTask("SchemaA", false, true);         // 3. Filtered out (Same content)
        ShapeUpdateInsertTask differentTask1 = createMockTask("SchemaB", false, false);   // 4. Included (Different content)
        ShapeUpdateInsertTask differentTask2 = createMockTask("SchemaC", false, false);   // 5. Included (Different content)

        List<ShapeUpdateInsertTask> inputTasks = Arrays.asList(
                insertTask,
                nonExistingTask,
                sameTask,
                differentTask1,
                differentTask2
        );

        // Act
        List<ShapeUpdateInsertTask> result = schemaTools.filterSchemasToUpdate(mockFdp, inputTasks);

        // Assert
        assertEquals(2, result.size(), "Only two tasks (B and C) should be included.");
        assertTrue(result.contains(differentTask1));
        assertTrue(result.contains(differentTask2));

        verify(insertTask, times(0)).isSameSchema(existingB);
        verify(nonExistingTask, times(0)).isSameSchema(existingC);
        verify(sameTask, times(1)).isSameSchema(existingA);
        verify(differentTask1, times(1)).isSameSchema(existingB);
        verify(differentTask2, times(1)).isSameSchema(existingC);
    }
}