package transferdata.requestresponses;

import java.util.ArrayList;

public record SchemaEdit(
        String uuid,
        String name,
        String description,
        boolean abstractSchema,
        String definition,
        ArrayList<String> extendsSchemaUuids,
        String suggestedResourceName,
        String suggestedUrlPrefix,
        String lastVersion
) {

}
