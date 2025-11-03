package nl.healthri.fdp.uploadschema.integration;

import nl.healthri.fdp.uploadschema.domain.ResourceTask;
import nl.healthri.fdp.uploadschema.domain.ShapeTask;
import nl.healthri.fdp.uploadschema.dto.request.Resource.ResourceRequest;
import nl.healthri.fdp.uploadschema.dto.request.Schema.ReleaseSchemaRequest;
import nl.healthri.fdp.uploadschema.dto.request.Schema.UpdateSchemaRequest;
import nl.healthri.fdp.uploadschema.dto.request.auth.LoginRequest;
import nl.healthri.fdp.uploadschema.dto.response.Schema.SchemaDataResponse;
import nl.healthri.fdp.uploadschema.dto.response.auth.LoginResponse;
import nl.healthri.fdp.uploadschema.dto.response.Resource.ResourceResponse;

import java.util.List;

public interface IFdpClient {
    void setAuthToken(LoginResponse loginResponse);
    LoginResponse getAuthToken(LoginRequest loginRequest);

    List<SchemaDataResponse> fetchSchemas();
    ResourceResponse insertSchema(ShapeTask task, UpdateSchemaRequest updateSchemaRequest);
    void updateSchema(ShapeTask task, UpdateSchemaRequest updateSchemaRequest);
    void releaseSchema(ShapeTask task, ReleaseSchemaRequest releaseSchemaRequest);

    List<ResourceResponse> fetchResources();
    ResourceResponse fetchResource(String resourceId);
    ResourceResponse insertResource(ResourceTask task, ResourceRequest resourceRequest );
    void updateResource(ResourceTask task, ResourceResponse resourceResponse);
}
