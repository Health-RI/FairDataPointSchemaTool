package nl.healthri.fdp.uploadschema.config.fdp;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import nl.healthri.fdp.uploadschema.dto.Settings.SettingsResponse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

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

        List<Forms.Autocomplete.Source> mergedSources =
                new java.util.ArrayList<>();

        List<Forms.Autocomplete.Source> existingSources =
                mergedSettings.forms.autocomplete.sources;

        List<SettingsResponse.Forms.Autocomplete.Source> incomingSources =
                fdpSettings.forms().autocomplete().sources();

        // todo: allow multiple rdf types?
        java.util.Map<String, Forms.Autocomplete.Source> incomingByRdfType =
                incomingSources.stream().collect(
                        java.util.stream.Collectors.toMap(
                                SettingsResponse.Forms.Autocomplete.Source::rdfType,
                                s -> {
                                    Forms.Autocomplete.Source src = new Forms.Autocomplete.Source();
                                    src.rdfType = s.rdfType();
                                    src.sparqlEndpoint = s.sparqlEndpoint();
                                    src.sparqlQuery = s.sparqlQuery();
                                    return src;
                                }
                        ));

        if (existingSources != null) {
            for (Forms.Autocomplete.Source existing : existingSources) {
                if (incomingByRdfType.containsKey(existing.rdfType)) {
                    mergedSources.add(incomingByRdfType.get(existing.rdfType));
                    incomingByRdfType.remove(existing.rdfType);
                } else {
                    mergedSources.add(existing);
                }
            }
        }

        mergedSources.addAll(incomingByRdfType.values());
        mergedSettings.forms.autocomplete.sources = mergedSources;

        return this;
    }
}
