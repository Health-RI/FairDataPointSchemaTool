package nl.healthri.fdp.uploadschema.dto.response.Schema;

import java.util.ArrayList;

public record SchemaDataResponse(
        String uuid,
        String name,
        Latest latest,
        Object draft,
        ArrayList<String> versions,
        ArrayList<String> extendSchemaUuids,
        ArrayList<String> childSchemaUuids) {

    public record Latest(
            String uuid,
            String version,
            String versionUuid,
            String previousVersionUuid,
            String name,
            boolean published,
            boolean abstractSchema,
            boolean latest,
            String type,
            String origin,
            String importedFrom,
            String definition,
            String description,
            ArrayList<String> targetClasses,
            ArrayList<String> extendsSchemaUuids,
            String suggestedResourceName,
            String suggestedUrlPrefix
    ) {

    }
}
