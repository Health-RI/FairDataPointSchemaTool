package nl.healthri.fdp.uploadschema.integration;

import nl.healthri.fdp.uploadschema.Version;
import nl.healthri.fdp.uploadschema.domain.ResourceTask;
import nl.healthri.fdp.uploadschema.domain.ShapeTask;
import nl.healthri.fdp.uploadschema.dto.request.Resource.ResourceRequest;
import nl.healthri.fdp.uploadschema.dto.request.Schema.ReleaseSchemaRequest;
import nl.healthri.fdp.uploadschema.dto.request.Schema.UpdateSchemaRequest;
import nl.healthri.fdp.uploadschema.dto.request.auth.LoginRequest;
import nl.healthri.fdp.uploadschema.dto.response.Schema.SchemaDataResponse;
import nl.healthri.fdp.uploadschema.dto.response.auth.LoginResponse;
import nl.healthri.fdp.uploadschema.utils.SchemaInfo;
import nl.healthri.fdp.uploadschema.dto.response.Resource.ResourceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class FdpService {
    private final FdpClientInterface fdpClient;

    private static final Logger logger = LoggerFactory.getLogger(FdpService.class);

    @Autowired
    public FdpService(FdpClientInterface fdpClient) {
        this.fdpClient = fdpClient;
    }

    public void authenticate(String username, String password){
        LoginRequest loginRequest = new LoginRequest(username, password);
        LoginResponse loginResponse = fdpClient.getAuthToken(loginRequest);

        fdpClient.setAuthToken(loginResponse);
    }

    public List<SchemaDataResponse> getAllSchemas() {
        return fdpClient.fetchSchemas();
    }

    public void createSchema(ShapeTask task){
        List<SchemaDataResponse> schemaDataResponseList = getAllSchemas();

        Map<String, SchemaInfo> schemaInfoMap = new HashMap<>();
        for(SchemaDataResponse schemaDataResponse : schemaDataResponseList) {
            Version version = new Version(schemaDataResponse.latest().version());
            SchemaInfo schemaInfo = new SchemaInfo(version, schemaDataResponse.uuid(), schemaDataResponse.latest().definition());
            schemaInfoMap.put(schemaDataResponse.name(), schemaInfo);
        }


        UpdateSchemaRequest updateSchemaRequest = new UpdateSchemaRequest(
                task.shape,
                task.description(), false,
                task.model,
                task.getParentUID(schemaInfoMap),
                task.shape,
                task.url());

        ResourceResponse resourceResponse = fdpClient.insertSchema(task, updateSchemaRequest);
        task.uuid = resourceResponse.uuid();
    }


    public void updateSchema(ShapeTask task){
        List<SchemaDataResponse> schemaDataResponseList = getAllSchemas();

        Map<String, SchemaInfo> schemaInfoMap = new HashMap<>();
        for(SchemaDataResponse schemaDataResponse : schemaDataResponseList) {
            Version version = new Version(schemaDataResponse.latest().version());
            SchemaInfo schemaInfo = new SchemaInfo(version, schemaDataResponse.uuid(), schemaDataResponse.latest().definition());
            schemaInfoMap.put(schemaDataResponse.name(), schemaInfo);
        }

        UpdateSchemaRequest updateSchemaRequest = new UpdateSchemaRequest(
                task.shape,
                task.description(), false,
                task.model,
                task.getParentUID(schemaInfoMap),
                task.shape,
                task.url());

        fdpClient.updateSchema(task, updateSchemaRequest);
    }

    public void releaseSchema(ShapeTask task){
        ReleaseSchemaRequest releaseSchemaRequest =  ReleaseSchemaRequest.of(task.shape, false, task.version);

        fdpClient.releaseSchema(task, releaseSchemaRequest);
    }

    public List<ResourceResponse> getAllResources() {
        return fdpClient.fetchResources();
    }

    public void createResource(ResourceTask task){
        ResourceRequest resourceRequest = new ResourceRequest(
                task.resource,
                task.url(),
                new ArrayList<>(List.of(task.shapeUUUID)),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>());

        ResourceResponse resourceResponse = fdpClient.insertResource(task, resourceRequest);
        task.UUID = resourceResponse.uuid();
    }

    public void updateResource(ResourceTask task){
        ResourceResponse resourceResponse = fdpClient.fetchResource(task.UUID);

        if (resourceResponse.children().stream().anyMatch(c -> c.resourceDefinitionUuid().equals(task.childUUuid))) {
            logger.info("resource {} already has link to child {}", resourceResponse.name(), task.childName);
        } else {
            //FIXME TagsURI is hardcoded..
            ResourceResponse.ListView listView =  new ResourceResponse.ListView(task.pluralName(), "http://www.w3.org/ns/dcat#themeTaxonomy", new ArrayList<>());
            ResourceResponse.Child child = new ResourceResponse.Child(task.childUUuid, task.childRelationIri, listView);
            resourceResponse.children().add(child);
        }

        fdpClient.updateResource(task, resourceResponse);
    }
}
