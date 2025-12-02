package nl.healthri.fdp.uploadschema.dto.Resource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true) // Description, abstractschema, definition are ignored.
public record ResourceResponse(
        String uuid,
        String name,
        String urlPrefix,
        ArrayList<String> metadataSchemaUuids,
        ArrayList<String> targetClassUris,
        ArrayList<Child> children,
        ArrayList<ExternalLink> externalLinks
) {
    public record Child(
            String resourceDefinitionUuid,
            String relationUri,
            ListView listView) {
    }

    public record ExternalLink(
            String title,
            String propertyUri) {
    }

    public record ListView(
            String title,
            String tagsUri,
            ArrayList<Metadata> metadata) {
    }

    public record Metadata(
            String title,
            String propertyUri) {
    }
}
