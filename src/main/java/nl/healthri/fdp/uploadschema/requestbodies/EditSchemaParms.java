package nl.healthri.fdp.uploadschema.requestbodies;

import java.util.Set;

public record EditSchemaParms(
        String name,
        String description,
        boolean abstractSchema,
        String definition,
        Set<String> extendsSchemaUuids,
        String suggestedResourceName,
        String suggestedUrlPrefix) {
}
