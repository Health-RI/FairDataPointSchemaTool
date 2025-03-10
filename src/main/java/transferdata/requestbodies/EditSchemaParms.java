package transferdata.requestbodies;

import java.util.ArrayList;

public record EditSchema(
        String name,
        String description,
        boolean abstractSchema,
        String definition,
        ArrayList<String> extendsSchemaUuids,
        String suggestedResourceName,
        String suggestedUrlPrefix) {
}
