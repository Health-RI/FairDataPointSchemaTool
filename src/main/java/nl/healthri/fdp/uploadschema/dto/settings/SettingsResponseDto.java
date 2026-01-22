package nl.healthri.fdp.uploadschema.dto.settings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
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
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MetadataMetric(
            String metricUri,
            String resourceUri
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Ping(
            boolean enabled,
            List<String> endpoints,
            List<String> endpointsFromConfig,
            String interval
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Repository(
            String type
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Search(
            List<String> filters
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Forms(
            Autocomplete autocomplete
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Autocomplete(
                boolean searchNamespace,
                List<Source> sources
        ) {
            @JsonIgnoreProperties(ignoreUnknown = true)
            public record Source(
                    String rdfType,
                    String sparqlEndpoint,
                    String sparqlQuery
            ) {}
        }
    }

}

