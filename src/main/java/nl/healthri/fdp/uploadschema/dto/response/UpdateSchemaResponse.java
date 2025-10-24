package nl.healthri.fdp.uploadschema.dto.response;

import java.util.HashSet;

public record UpdateSchemaResponse(
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
