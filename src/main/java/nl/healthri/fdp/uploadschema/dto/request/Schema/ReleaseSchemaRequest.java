package nl.healthri.fdp.uploadschema.dto.request.Schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.healthri.fdp.uploadschema.Version;

public record ReleaseSchemaRequest(
        @JsonProperty("description")
        String resourceName,
        boolean published,
        String version,
        String major,
        String minor,
        String patch) {

    @JsonIgnore
    public static ReleaseSchemaRequest of(String resourceName, boolean published, Version v) {
        return new ReleaseSchemaRequest(resourceName, published, v.toString(), "" + v.major(), "" + v.minor(), "" + v.patch());
    }
}
