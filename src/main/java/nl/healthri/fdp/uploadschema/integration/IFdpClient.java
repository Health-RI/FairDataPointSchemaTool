package nl.healthri.fdp.uploadschema.integration;

import nl.healthri.fdp.uploadschema.dto.request.Resource.ResourceRequest;
import nl.healthri.fdp.uploadschema.dto.request.Schema.ReleaseSchemaRequest;
import nl.healthri.fdp.uploadschema.dto.request.Schema.UpdateSchemaRequest;
import nl.healthri.fdp.uploadschema.dto.request.auth.LoginRequest;
import nl.healthri.fdp.uploadschema.dto.response.Schema.SchemaDataResponse;
import nl.healthri.fdp.uploadschema.dto.response.auth.LoginResponse;
import nl.healthri.fdp.uploadschema.tasks.ResourceUpdateInsertTask;
import nl.healthri.fdp.uploadschema.tasks.ShapeUpdateInsertTask;
import nl.healthri.fdp.uploadschema.dto.response.Resource.ResourceResponse;

import java.net.http.HttpRequest;

public interface IFdpClient {
    void setAuthToken(LoginResponse loginResponse);
    LoginResponse getAuthToken(LoginRequest loginRequest);

    SchemaDataResponse[] fetchSchemas();
    ResourceResponse insertSchema(ShapeUpdateInsertTask task, UpdateSchemaRequest updateSchemaRequest);
    void updateSchema(ShapeUpdateInsertTask task, UpdateSchemaRequest updateSchemaRequest);
    void releaseSchema(ShapeUpdateInsertTask task, ReleaseSchemaRequest releaseSchemaRequest);

    ResourceResponse[] fetchResources();
    ResourceResponse fetchResource(String resourceId);
    ResourceResponse insertResource(ResourceUpdateInsertTask task, ResourceRequest resourceRequest );
    void updateResource(ResourceUpdateInsertTask task, ResourceResponse resourceResponse);
}
