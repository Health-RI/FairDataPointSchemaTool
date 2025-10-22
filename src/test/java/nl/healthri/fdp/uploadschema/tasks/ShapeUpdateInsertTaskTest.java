package nl.healthri.fdp.uploadschema.tasks;

import nl.healthri.fdp.uploadschema.FDP;
import nl.healthri.fdp.uploadschema.Version;
import nl.healthri.fdp.uploadschema.requestresponses.SchemaDataResponse;
import nl.healthri.fdp.uploadschema.utils.FileHandler;
import nl.healthri.fdp.uploadschema.utils.Properties;
import nl.healthri.fdp.uploadschema.utils.RdfUtils;
import nl.healthri.fdp.uploadschema.utils.ShapesMap;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShapeUpdateInsertTaskTest {

    /**
     * Tests the functionality of the `ShapeUpdateInsertTask.createTasks`
     * The test uses mocking to simulate:
     * <p>
     * - Fetching schema data from an FDP (Fair Data Point).
     * - RDF model reading from files via a `FileHandler`.
     * <p>
     * The schema is present on FDP, but the schema of the file is different
     * so the task will be labeled with "UPDATE"
     */
    @Test
    void testCreateTasks_updated_shape() {
        // Mock Properties
        Properties props = Mockito.mock(Properties.class);
        props.schemasToPublish = List.of("TestShape");
        Mockito.when(props.getFiles()).thenReturn(Map.of("TestShape", List.of(URI.create("file:test.ttl"))));
        Mockito.when(props.getVersion()).thenReturn(new Version("2.0.0"));
        Mockito.when(props.getParents("TestShape")).thenReturn(Set.of("ParentShape"));

        // Mock FDP and its fetchSchemaFromFDP result
        ShapesMap shapesOnFdp = createFdpShapeMap(
                createFdpResponse("TestShape", "", "1.0.0", "uuid"));

        FDP fdp = Mockito.mock(FDP.class);
        Mockito.when(fdp.fetchSchemaFromFDP()).thenReturn(shapesOnFdp);

        // Mock FileHandler
        FileHandler fileHandler = Mockito.mock(FileHandler.class);
        Mockito.when(fileHandler.readFiles(Mockito.anyList())).thenReturn(RdfUtils.fromTurtleString(getShapeModel2()));

        // Run createTasks
        List<ShapeUpdateInsertTask> tasks = ShapeUpdateInsertTask.createTasks(props, fdp, fileHandler);

        // Assert
        assertEquals(1, tasks.size());
        ShapeUpdateInsertTask task = tasks.getFirst();
        assertEquals("TestShape", task.shape);
        assertEquals(new Version("2.0.0"), task.version);
        assertEquals(getShapeModel2(), task.model);
        assertEquals(ShapeUpdateInsertTask.ShapeStatus.UPDATE, task.status);
    }

    /**
     * Tests the functionality of the `ShapeUpdateInsertTask.createTasks`
     * The test uses mocking to simulate:
     * <p>
     * - Fetching schema data from an FDP (Fair Data Point).
     * - RDF model reading from files via a `FileHandler`.
     * <p>
     * The schema is on FDP and is the same as from the file. So it the task will be labeled with "SAME"
     */
    @Test
    void testCreateTasks_identical_shape() {
        // Mock Properties
        Properties props = Mockito.mock(Properties.class);
        props.schemasToPublish = List.of("TestShape");
        Mockito.when(props.getFiles()).thenReturn(Map.of("TestShape", List.of(URI.create("file:test.ttl"))));
        Mockito.when(props.getVersion()).thenReturn(new Version("2.0.0"));
        Mockito.when(props.getParents("TestShape")).thenReturn(Set.of("ParentShape"));

        // Mock FDP and its fetchSchemaFromFDP result
        ShapesMap shapesOnFdp = createFdpShapeMap(
                createFdpResponse("TestShape", getShapeModel1(), "1.0.0", "uuid"));

        FDP fdp = Mockito.mock(FDP.class);
        Mockito.when(fdp.fetchSchemaFromFDP()).thenReturn(shapesOnFdp);

        // Mock FileHandler
        FileHandler fileHandler = Mockito.mock(FileHandler.class);
        Mockito.when(fileHandler.readFiles(Mockito.anyList())).thenReturn(RdfUtils.fromTurtleString(getShapeModel1()));

        // Run createTasks
        List<ShapeUpdateInsertTask> tasks = ShapeUpdateInsertTask.createTasks(props, fdp, fileHandler);

        // Assert
        assertEquals(1, tasks.size());
        ShapeUpdateInsertTask task = tasks.getFirst();
        assertEquals("TestShape", task.shape);
        assertEquals(new Version("2.0.0"), task.version);
        assertEquals(getShapeModel1(), task.model);
        assertEquals(ShapeUpdateInsertTask.ShapeStatus.SAME, task.status);
    }

    /**
     * Tests the functionality of the `ShapeUpdateInsertTask.createTasks`
     * The test uses mocking to simulate:
     * <p>
     * - Fetching schema data from an FDP (Fair Data Point).
     * - RDF model reading from files via a `FileHandler`.
     * <p>
     * The schema is not present on the FDP, so the task will be labeled with "SAME"
     */
    @Test
    void testCreateTasks_new() {
        // Mock Properties
        Properties props = Mockito.mock(Properties.class);
        props.schemasToPublish = List.of("TestShape");
        Mockito.when(props.getFiles()).thenReturn(Map.of("TestShape", List.of(URI.create("file:test.ttl"))));
        Mockito.when(props.getVersion()).thenReturn(new Version("2.0.0"));
        Mockito.when(props.getParents("TestShape")).thenReturn(Set.of("ParentShape"));

        // Mock FDP and its fetchSchemaFromFDP result
        ShapesMap shapesOnFdp = createFdpShapeMap(
                createFdpResponse("JustAnotherShape", "", "1.0.0", "uuid"));

        FDP fdp = Mockito.mock(FDP.class);
        Mockito.when(fdp.fetchSchemaFromFDP()).thenReturn(shapesOnFdp);

        // Mock FileHandler
        FileHandler fileHandler = Mockito.mock(FileHandler.class);
        Mockito.when(fileHandler.readFiles(Mockito.anyList())).thenReturn(RdfUtils.fromTurtleString(getShapeModel2()));

        // Run createTasks
        List<ShapeUpdateInsertTask> tasks = ShapeUpdateInsertTask.createTasks(props, fdp, fileHandler);

        // Assert
        assertEquals(1, tasks.size());
        ShapeUpdateInsertTask task = tasks.getFirst();
        assertEquals("TestShape", task.shape);
        assertEquals(new Version("2.0.0"), task.version);
        assertEquals(getShapeModel2(), task.model);
        assertEquals(ShapeUpdateInsertTask.ShapeStatus.INSERT, task.status);
    }

    private ShapesMap createFdpShapeMap(SchemaDataResponse... responses) {
        return new ShapesMap(responses);
    }

    private SchemaDataResponse createFdpResponse(String name, String definition, String version, String uuid) {
        SchemaDataResponse.Latest latest = new SchemaDataResponse.Latest(uuid,
                version, uuid,
                "",
                name, true, false, true,
                "", "", "", definition, "", null, null, "", "");

        return new SchemaDataResponse(uuid, name, latest, null, null, null, null);
    }

    private String getShapeModel1() {
        return linter("""
                @prefix ex:  <https://example.org/> .
                @prefix sh:  <https://www.w3.org/ns/shacl#> .
                @prefix xsd: <https://www.w3.org/2001/XMLSchema#> .
                @prefix foaf:<https://xmlns.com/foaf/0.1/> .
                
                ex:PersonShape a sh:NodeShape ;
                  sh:targetClass foaf:Person ;
                  sh:property ex:NamePropertyShape ;
                  sh:property ex:AgePropertyShape .
                
                ex:NamePropertyShape a sh:PropertyShape ;
                  sh:path foaf:name ;
                  sh:datatype xsd:string ;
                  sh:minCount 1 .
                
                ex:AgePropertyShape a sh:PropertyShape ;
                  sh:path foaf:age ;
                  sh:datatype xsd:integer ;
                  sh:minInclusive 0 ;
                  sh:maxCount 1 .""");
    }

    private String linter(String s) {
        //make sure the model is properly formatted
        return RdfUtils.modelAsTurtleString(RdfUtils.fromTurtleString(s));
    }

    private String getShapeModel2() {

        return linter("""
                @prefix ex:  <https://example.org/> .
                @prefix sh:  <https://www.w3.org/ns/shacl#> .
                @prefix xsd: <https://www.w3.org/2001/XMLSchema#> .
                @prefix foaf:<https://xmlns.com/foaf/0.1/> .
                
                ex:PersonShape a sh:NodeShape ;
                  sh:targetClass foaf:Person ;
                  sh:property ex:NamePropertyShape ;
                  sh:property ex:AgePropertyShape .
                
                ex:NamePropertyShape a sh:PropertyShape ;
                  sh:path foaf:name ;
                  sh:datatype xsd:string ;
                  sh:minCount 100 .
                
                ex:AgePropertyShape a sh:PropertyShape ;
                  sh:path foaf:age ;
                  sh:datatype xsd:integer ;
                  sh:minInclusive 0 ;
                  sh:maxCount 1 .""");
    }
}

