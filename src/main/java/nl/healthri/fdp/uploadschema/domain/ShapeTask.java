package nl.healthri.fdp.uploadschema.domain;

import nl.healthri.fdp.uploadschema.Version;
import nl.healthri.fdp.uploadschema.domain.enums.ShapeStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;

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

        this.validate();
    }

    public void validate() {
        if (shape == null || shape.isEmpty()) {
            logger.error("Invalid: shape is required");
            return;
        }
        if (version == null) {
            logger.error("Invalid: version is required");
            return;
        }
        if (uuid == null || uuid.isEmpty()) {
            logger.error("Invalid: uuid is required");
            return;
        }
        if (model == null || model.isEmpty()) {
            logger.error("Invalid: model is required");
            return;
        }
        if (status == null) {
            logger.error("Invalid: status is required");
            return;
        }
        if (parents == null || parents.isEmpty()) {
            logger.error("Invalid: no parents defined for shape '{}'", shape);
        }
    }
}
