package nl.healthri.fdp.uploadschema.requestbodies;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SchemaParms(boolean drafts,
                          @JsonProperty("abstract") boolean abstractSchema) {
}
