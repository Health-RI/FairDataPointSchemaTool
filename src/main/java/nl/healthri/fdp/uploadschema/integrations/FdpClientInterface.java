package nl.healthri.fdp.uploadschema.integrations;

import nl.healthri.fdp.uploadschema.domain.ResourceTask;
import nl.healthri.fdp.uploadschema.domain.ShapeTask;
import nl.healthri.fdp.uploadschema.dto.resource.ResourceRequestDto;
import nl.healthri.fdp.uploadschema.dto.schema.ReleaseSchemaRequestDto;
import nl.healthri.fdp.uploadschema.dto.schema.UpdateSchemaRequestDto;
import nl.healthri.fdp.uploadschema.dto.settings.SettingsRequestDto;
import nl.healthri.fdp.uploadschema.dto.settings.SettingsResponseDto;
import nl.healthri.fdp.uploadschema.dto.auth.LoginRequestDto;
import nl.healthri.fdp.uploadschema.dto.schema.SchemaDataResponseDto;
import nl.healthri.fdp.uploadschema.dto.auth.LoginResponseDto;
import nl.healthri.fdp.uploadschema.dto.resource.ResourceResponseDto;
import nl.healthri.fdp.uploadschema.integrations.exceptions.FdpClientException;

import java.util.List;

public interface FdpClientInterface {
    void setAuthToken(LoginResponseDto loginResponse);
    LoginResponseDto getAuthToken(LoginRequestDto loginRequest) throws FdpClientException;

    List<SchemaDataResponseDto> fetchSchemas() throws FdpClientException;
    ResourceResponseDto insertSchema(ShapeTask task, UpdateSchemaRequestDto updateSchemaRequest) throws FdpClientException;
    void updateSchema(ShapeTask task, UpdateSchemaRequestDto updateSchemaRequest) throws FdpClientException;
    void releaseSchema(ShapeTask task, ReleaseSchemaRequestDto releaseSchemaRequest) throws FdpClientException;

    List<ResourceResponseDto> fetchResources() throws FdpClientException;
    ResourceResponseDto fetchResource(String resourceId) throws FdpClientException;
    ResourceResponseDto insertResource(ResourceTask task, ResourceRequestDto resourceRequest) throws FdpClientException;
    void updateResource(ResourceTask task, ResourceResponseDto resourceResponse) throws FdpClientException;

    SettingsResponseDto getSettings() throws FdpClientException;
    void updateSettings(SettingsRequestDto settingsRequestDto) throws FdpClientException;
}
