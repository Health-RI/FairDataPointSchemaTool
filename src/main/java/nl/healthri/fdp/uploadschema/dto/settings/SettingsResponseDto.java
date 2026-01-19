package nl.healthri.fdp.uploadschema.dto.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.healthri.fdp.uploadschema.config.fdp.Settings;

import java.util.List;

public record SettingsResponseDto(
        String clientUrl,
        String persistentUrl,
        String appTitle,
        String appSubtitle,
        String appTitleFromConfig,
        String appSubtitleFromConfig,
        List<MetadataMetric> metadataMetrics,
        Ping ping,
        Repository repository,
        Search search,
        Forms forms
) {
    public record MetadataMetric(
            String metricUri,
            String resourceUri
    ) {}

    public record Ping(
            boolean enabled,
            List<String> endpoints,
            List<String> endpointsFromConfig,
            String interval
    ) {}

    public record Repository(
            String type
    ) {}

    public record Search(
            List<String> filters
    ) {}

    public record Forms(
            Autocomplete autocomplete
    ) {
        public record Autocomplete(
                boolean searchNamespace,
                List<Source> sources
        ) {
            public record Source(
                    String rdfType,
                    String sparqlEndpoint,
                    String sparqlQuery
            ) {}
        }
    }

}


