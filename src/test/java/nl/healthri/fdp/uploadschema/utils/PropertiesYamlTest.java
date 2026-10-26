package nl.healthri.fdp.uploadschema.utils;

import nl.healthri.fdp.uploadschema.config.fdp.Properties;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class PropertiesYamlTest {

    @Test
    void testLoadPropertiesYamlFromProjectRoot() {
        assertTrue(
                Files.exists(Paths.get("Properties.yaml")),
                "properties.yaml should exist in the project root"
        );
    }

    @Test
    void testDataServiceLinkedToDistribution() throws IOException {
        Properties properties = Properties.load(Paths.get("Properties.yaml").toFile());

        Properties.ResourceProperties dataService = properties.resources.get("Data Service");
        assertNotNull(dataService, "Data Service should be defined in resources");
        assertEquals("Data Service", dataService.schema());
        assertEquals("Distribution", dataService.parentResource());
        assertEquals("http://www.w3.org/ns/dcat#accessService", dataService.parentRelationIri());
    }

    @Test
    void testPropertiesValid() {
        File file = Paths.get("Properties.yaml").toFile();
        try {
            Properties.load(file);
        } catch (IOException e) {
            fail("Failed to load properties.yaml: " + e.getMessage());
        }
    }
}
