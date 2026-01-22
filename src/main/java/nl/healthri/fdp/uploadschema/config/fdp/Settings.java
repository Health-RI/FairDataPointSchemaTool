package nl.healthri.fdp.uploadschema.config.fdp;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.healthri.fdp.uploadschema.dto.settings.SettingsResponseDto;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import nl.healthri.fdp.uploadschema.config.fdp.Settings.Forms.Autocomplete.Source;

public class Settings {
    private static Settings settings;

    public String clientUrl;
    public String persistentUrl;
    public String appTitle;
    public String appSubtitle;
    public String appTitleFromConfig;
    public String appSubtitleFromConfig;
    public List<MetadataMetric> metadataMetrics;
    public Ping ping;
    public Repository repository;
    public Search search;
    public Forms forms;

    public static class MetadataMetric {
        public String metricUri;
        public String resourceUri;

    }

    public static class Ping {
        public boolean enabled;
        public List<String> endpoints;
        public List<String> endpointsFromConfig;
        public String interval;
    }

    public static class Repository {
        public String type;
    }

    public static class Search {
        public List<String> filters;
    }

    public static class Forms {
        public Autocomplete autocomplete;

        public static class Autocomplete {
            public boolean searchNamespace;
            public List<Source> sources;

            public static class Source {
                public String rdfType;
                public String sparqlEndpoint;
                public String sparqlQuery;

                public String getRdfType() {
                    return rdfType;
                }
            }
        }
    }

    public Settings() {}

    // Always returns the existing settings if already initialized.
    public static Settings GetSettings(){
        if(settings == null){
            throw new NullPointerException("Settings instance is not set");
        }

        return settings;
    }

    // Always returns the existing settings if already initialized.
    public static Settings GetSettings(File file) throws IOException {
        if(settings == null){
            if (!file.exists() || !file.isFile()) {
                throw new FileNotFoundException("Settings file not found: " + file.getAbsolutePath());
            }
            ObjectMapper mapper = new ObjectMapper();
            settings = mapper.readValue(file, Settings.class);
        }

        return settings;
    }


    // Merges missing sources from new settings into current settings
    public Settings Merge(Settings newSettings){
        // Early return if new settings are invalid
        if (newSettings == null ||
                newSettings.forms == null ||
                newSettings.forms.autocomplete == null ||
                newSettings.forms.autocomplete.sources == null) {
            return this;
        }

        List<Source> currentSources = this.forms.autocomplete.sources;
        List<Source> newSources = newSettings.forms.autocomplete.sources;

        // Collect existing rdfTypes into a Set for simpler lookup
        Set<String> existingTypes = currentSources.stream()
                .map(Source::getRdfType)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Add sources with rdfTypes not located in currentSources
        newSources.stream()
                .filter(s -> s.getRdfType() != null && !existingTypes.contains(s.getRdfType()))
                .forEach(currentSources::add);

        return this;
    }

    public static Settings convertToEntity(SettingsResponseDto settingsResponseDTO) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(settingsResponseDTO, Settings.class);
    }
}
