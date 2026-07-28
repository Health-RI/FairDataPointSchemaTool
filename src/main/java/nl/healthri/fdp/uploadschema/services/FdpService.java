package nl.healthri.fdp.uploadschema.services;

import nl.healthri.fdp.uploadschema.config.fdp.Settings;
import nl.healthri.fdp.uploadschema.domain.ResourceTask;
import nl.healthri.fdp.uploadschema.domain.ShapeTask;
import nl.healthri.fdp.uploadschema.domain.Version;
import nl.healthri.fdp.uploadschema.dto.auth.LoginRequestDto;
import nl.healthri.fdp.uploadschema.dto.auth.LoginResponseDto;
import nl.healthri.fdp.uploadschema.dto.resource.ResourceRequestDto;
import nl.healthri.fdp.uploadschema.dto.resource.ResourceResponseDto;
import nl.healthri.fdp.uploadschema.dto.schema.ReleaseSchemaRequestDto;
import nl.healthri.fdp.uploadschema.dto.schema.SchemaDataResponseDto;
import nl.healthri.fdp.uploadschema.dto.schema.UpdateSchemaRequestDto;
import nl.healthri.fdp.uploadschema.dto.settings.SettingsRequestDto;
import nl.healthri.fdp.uploadschema.dto.settings.SettingsResponseDto;
import nl.healthri.fdp.uploadschema.integrations.FdpClientInterface;
import nl.healthri.fdp.uploadschema.integrations.exceptions.FdpClientException;
import nl.healthri.fdp.uploadschema.utils.SchemaInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nl.healthri.fdp.uploadschema.config.fdp.Settings.convertToEntity;

public class FdpService implements FdpServiceInterface {
    private final FdpClientInterface fdpClient;

    private static final Logger logger = LoggerFactory.getLogger(FdpService.class);

    public FdpService(FdpClientInterface fdpClient) {
        this.fdpClient = fdpClient;
    }

    public void authenticate(String username, String password) throws FdpClientException {
        LoginRequestDto loginRequest = new LoginRequestDto(username, password);
        LoginResponseDto loginResponse = fdpClient.getAuthToken(loginRequest);
        fdpClient.setAuthToken(loginResponse);
    }

    public List<SchemaDataResponseDto> getAllSchemas() throws FdpClientException{
        return fdpClient.fetchSchemas();
    }

    public void createSchema(ShapeTask task) throws FdpClientException {
            List<SchemaDataResponseDto> schemaDataResponseList = getAllSchemas();

            Map<String, SchemaInfo> schemaInfoMap = new HashMap<>();
            for(SchemaDataResponseDto schemaDataResponse : schemaDataResponseList) {
                Version version = new Version(schemaDataResponse.latest().version());
                SchemaInfo schemaInfo = new SchemaInfo(version, schemaDataResponse.uuid(), schemaDataResponse.latest().definition());
                schemaInfoMap.put(schemaDataResponse.name(), schemaInfo);
            }


            UpdateSchemaRequestDto updateSchemaRequest = new UpdateSchemaRequestDto(
                    task.shape,
                    task.description(), false,
                    task.model,
                    task.getParentUID(schemaInfoMap),
                    task.shape,
                    task.url());

            ResourceResponseDto resourceResponse = fdpClient.insertSchema(task, updateSchemaRequest);
            task.uuid = resourceResponse.uuid();
    }


    public void updateSchema(ShapeTask task) throws FdpClientException {
        List<SchemaDataResponseDto> schemaDataResponseList = getAllSchemas();

        Map<String, SchemaInfo> schemaInfoMap = new HashMap<>();
        for(SchemaDataResponseDto schemaDataResponse : schemaDataResponseList) {
            Version version = new Version(schemaDataResponse.latest().version());
            SchemaInfo schemaInfo = new SchemaInfo(version, schemaDataResponse.uuid(), schemaDataResponse.latest().definition());
            schemaInfoMap.put(schemaDataResponse.name(), schemaInfo);
        }

        UpdateSchemaRequestDto updateSchemaRequest = new UpdateSchemaRequestDto(
                task.shape,
                task.description(), false,
                task.model,
                task.getParentUID(schemaInfoMap),
                task.shape,
                task.url());

        fdpClient.updateSchema(task, updateSchemaRequest);
    }

    public void releaseSchema(ShapeTask task) throws FdpClientException{
        ReleaseSchemaRequestDto releaseSchemaRequest =  ReleaseSchemaRequestDto.of(task.shape, false, task.version);

        fdpClient.releaseSchema(task, releaseSchemaRequest);
    }

    public List<ResourceResponseDto> getAllResources() throws FdpClientException{
        return fdpClient.fetchResources();
    }

    public void createResource(ResourceTask task) throws FdpClientException{
        ResourceRequestDto resourceRequest = new ResourceRequestDto(
                task.resource,
                task.url(),
                new ArrayList<>(List.of(task.shapeUUUID)),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>());

        ResourceResponseDto resourceResponse = fdpClient.insertResource(task, resourceRequest);
        task.UUID = resourceResponse.uuid();
    }

    public void updateResource(ResourceTask task) throws FdpClientException{
        ResourceResponseDto resourceResponse = fdpClient.fetchResource(task.UUID);

        if (resourceResponse.children().stream().anyMatch(c -> c.resourceDefinitionUuid().equals(task.childUUuid))) {
            logger.info("resource {} already has link to child {}", resourceResponse.name(), task.childName);
        } else {
            //FIXME TagsURI is hardcoded..
            ResourceResponseDto.ListView listView =  new ResourceResponseDto.ListView(task.pluralName(), "http://www.w3.org/ns/dcat#themeTaxonomy", new ArrayList<>());
            ResourceResponseDto.Child child = new ResourceResponseDto.Child(task.childUUuid, task.childRelationIri, listView);
            resourceResponse.children().add(child);
        }

        fdpClient.updateResource(task, resourceResponse);
    }

    public void updateSettings(Settings newSettings){
        SettingsResponseDto fdpSettingsResponseDto = fdpClient.getSettings();

        Settings fdpSettings = convertToEntity(fdpSettingsResponseDto);
        Settings mergedSettings = fdpSettings.Merge(newSettings);

        SettingsRequestDto settingsRequestDto = SettingsRequestDto.convertToDto(mergedSettings);
        fdpClient.updateSettings(settingsRequestDto);
    }
}
