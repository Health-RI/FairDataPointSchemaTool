package nl.healthri.fdp.uploadschema.utils;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class ObjectMap<T> {
    protected Map<String, T> map;

    public boolean isPresent(String name) {
        return map.containsKey(name);
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public Optional<T> getValue(String name) {
        return Optional.ofNullable(map.get(name));
    }
}
