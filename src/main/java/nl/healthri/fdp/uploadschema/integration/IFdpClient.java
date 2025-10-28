package nl.healthri.fdp.uploadschema.integration;

import nl.healthri.fdp.uploadschema.tasks.ResourceUpdateInsertTask;
import nl.healthri.fdp.uploadschema.tasks.ShapeUpdateInsertTask;
import nl.healthri.fdp.uploadschema.utils.ResourceMap;
import nl.healthri.fdp.uploadschema.utils.ShapesMap;
import nl.healthri.fdp.uploadschema.dto.response.Resource.ResourceResponse;

public interface IFdpClient {
    ShapesMap fetchSchemas();
    ResourceMap fetchResources();
    void insertResource(ResourceUpdateInsertTask task);
    void updateResource(ResourceUpdateInsertTask task);
    ResourceResponse fetchResource(String resourceId);
    void insertSchema(ShapeUpdateInsertTask task);
    void updateSchema(ShapeUpdateInsertTask task);
    void releaseSchema(ShapeUpdateInsertTask task);
}
