package nl.healthri.fdp.uploadschema.domain;

import nl.healthri.fdp.uploadschema.tasks.ResourceUpdateInsertTask;
import nl.healthri.fdp.uploadschema.utils.ResourceInfo;
import nl.healthri.fdp.uploadschema.utils.SchemaInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.regex.Pattern;

public class ResourceTask {
    public final String resource;
    public String UUID;
    public String shapeUUUID;
    public String childUUuid;
    public String childRelationIri;
    public String childName;
    public boolean exists;

    private static final Logger logger = LoggerFactory.getLogger(ResourceTask.class);


    public ResourceTask(String resource, String uuid, String shapeUUUID) {
        this.resource = resource;
        this.UUID = uuid;
        this.shapeUUUID = shapeUUUID;
        this.exists = false;
    }

    public ResourceTask(String resource, String uuid, String shapeUUUID, String childUUUID, String childRelationIri, String childName) {
        this.resource = resource;
        this.UUID = uuid;
        this.shapeUUUID = shapeUUUID;
        this.childUUuid =  childUUUID;
        this.childRelationIri = childRelationIri;
        this.childName = childName;
        this.exists = false;
    }

    public void addChildInfo(String childUUuid, String childRelationIri, String childName) {
        this.childUUuid = childUUuid;
        this.childRelationIri = childRelationIri;
        this.childName = childName;
    }

    public void validate() {
        if (this.resource == null || this.resource.isEmpty()) {
            logger.error("Invalid: resource is required");
        }
        if (this.UUID == null || this.UUID.isEmpty()) {
            logger.error("Invalid: UUID is required");
        }
        if (this.shapeUUUID == null || this.shapeUUUID.isEmpty()) {
            logger.error("Invalid: shapeUUUID is required");
        }
        if (this.childUUuid != null && (this.childRelationIri == null || this.childRelationIri.isEmpty())) {
            logger.error("Ìnvalid: childRelationIri should be set if childUUuid is provided");
        }
        if (this.childRelationIri != null && (this.childUUuid == null || this.childUUuid.isEmpty())) {
            logger.error("Ìnvalid: childUUuid should be set if childRelationIri is provided");
        }
        if (this.childName != null && this.childName.isEmpty()) {
            logger.error("Ìnvalid: childName is empty");
        }
    }

    public String pluralName() {
        //FIXME shape datasetSeries is already plural form.
        if (this.childName.toLowerCase().endsWith("ies")) return this.childName;

        // Rule 1: Words ending in consonant + "y" -> replace "y" with "ies"
        if (Pattern.matches(".*[^aeiou]y$", this.childName)) {
            return this.childName.replaceAll("y$", "ies");
        }
        // Rule 2: Words ending in "s", "x", "z", "ch", or "sh" -> add "es"
        else if (Pattern.matches(".*(s|x|z|ch|sh)$", this.childName)) {
            return this.childName + "es";
        }
        // Default rule: Just add "s"
        else {
            return this.childName + "s";
        }
    }

    public void addShapeUUID(Map<String, SchemaInfo> fdpSchemaInfoMap, String schema) {
        String name = schema.isBlank() ? this.resource : schema;
        String fdpSchemaUUID = fdpSchemaInfoMap.get(name).uuid();

        if (fdpSchemaUUID == null || fdpSchemaUUID.isEmpty()) {
            logger.error("Can't find shape: {} ", this.resource);
        }

        this.shapeUUUID = fdpSchemaUUID;
    }

    public String url() {
        return this.resource.toLowerCase().replaceAll(" ", "");
    }

    public boolean isInsert() {
        return !this.exists;
    }

    public boolean hasChild() {
        return this.childUUuid != null;
    }

    public void addExistingInfo(Map<String, ResourceInfo> fdpResourcesMap) {
        String uuid = fdpResourcesMap.get(this.resource).uuid();
        if(uuid == null || uuid.isEmpty()) {
            logger.warn("Can't find existing info for resource: {} ", this.resource);
        }

        this.exists = true;
        this.UUID = uuid;
    }


}
