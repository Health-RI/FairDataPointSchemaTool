package nl.healthri.fdp.uploadschema.integration;

import jakarta.annotation.Resource;
import nl.healthri.fdp.uploadschema.dto.response.Schema.SchemaDataResponse;
import nl.healthri.fdp.uploadschema.tasks.ResourceUpdateInsertTask;
import nl.healthri.fdp.uploadschema.tasks.ShapeUpdateInsertTask;
import nl.healthri.fdp.uploadschema.dto.response.Resource.ResourceResponse;

import java.net.http.HttpRequest;

public interface IFdpClient {
    SchemaDataResponse[] fetchSchemas();
    ResourceResponse insertSchema(ShapeUpdateInsertTask task, HttpRequest.BodyPublisher body);
    void updateSchema(ShapeUpdateInsertTask task,  HttpRequest.BodyPublisher body);
    void releaseSchema(ShapeUpdateInsertTask task, HttpRequest.BodyPublisher body);

    ResourceResponse[] fetchResources();
    ResourceResponse fetchResource(String resourceId);
    ResourceResponse insertResource(ResourceUpdateInsertTask task, HttpRequest.BodyPublisher body );
    void updateResource(ResourceUpdateInsertTask task, HttpRequest.BodyPublisher body);
}
