package nl.healthri.fdp.uploadschema.integrations;

import nl.healthri.fdp.uploadschema.config.fdp.Settings;
import nl.healthri.fdp.uploadschema.domain.ResourceTask;
import nl.healthri.fdp.uploadschema.domain.ShapeTask;
import nl.healthri.fdp.uploadschema.dto.Resource.ResourceRequest;
import nl.healthri.fdp.uploadschema.dto.Schema.ReleaseSchemaRequest;
import nl.healthri.fdp.uploadschema.dto.Schema.UpdateSchemaRequest;
import nl.healthri.fdp.uploadschema.dto.Settings.SettingsResponse;
import nl.healthri.fdp.uploadschema.dto.auth.LoginRequest;
import nl.healthri.fdp.uploadschema.dto.Schema.SchemaDataResponse;
import nl.healthri.fdp.uploadschema.dto.auth.LoginResponse;
import nl.healthri.fdp.uploadschema.dto.Resource.ResourceResponse;

import java.util.List;

public interface FdpClientInterface {
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

    SettingsResponse getSettings();
    void updateSettings(Settings settings);
}
