package transferdata.requestbodies;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ReleaseSchemaParms(
        @JsonProperty("description")
        String resourceName,
        boolean published,
        String version) {
}
