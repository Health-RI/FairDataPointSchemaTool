package nl.healthri.fdp.uploadschema.config.fdp;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.healthri.fdp.uploadschema.dto.Settings.SettingsResponse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toMap;

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

    public Settings() {}

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
            }
        }
    }

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


    // Merges all data from fdpSettings into Settings instance only adding missing form sources from Settings instance
    public Settings Merge(SettingsResponse fdpSettings){
        Settings mergedSettings = settings;

        mergedSettings.clientUrl = fdpSettings.clientUrl();
        mergedSettings.persistentUrl = fdpSettings.persistentUrl();
        mergedSettings.appTitle = fdpSettings.appTitle();
        mergedSettings.appSubtitle = fdpSettings.appSubtitle();
        mergedSettings.appTitleFromConfig = fdpSettings.appTitleFromConfig();
        mergedSettings.appSubtitleFromConfig = fdpSettings.appSubtitleFromConfig();
        mergedSettings.metadataMetrics = fdpSettings.metadataMetrics().stream()
                .map(m -> {
                    MetadataMetric mm = new MetadataMetric();
                    mm.metricUri = m.metricUri();
                    mm.resourceUri = m.resourceUri();
                    return mm;
                }).toList();

        if (mergedSettings.ping == null) mergedSettings.ping = new Ping();
        mergedSettings.ping.enabled = fdpSettings.ping().enabled();
        mergedSettings.ping.endpoints = fdpSettings.ping().endpoints();
        mergedSettings.ping.endpointsFromConfig = fdpSettings.ping().endpointsFromConfig();
        mergedSettings.ping.interval = fdpSettings.ping().interval();

        if (mergedSettings.repository == null) mergedSettings.repository = new Repository();
        this.repository.type = fdpSettings.repository().type();

        if (mergedSettings.search == null) mergedSettings.search = new Search();
        mergedSettings.search.filters = fdpSettings.search().filters();

        if (mergedSettings.forms == null) mergedSettings.forms = new Forms();
        if (mergedSettings.forms.autocomplete == null) mergedSettings.forms.autocomplete = new Forms.Autocomplete();

        mergedSettings.forms.autocomplete.searchNamespace =
                fdpSettings.forms().autocomplete().searchNamespace();

        List<Forms.Autocomplete.Source> mergedSources = new java.util.ArrayList<>();
        List<Forms.Autocomplete.Source> settingsSources = mergedSettings.forms.autocomplete.sources;

        // Creates map with RdfType as key and the Source as value
        // Each source is mapped from SettingsResponse Source to Settings Source.
        List<Forms.Autocomplete.Source> fdpSourceList = new ArrayList<>();
        List<SettingsResponse.Forms.Autocomplete.Source> fdpSources = fdpSettings.forms().autocomplete().sources();
        for (SettingsResponse.Forms.Autocomplete.Source source : fdpSources) {
            Forms.Autocomplete.Source src = new Forms.Autocomplete.Source();
            src.rdfType = source.rdfType();
            src.sparqlEndpoint = source.sparqlEndpoint();
            src.sparqlQuery = source.sparqlQuery();
            mergedSettings.forms.autocomplete.sources.add(src);
        }


        // Checks if each Source in Settings is already the in mergedSettings resource.
        // Adds to mergedSettings resource list if resource is not in merged settings.
        if (settingsSources != null) {
            for (Forms.Autocomplete.Source source : settingsSources) {
                boolean exists = mergedSettings.forms.autocomplete.sources
                        .stream()
                        .anyMatch(s -> Objects.equals(s.rdfType, source.rdfType));

                if (!exists) {
                    mergedSettings.forms.autocomplete.sources.add(source);
                }
            }
        }

        return this;
    }
}
