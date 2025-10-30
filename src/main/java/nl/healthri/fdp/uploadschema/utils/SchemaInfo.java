package nl.healthri.fdp.uploadschema.utils;

import nl.healthri.fdp.uploadschema.Version;
import nl.healthri.fdp.uploadschema.dto.response.Schema.SchemaDataResponse;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public record SchemaInfo(Version version, String uuid) {
}