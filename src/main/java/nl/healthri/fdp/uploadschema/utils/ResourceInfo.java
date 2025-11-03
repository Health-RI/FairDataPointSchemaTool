package nl.healthri.fdp.uploadschema.utils;

import nl.healthri.fdp.uploadschema.dto.response.Resource.ResourceResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ResourceInfo(String name, String uuid) {

    public static Map<String, ResourceInfo> createResourceInfoMap(List<ResourceResponse> resourceResponseList){
        Map<String, ResourceInfo> fdpResourceMap = new HashMap<>();
        for(ResourceResponse resourceResponse : resourceResponseList) {
            ResourceInfo resourceInfo = new ResourceInfo(resourceResponse.name(), resourceResponse.uuid());
            fdpResourceMap.put(resourceResponse.name(), resourceInfo);
        }

        return fdpResourceMap;
    }
}