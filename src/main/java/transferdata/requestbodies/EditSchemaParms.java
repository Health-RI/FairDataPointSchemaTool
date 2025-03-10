package transferdata.requestbodies;

import java.util.ArrayList;

public record EditSchemaParms(
        String name,
        String description,
        boolean abstractSchema,
        String definition,
        ArrayList<String> extendsSchemaUuids,
        String suggestedResourceName,
        String suggestedUrlPrefix) {
}
