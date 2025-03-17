package nl.healthri.fdp.uploadschema.requestresponses;

import nl.healthri.fdp.uploadschema.requestbodies.ResourceParms;

import java.util.ArrayList;

public record ResourceEditResponse(String uuid,
                                   String name,
                                   String urlPrefix,
                                   ArrayList<String> metadataSchemaUuids,
                                   ArrayList<String> targetClassUris,
                                   ArrayList<ResourceParms.ResourceChild> children,
                                   ArrayList<ResourceParms.ResourceLink> externalLinks) {

    public record ResourceChild(String UUID) {
    }

    public record ResourceLink(String title, String propertyUri) {
    }
 
}
