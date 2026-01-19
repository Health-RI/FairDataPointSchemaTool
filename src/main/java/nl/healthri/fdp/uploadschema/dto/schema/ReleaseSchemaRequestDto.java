package nl.healthri.fdp.uploadschema.dto.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.healthri.fdp.uploadschema.domain.Version;

public record ReleaseSchemaRequestDto(
        @JsonProperty("description")
        String resourceName,
        boolean published,
        String version,
        String major,
        String minor,
        String patch) {

    @JsonIgnore
    public static ReleaseSchemaRequestDto of(String resourceName, boolean published, Version v) {
        return new ReleaseSchemaRequestDto(resourceName, published, v.toString(), "" + v.major(), "" + v.minor(), "" + v.patch());
    }
}
