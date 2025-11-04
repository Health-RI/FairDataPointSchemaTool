package nl.healthri.fdp.uploadschema.tasks;

import java.io.IOException;

public interface SchemaToolServiceInterface {
    void createOrUpdateSchemas(boolean force) throws IOException;
    void convertTemplatesToShaclShapes() throws IOException;
    void mergeShapesToFdpSchemas() throws IOException;
    void mergeShapesForValidation() throws IOException;
    void addResourceDescriptions();
}
