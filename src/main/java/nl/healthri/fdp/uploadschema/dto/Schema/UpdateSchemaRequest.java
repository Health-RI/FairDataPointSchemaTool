package nl.healthri.fdp.uploadschema.dto.Schema;

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
