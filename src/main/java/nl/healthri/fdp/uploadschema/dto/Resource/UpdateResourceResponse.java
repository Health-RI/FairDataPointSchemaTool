package nl.healthri.fdp.uploadschema.dto.Resource;

import nl.healthri.fdp.uploadschema.dto.Resource.ResourceRequest;

import java.util.ArrayList;

public record UpdateResourceResponse(String uuid,
                                     String name,
                                     String urlPrefix,
                                     ArrayList<String> metadataSchemaUuids,
                                     ArrayList<String> targetClassUris,
                                     ArrayList<ResourceRequest.ResourceChild> children,
                                     ArrayList<ResourceRequest.ResourceLink> externalLinks) {

    public record ResourceChild(String UUID) {
    }

    public record ResourceLink(String title, String propertyUri) {
    }
 
}
