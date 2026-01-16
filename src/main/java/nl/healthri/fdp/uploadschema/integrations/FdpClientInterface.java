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
import nl.healthri.fdp.uploadschema.integrations.exceptions.FdpClientException;

import java.util.List;

public interface FdpClientInterface {
    void setAuthToken(LoginResponse loginResponse);
    LoginResponse getAuthToken(LoginRequest loginRequest) throws FdpClientException;

    List<SchemaDataResponse> fetchSchemas() throws FdpClientException;
    ResourceResponse insertSchema(ShapeTask task, UpdateSchemaRequest updateSchemaRequest) throws FdpClientException;
    void updateSchema(ShapeTask task, UpdateSchemaRequest updateSchemaRequest) throws FdpClientException;
    void releaseSchema(ShapeTask task, ReleaseSchemaRequest releaseSchemaRequest) throws FdpClientException;

    List<ResourceResponse> fetchResources() throws FdpClientException;
    ResourceResponse fetchResource(String resourceId) throws FdpClientException;
    ResourceResponse insertResource(ResourceTask task, ResourceRequest resourceRequest) throws FdpClientException;
    void updateResource(ResourceTask task, ResourceResponse resourceResponse) throws FdpClientException;

    SettingsResponse getSettings();
    void updateSettings(Settings settings);
}
