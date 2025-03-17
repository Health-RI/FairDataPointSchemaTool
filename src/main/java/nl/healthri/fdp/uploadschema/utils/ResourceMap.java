package nl.healthri.fdp.uploadschema.utils;

import nl.healthri.fdp.uploadschema.requestresponses.ResourceResponse;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class ResourceMap extends ObjectMap<ResourceMap.ResourceInfo> {

    public ResourceMap(ResourceResponse[] resourceResponses) {
        map = Arrays.stream(resourceResponses).collect(Collectors.toMap(ResourceResponse::name, rr -> new ResourceInfo(rr.name(), rr.uuid())));
    }

    public Optional<String> getUUID(String name) {
        return getValue(name).map(ResourceInfo::uuid);
    }

    public record ResourceInfo(String name, String uuid) {
    }
}
