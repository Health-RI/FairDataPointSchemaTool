package nl.healthri.fdp.uploadschema.dto.request;

import java.util.Set;

public record UpdateSchemaRequest(
        String name,
        String description,
        boolean abstractSchema,
        String definition,
        Set<String> extendsSchemaUuids,
        String suggestedResourceName,
        String suggestedUrlPrefix) {
}
