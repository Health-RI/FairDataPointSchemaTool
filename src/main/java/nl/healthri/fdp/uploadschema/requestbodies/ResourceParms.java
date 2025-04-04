package nl.healthri.fdp.uploadschema.requestbodies;

import java.util.ArrayList;

public record ResourceParms(String name,
                            String urlPrefix,
                            ArrayList<String> metadataSchemaUuids,
                            ArrayList<String> targetClassUris,
                            ArrayList<ResourceChild> children,
                            ArrayList<ResourceLink> externalLinks) {

    public record ResourceChild(String UUID) {
    }

    public record ResourceLink(String title, String propertyUri) {
    }
}
