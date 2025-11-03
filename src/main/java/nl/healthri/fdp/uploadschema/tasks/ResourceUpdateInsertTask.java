package nl.healthri.fdp.uploadschema.tasks;

import nl.healthri.fdp.uploadschema.Version;
import nl.healthri.fdp.uploadschema.dto.response.Resource.ResourceResponse;
import nl.healthri.fdp.uploadschema.dto.response.Schema.SchemaDataResponse;
import nl.healthri.fdp.uploadschema.integration.FdpService;
import nl.healthri.fdp.uploadschema.utils.Properties;
import nl.healthri.fdp.uploadschema.utils.ResourceInfo;
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

        Map<String, ResourceInfo> fdpResourceMap = new HashMap<>();
        for(ResourceResponse resourceResponse : resourceResponseList) {
            ResourceInfo resourceInfo = new ResourceInfo(resourceResponse.name(), resourceResponse.uuid());
            fdpResourceMap.put(resourceResponse.name(), resourceInfo);
        }


        return p.resources.entrySet().stream().map(r -> {
            //now we to update the parent not the resource itself!
            var parentName = r.getValue().parentResource();
            var childName = r.getKey();
            var childIri = r.getValue().parentRelationIri();

            return new ResourceUpdateInsertTask(parentName)
                    .addExistingInfo(fdpResourceMap) //adds uuid
                    .addChildInfo(childName, childIri, fdpResourceMap);
        }).toList();
    }

    public ResourceUpdateInsertTask addExistingInfo(Map<String, ResourceInfo> fdpResourcesMap) {
        String uuid = fdpResourcesMap.get(this.resource).uuid();
        if(uuid == null || uuid.isEmpty()) {
            logger.warn("Can't find existing info for resource: {} ", this.resource);
        }

        this.exists = true;
        this.UUID = uuid;
        return this;
}

    public ResourceUpdateInsertTask addChildInfo(String name, String relationIri, Map<String, ResourceInfo> fdpResourcesMap) {
        String uuid = fdpResourcesMap.get(this.resource).uuid();
        if(uuid == null || uuid.isEmpty()) {
            logger.error("Child resource is not found {} ", name);
            return this;
        }

        this.childUUuid = uuid;
        this.childRelationIri = relationIri;
        this.childName = name;
        return this;
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



    public String url() {
        return resource.toLowerCase().replaceAll(" ", "");
    }

    public boolean isInsert() {
        return !exists;
    }

    public boolean hasChild() {
        return childUUuid != null;
    }

}
