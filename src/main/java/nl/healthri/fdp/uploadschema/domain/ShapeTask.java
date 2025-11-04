package nl.healthri.fdp.uploadschema.domain;

import nl.healthri.fdp.uploadschema.domain.enums.ShapeStatus;
import nl.healthri.fdp.uploadschema.utils.SchemaInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ShapeTask {
    public final String shape;
    public Version version;
    public String uuid;
    public Set<String> parents; // Names of parents for this schema
    public String model;
    public ShapeStatus status;

    private static final Logger logger = LoggerFactory.getLogger(ShapeTask.class);

    public ShapeTask(String shape, Version version, String uuid, Set<String> parents, String model, ShapeStatus status) {
        this.shape = shape;
        this.version = version;
        this.uuid = uuid;
        this.parents = parents;
        this.model = model;
        this.status = status;
    }

    public Set<String> getParentUID(Map<String, SchemaInfo> schemaMap) {
        if (this.parents.isEmpty()) {
            return Collections.emptySet();
        }

        return this.parents.stream()
                .map(schemaMap::get) // SchemaInfo
                .map(SchemaInfo::uuid) // SchemaInfo.UUID
                .collect(Collectors.toSet());
    }

    public String description() {
        return shape;
    }

    public String url() {
        return shape.toLowerCase().replaceAll(" ", "");
    }

    public ShapeStatus status() {
        return status;
    }
}
