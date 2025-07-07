package nl.healthri.fdp.uploadschema.utils;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
    void testPropertiesValid() {
        File file = Paths.get("Properties.yaml").toFile();
        try {
            Properties.load(file);
        } catch (IOException e) {
            fail("Failed to load properties.yaml: " + e.getMessage());
        }
    }
}
