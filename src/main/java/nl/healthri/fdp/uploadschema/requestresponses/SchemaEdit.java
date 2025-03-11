package nl.healthri.fdp.uploadschema.requestresponses;

import java.util.HashSet;

public record SchemaEdit(
        String uuid,
        String name,
        String description,
        boolean abstractSchema,
        String definition,
        HashSet<String> extendsSchemaUuids,
        String suggestedResourceName,
        String suggestedUrlPrefix,
        String lastVersion
) {

}
