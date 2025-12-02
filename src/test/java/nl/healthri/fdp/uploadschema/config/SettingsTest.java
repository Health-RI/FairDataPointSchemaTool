package nl.healthri.fdp.uploadschema.config;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.healthri.fdp.uploadschema.config.fdp.Settings;
import nl.healthri.fdp.uploadschema.dto.Settings.SettingsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SettingsTest {

    @TempDir
    File tempDir;

    // Helper to reset the static 'settings' instance
    @BeforeEach
    void resetSettingsSingleton() throws Exception {
        Field settingsField = Settings.class.getDeclaredField("settings");
        settingsField.setAccessible(true);
        settingsField.set(null, null);
    }

    // Create a minimal SettingsResponse for merging
    private SettingsResponse createMinimalFdpSettingsResponse() {
        return new SettingsResponse(
                "clientUrl", "persistentUrl", "appTitle", "appSubtitle",
                "appTitleConfig", "appSubtitleConfig",
                List.of(),
                new SettingsResponse.Ping(true, List.of("e1"), List.of("e1c"), "1h"),
                new SettingsResponse.Repository("type"),
                new SettingsResponse.Search(List.of("f1")),
                new SettingsResponse.Forms(
                        new SettingsResponse.Forms.Autocomplete(
                                true,
                                List.of(
                                        new SettingsResponse.Forms.Autocomplete.Source("FdpRdfType1", "fsparql1", "fquery1")
                                )
                        )
                )
        );
    }

    // Create a SettingsResponse with an RDF Type used twice
    private SettingsResponse createFdpSettingsResponseWithDuplicates() {
        return new SettingsResponse(
                "clientUrl", "persistentUrl", "appTitle", "appSubtitle",
                "appTitleConfig", "appSubtitleConfig",
                List.of(),
                new SettingsResponse.Ping(true, List.of("e1"), List.of("e1c"), "1h"),
                new SettingsResponse.Repository("type"),
                new SettingsResponse.Search(List.of("f1")),
                new SettingsResponse.Forms(
                        new SettingsResponse.Forms.Autocomplete(
                                true,
                                List.of(
                                        new SettingsResponse.Forms.Autocomplete.Source("DuplicateRdfType", "fsparql1", "fquery1"),
                                        new SettingsResponse.Forms.Autocomplete.Source("DuplicateRdfType", "fsparql2", "fquery2") // DUPLICATE
                                )
                        )
                )
        );
    }

    public File createFile(String jsonFdpSettings) throws IOException {
        // Get initial settings loaded (they don't need sources for this test)
        File validFile = new File(tempDir, "settings.json");
        try (FileWriter writer = new FileWriter(validFile)) {
            writer.write(jsonFdpSettings);
        }

        return validFile;
    }

    @Test
    public void SourceFoundInFdp_WhenMerging_ReturnsSettingsWithFdpSource() throws IOException {
        // ARRANGE
        final String localRdfType = "CommonRdfType";
        final String localQuery = "LocalQuery";

        final String fdpQuery = "FdpQuery";

        String jsonFdpSettings = "{\"forms\": {\"autocomplete\": {\"sources\": [{\"rdfType\": \"" + localRdfType + "\", \"sparqlQuery\": \"" + localQuery + "\"}]}}}";
        File file = createFile(jsonFdpSettings);
        Settings settings = Settings.GetSettings(file);


        // Setup FDP Settings with the same rdfType, but different query
        SettingsResponse fdpSettings = new SettingsResponse(
                "clientUrl", "persistentUrl", "appTitle", "appSubtitle",
                "appTitleConfig", "appSubtitleConfig",
                List.of(),
                new SettingsResponse.Ping(true, List.of(), List.of(), "1h"),
                new SettingsResponse.Repository("type"),
                new SettingsResponse.Search(List.of()),
                new SettingsResponse.Forms(
                        new SettingsResponse.Forms.Autocomplete(
                                true,
                                List.of(
                                        new SettingsResponse.Forms.Autocomplete.Source(localRdfType, "fsparql", fdpQuery)
                                )
                        )
                )
        );

        // ACT
        settings = settings.Merge(fdpSettings);

        // ASSERT
        assertEquals(2, settings.forms.autocomplete.sources.size(), "Should have 1 resource from settings and 1 resource from fdpSettings.");
    }

    @Test
    public void SourceMissingInFdpSettings_WhenMerging_ReturnsSettingsWithSource() throws IOException {
        // ARRANGE
        final String localRdfType = "LocalOnlyRdfType";
        final String localQuery = "LocalQuery";

        String jsonFdpSettings = "{\"forms\": {\"autocomplete\": {\"sources\": [{\"rdfType\": \"" + localRdfType + "\", \"sparqlQuery\": \"" + localQuery + "\"}]}}}";
        File file = createFile(jsonFdpSettings);
        Settings settings = Settings.GetSettings(file);
        SettingsResponse fdpSettings = createMinimalFdpSettingsResponse();

        // ACT
        settings = settings.Merge(fdpSettings);

        // ASSERT
        assertEquals(2, settings.forms.autocomplete.sources.size(), "Should have both local and FDP source.");

        // Check if the local source is preserved
        Settings.Forms.Autocomplete.Source localSource = settings.forms.autocomplete.sources.stream()
                .filter(s -> s.rdfType.equals(localRdfType))
                .findFirst().orElseThrow(() -> new AssertionError("Local source not found in merged settings"));
        assertEquals(localQuery, localSource.sparqlQuery, "Local source's query should be preserved.");

        // Check if the FDP source is present
        settings.forms.autocomplete.sources.stream()
                .filter(s -> s.rdfType.equals("FdpRdfType1"))
                .findFirst().orElseThrow(() -> new AssertionError("FDP source not found in merged settings"));
    }

    @Test
    public void FileNotFound_WhenGettingSettings_ThrowsFileNotFoundException() throws IOException {
        // ARRANGE
        File nonExistentFile = new File(tempDir, "nonExistent.json");

        // ACT && ASSERT
        assertThrows(FileNotFoundException.class, () -> {
            Settings.GetSettings(nonExistentFile);
        });
    }

    @Test
    public void MalformedJsonFile_WhenGettingSettings_ThrowsIOException() throws IOException {
        // ARRANGE
        String jsonFdpSettings = "{ \"forms\": { \"autocomplete\": \"invalid";
        File file = createFile(jsonFdpSettings);
        SettingsResponse fdpSettings = createMinimalFdpSettingsResponse();

        // ACT & ASSERT
        assertThrows(JsonMappingException.class, () -> {
            Settings.GetSettings(file);
        });
    }

    @Test
    public void ValidJsonFile_WhenGettingSettings_ReturnsSettings() throws IOException {
        // ARRANGE
        final String expectedAppTitle = "TestTitle";
        String jsonFdpSettings = "{\"appTitle\":\"" + expectedAppTitle + "\", \"forms\": {}}";
        File file = createFile(jsonFdpSettings);

        // ACT
        Settings settings = Settings.GetSettings(file);

        // ASSERT
        assertNotNull(settings, "Settings should not be null.");
        assertEquals(expectedAppTitle, settings.appTitle, "Settings appTitle should match the file content.");

        // Check singleton logic (calling again returns same instance)
        Settings settings2 = Settings.GetSettings();
        assertSame(settings, settings2, "Subsequent calls should return the same singleton instance.");
    }
}