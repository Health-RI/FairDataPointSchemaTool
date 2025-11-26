package nl.healthri.fdp.uploadschema.utils;

import nl.healthri.fdp.uploadschema.domain.Version;
import nl.healthri.fdp.uploadschema.dto.Schema.SchemaDataResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record SchemaInfo(Version version, String uuid, String definition) {
    public static Map<String, SchemaInfo> createSchemaInfoMap(List<SchemaDataResponse> schemaDataResponseList) {
        Map<String, SchemaInfo> schemaInfoMap = new HashMap<>();
        for (SchemaDataResponse schemaDataResponse : schemaDataResponseList) {
            Version version = new Version(schemaDataResponse.latest().version());

            SchemaInfo schemaInfo = new SchemaInfo(
                    version,
                    schemaDataResponse.uuid(),
                    schemaDataResponse.latest().definition()
            );

            schemaInfoMap.put(schemaDataResponse.name(), schemaInfo);
        }

        return schemaInfoMap;
    }
}
