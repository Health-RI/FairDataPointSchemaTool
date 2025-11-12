package nl.healthri.fdp.uploadschema.services;

import nl.healthri.fdp.uploadschema.domain.Version;
import nl.healthri.fdp.uploadschema.domain.ShapeTask;
import nl.healthri.fdp.uploadschema.domain.enums.ShapeStatus;
import nl.healthri.fdp.uploadschema.dto.response.Schema.SchemaDataResponse;
import nl.healthri.fdp.uploadschema.utils.*;
import nl.healthri.fdp.uploadschema.utils.Properties;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.net.URI;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShapeTaskServiceTest {

    @Mock
    private FdpServiceInterface fdpServiceMock;
    @Mock
    private FileHandler fileHandlerMock;
    @Mock
    private Properties propertiesMock;
    private ShapeTaskService shapeTaskService;

    @BeforeEach
    void setUp() {
        fdpServiceMock = mock(FdpService.class);
        fileHandlerMock = mock(FileHandler.class);
        propertiesMock = mock(Properties.class);
        shapeTaskService = new ShapeTaskService(fdpServiceMock, fileHandlerMock, propertiesMock);
    }

    Model newModel() {
        String ttl = """
        @prefix ex: <http://example.org/> .
        @prefix foaf: <http://xmlns.com/foaf/0.1/> .

        ex:Alice a foaf:Person ;
            foaf:name "Alice" ;
            foaf:age 30 .
        """;

        return RdfUtils.fromTurtleString(ttl);
    }

    Model newDifferentModel() {
        String ttl = """
        @prefix ex: <http://example.org/> .
        @prefix foaf: <http://xmlns.com/foaf/0.1/> .

        ex:Alice a foaf:Person ;
            foaf:name "Peter" ;
            foaf:age 40 .
        """;

        return RdfUtils.fromTurtleString(ttl);
    }

    @Test
    void propertiesSchemaTitleFoundInPropertiesFiles_WhenLookingForTitleKeyValue_ReturnsNewShapeTask() {
        // Arrange
        String schemaTitle = "TestSchema";
        URI fileUri = URI.create("file://test-schema.ttl");
        List<URI> uris = List.of(fileUri);
        Version version = new Version("1.0.0");

        when(propertiesMock.getFiles()).thenReturn(Map.of(schemaTitle, uris));
        when(propertiesMock.getSchemasToPublish()).thenReturn(List.of(schemaTitle));
        when(propertiesMock.getVersion()).thenReturn(version);
        when(propertiesMock.getParents(schemaTitle)).thenReturn(Set.of());
        when(fdpServiceMock.getAllSchemas()).thenReturn(Collections.emptyList());

        Model model = new LinkedHashModel();
        when(fileHandlerMock.readFiles(uris)).thenReturn(model);

        // Act
        List<ShapeTask> tasks = shapeTaskService.createTasks();

        // Assert
        assertEquals(1, tasks.size());
        ShapeTask task = tasks.getFirst();
        assertEquals(schemaTitle, task.shape);
        assertEquals(ShapeStatus.INSERT, task.status());
        assertEquals(version, task.version);
    }

    @Test
    void propertiesSchemaTitleNotFoundInPropertiesFiles_WhenLookingForTitleKeyValue_ReturnsNoSuchElementException() {
        // Arrange
        String schemaTitle = "MissingSchema";

        // Act
        when(propertiesMock.getSchemasToPublish()).thenReturn(List.of(schemaTitle));
        when(propertiesMock.getFiles()).thenReturn(Map.of());

        // Assert
        assertThrows(NoSuchElementException.class, () -> shapeTaskService.createTasks());
    }

    @Test
    void propertiesSchemaTitleFoundInFdpSchemaInfoMap_WhenLookingForTitleKeyValue_ReturnsShapeTaskWithStatusInsert() {
        // Arrange
        String schemaTitle = "InsertSchema";
        URI uri = URI.create("file://insert-schema.ttl");
        Version version = new Version("1.0.0");

        Model model = newModel();
        String ttl = RdfUtils.modelAsTurtleString(model);

        SchemaDataResponse.Latest latest = new SchemaDataResponse.Latest(
                "uuid-latest", "1.0.0", "vUuid", null, schemaTitle,
                true, false, true, "type", "origin", "imported", ttl, "desc",
                new ArrayList<>(), new ArrayList<>(), "res", "prefix"
        );

        SchemaDataResponse fdpSchemaDataResponse = new SchemaDataResponse(
                "uuid-main", schemaTitle, latest, null,
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
        );

        when(propertiesMock.getFiles()).thenReturn(Map.of(schemaTitle, List.of(uri)));
        when(propertiesMock.getSchemasToPublish()).thenReturn(List.of(schemaTitle));
        when(propertiesMock.getVersion()).thenReturn(version);
        when(propertiesMock.getParents(schemaTitle)).thenReturn(Set.of());
        when(fdpServiceMock.getAllSchemas()).thenReturn(List.of(fdpSchemaDataResponse));
        when(fileHandlerMock.readFiles(List.of(uri))).thenReturn(model);

        // Act
        List<ShapeTask> tasks = shapeTaskService.createTasks();

        // Assert
        assertEquals(1, tasks.size());
        assertEquals(ShapeStatus.SAME, tasks.getFirst().status());
    }

    @Test
    void propertiesSchemaTitleNotFoundInFdpSchemaInfoMap_WhenLookingForTitleKeyValue_ReturnsShapeTaskWithStatusSameOrUpdate() {
        // Arrange
        String schemaTitle = "InsertSchema";
        URI uri = URI.create("file://insert-schema.ttl");
        Version version = new Version("1.0.0");

        when(propertiesMock.getFiles()).thenReturn(Map.of(schemaTitle, List.of(uri)));
        when(propertiesMock.getSchemasToPublish()).thenReturn(List.of(schemaTitle));
        when(propertiesMock.getVersion()).thenReturn(version);
        when(propertiesMock.getParents(schemaTitle)).thenReturn(Set.of());
        when(fdpServiceMock.getAllSchemas()).thenReturn(Collections.emptyList());

        Model model = newModel();
        when(fileHandlerMock.readFiles(List.of(uri))).thenReturn(model);

        // Act
        List<ShapeTask> tasks = shapeTaskService.createTasks();

        // Assert
        assertEquals(1, tasks.size());
        assertEquals(ShapeStatus.INSERT, tasks.getFirst().status());
    }

    @Test
    void NoChangesFound_WhenComparingPropertiesFileWithMatchingFdpShapeFile_ReturnsNewShapeTaskWithStatusSame() {
        // Arrange
        String schemaTitle = "SameSchema";
        URI fileUri = URI.create("file://same-schema.ttl");
        List<URI> uris = List.of(fileUri);
        Version version = new Version("1.0.0");

        Model model = newModel();
        Model sameModel = newModel();
        String ttlDifferentModel = RdfUtils.modelAsTurtleString(sameModel);

        SchemaDataResponse.Latest latest = new SchemaDataResponse.Latest(
                "uuid-latest",
                "1.0.0",
                "versionUuid",
                null,
                schemaTitle,
                true,
                false,
                true,
                "type",
                "origin",
                "importedFrom",
                ttlDifferentModel,
                "desc",
                new ArrayList<>(),
                new ArrayList<>(),
                "resName",
                "urlPrefix"
        );

        SchemaDataResponse existingResponse = new SchemaDataResponse(
                "uuid-main",
                schemaTitle,
                latest,
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );

        when(propertiesMock.getFiles()).thenReturn(Map.of(schemaTitle, uris));
        when(propertiesMock.getSchemasToPublish()).thenReturn(List.of(schemaTitle));
        when(propertiesMock.getVersion()).thenReturn(version);
        when(propertiesMock.getParents(schemaTitle)).thenReturn(Set.of());
        when(fdpServiceMock.getAllSchemas()).thenReturn(List.of(existingResponse));
        when(fileHandlerMock.readFiles(uris)).thenReturn(model);

        // Act
        List<ShapeTask> tasks = shapeTaskService.createTasks();

        // Assert
        assertEquals(ShapeStatus.SAME, tasks.getFirst().status());
    }

    @Test
    void ChangesFound_WhenComparingPropertiesFileWithMatchingFdpShapeFile_ReturnsNewShapeTaskWithStatusUpdate() {
        // Arrange
        String schemaTitle = "SameSchema";
        URI fileUri = URI.create("file://same-schema.ttl");
        List<URI> uris = List.of(fileUri);
        Version version = new Version("1.0.0");

        Model model = newModel();
        Model sameModel = newDifferentModel();
        String ttlDifferentModel = RdfUtils.modelAsTurtleString(sameModel);

        SchemaDataResponse.Latest latest = new SchemaDataResponse.Latest(
                "uuid-latest",
                "1.0.0",
                "versionUuid",
                null,
                schemaTitle,
                true,
                false,
                true,
                "type",
                "origin",
                "importedFrom",
                ttlDifferentModel,
                "desc",
                new ArrayList<>(),
                new ArrayList<>(),
                "resName",
                "urlPrefix"
        );

        SchemaDataResponse existingResponse = new SchemaDataResponse(
                "uuid-main",
                schemaTitle,
                latest,
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );

        when(propertiesMock.getFiles()).thenReturn(Map.of(schemaTitle, uris));
        when(propertiesMock.getSchemasToPublish()).thenReturn(List.of(schemaTitle));
        when(propertiesMock.getVersion()).thenReturn(version);
        when(propertiesMock.getParents(schemaTitle)).thenReturn(Set.of());
        when(fdpServiceMock.getAllSchemas()).thenReturn(List.of(existingResponse));
        when(fileHandlerMock.readFiles(uris)).thenReturn(model);

        // Act
        List<ShapeTask> tasks = shapeTaskService.createTasks();

        // Assert
        assertEquals(ShapeStatus.UPDATE, tasks.getFirst().status());
    }
}