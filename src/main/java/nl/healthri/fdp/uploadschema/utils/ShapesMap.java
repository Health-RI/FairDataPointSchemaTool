package nl.healthri.fdp.uploadschema.utils;

import nl.healthri.fdp.uploadschema.Version;
import nl.healthri.fdp.uploadschema.requestresponses.SchemaDataResponse;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class ShapesMap extends ObjectMap<ShapesMap.SchemaInfo> {

    public ShapesMap(SchemaDataResponse[] repsonses) {
        this.map = Arrays
                .stream(repsonses)
                .collect(Collectors.toMap(SchemaDataResponse::name, sr -> new SchemaInfo(new Version(sr.latest().version()), sr.uuid())));
    }

    public Optional<Version> getVersion(String name) {
        return getValue(name).map(SchemaInfo::version);
    }

    public Optional<String> getUUID(String name) {
        return getValue(name).map(SchemaInfo::uuid);
    }

    public record SchemaInfo(Version version, String uuid) {
    }
}
