package nl.healthri.fdp.uploadschema.dto.schema;

import java.util.Set;

public record UpdateSchemaRequestDto(
        String name,
        String description,
        boolean abstractSchema,
        String definition,
        Set<String> extendsSchemaUuids,
        String suggestedResourceName,
        String suggestedUrlPrefix) {
}
