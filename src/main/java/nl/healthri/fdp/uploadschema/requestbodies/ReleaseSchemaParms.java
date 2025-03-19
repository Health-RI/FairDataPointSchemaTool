package nl.healthri.fdp.uploadschema.requestbodies;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.healthri.fdp.uploadschema.Version;

public record ReleaseSchemaParms(
        @JsonProperty("description")
        String resourceName,
        boolean published,
        String version,
        String major,
        String minor,
        String patch) {

    @JsonIgnore
    public static ReleaseSchemaParms of(String resourceName, boolean published, Version v) {
        return new ReleaseSchemaParms(resourceName, published, v.toString(), "" + v.major(), "" + v.minor(), "" + v.patch());
    }
}
