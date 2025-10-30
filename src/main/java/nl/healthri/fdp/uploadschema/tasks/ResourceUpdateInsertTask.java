package nl.healthri.fdp.uploadschema.tasks;

import nl.healthri.fdp.uploadschema.dto.response.Resource.ResourceResponse;
import nl.healthri.fdp.uploadschema.integration.FdpService;
import nl.healthri.fdp.uploadschema.utils.Properties;
import nl.healthri.fdp.uploadschema.utils.ResourceInfo;
import nl.healthri.fdp.uploadschema.utils.ResourceMap;
import nl.healthri.fdp.uploadschema.utils.SchemaInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ResourceUpdateInsertTask {

    private static final Logger logger = LoggerFactory.getLogger(ResourceUpdateInsertTask.class);

    public final String resource;
    public String UUID;
    public String shapeUUUID;
    public String childUUuid;
    public String childRelationIri;
    public String childName;
    boolean exists = false;

    public ResourceUpdateInsertTask(String resource) {
        this.resource = resource;
    }

    // todo: Map<Name, ResourceInfo>
    // todo: Map<Name, SchemaInfo>
    public static List<ResourceUpdateInsertTask> createParentTask(Properties p, FdpService fdpService) {

        List<ResourceResponse> resourceResponseList = fdpService.getAllResources();

        Map<String, ResourceInfo> resourceMap = new HashMap<>();
        for(ResourceResponse resourceResponse : resourceResponseList) {
            ResourceInfo resourceInfo = new ResourceInfo(resourceResponse.name(), resourceResponse.uuid());
            resourceMap.put(resourceResponse.name(), resourceInfo);
        }


        return p.resources.entrySet().stream().map(r -> {
            //now we to update the parent not the resource itself!
            var parentName = r.getValue().parentResource();
            var childName = r.getKey();
            var childIri = r.getValue().parentRelationIri();
            return new ResourceUpdateInsertTask(parentName)
                    .addExistingInfo(resourcesOnFdp) //adds uuid
                    .addChildInfo(childName, childIri, resourcesOnFdp);
        }).toList();
    }

    public static List<ResourceUpdateInsertTask> createTask(Properties p, FdpService fdpService) {
        var resourcesOnFdp = fdpService.getAllResources();
        var shapesOnFdp = fdpService.getAllSchemas();

        return p.resources.entrySet().stream().map(r -> new ResourceUpdateInsertTask(r.getKey())
                .addExistingInfo(resourcesOnFdp)
                .addShapeUUID(shapesOnFdp, r.getValue().schema())).toList();
    }

    public String pluralName() {
        //FIXME shape datasetSeries is already plural form.
        if (childName.toLowerCase().endsWith("ies")) return childName;

        // Rule 1: Words ending in consonant + "y" -> replace "y" with "ies"
        if (Pattern.matches(".*[^aeiou]y$", childName)) {
            return childName.replaceAll("y$", "ies");
        }
        // Rule 2: Words ending in "s", "x", "z", "ch", or "sh" -> add "es"
        else if (Pattern.matches(".*(s|x|z|ch|sh)$", childName)) {
            return childName + "es";
        }
        // Default rule: Just add "s"
        else {
            return childName + "s";
        }
    }

    public ResourceUpdateInsertTask addShapeUUID(SchemaInfo shapes, String schema) {
        String name = schema.isBlank() ? resource : schema;
        var shape = shapes.getUUID(name);
        shape.ifPresentOrElse(s -> shapeUUUID = s,
                () -> logger.error("Can't find shape: {} ", resource));
        return this;
    }

    public String url() {
        return resource.toLowerCase().replaceAll(" ", "");
    }

    public boolean isInsert() {
        return !exists;
    }

    public boolean hasChild() {
        return childUUuid != null;
    }

    public ResourceUpdateInsertTask addExistingInfo(ResourceMap resourceOnFdp) {
        var uuid = resourceOnFdp.getUUID(resource);
        exists = uuid.isPresent();
        uuid.ifPresentOrElse(u -> this.UUID = u,
                () -> logger.warn("update of resource is not supported yet"));
        return this;
    }

    public ResourceUpdateInsertTask addChildInfo(String name, String relationIri, ResourceMap resourceOnFdp) {
        var uuid = resourceOnFdp.getUUID(name);
        if (uuid.isPresent()) {
            childName = name;
            childRelationIri = relationIri;
            childUUuid = uuid.get();
        } else {
            logger.error("Child resource is not found {} ", name);
        }
        return this;
    }
}
