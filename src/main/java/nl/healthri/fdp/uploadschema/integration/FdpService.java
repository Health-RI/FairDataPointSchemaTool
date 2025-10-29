package nl.healthri.fdp.uploadschema.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.healthri.fdp.uploadschema.dto.request.Resource.ResourceRequest;
import nl.healthri.fdp.uploadschema.dto.request.Schema.ReleaseSchemaRequest;
import nl.healthri.fdp.uploadschema.dto.request.Schema.UpdateSchemaRequest;
import nl.healthri.fdp.uploadschema.dto.request.auth.LoginRequest;
import nl.healthri.fdp.uploadschema.dto.response.Schema.SchemaDataResponse;
import nl.healthri.fdp.uploadschema.dto.response.auth.LoginResponse;
import nl.healthri.fdp.uploadschema.tasks.ResourceUpdateInsertTask;
import nl.healthri.fdp.uploadschema.tasks.ShapeUpdateInsertTask;
import nl.healthri.fdp.uploadschema.utils.ResourceMap;
import nl.healthri.fdp.uploadschema.utils.ShapesMap;
import nl.healthri.fdp.uploadschema.dto.response.Resource.ResourceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.http.HttpRequest;
import java.util.*;


@Service
public class FdpService {
    private final IFdpClient fdpClient;

    private static final Logger logger = LoggerFactory.getLogger(FdpService.class);

    @Autowired
    public FdpService(IFdpClient fdpClient) {
        this.fdpClient = fdpClient;
    }

    public void authenticate(String username, String password){
        LoginRequest loginRequest = new LoginRequest(username, password);
        LoginResponse loginResponse = fdpClient.getAuthToken(loginRequest);

        fdpClient.setAuthToken(loginResponse);
    }

    public ShapesMap getAllSchemas() {
        SchemaDataResponse[] schemaDataResponseList = fdpClient.fetchSchemas();
        return new ShapesMap(schemaDataResponseList);
    }

    public void createSchema(ShapeUpdateInsertTask task){
        ShapesMap shapesMap = getAllSchemas();

        UpdateSchemaRequest updateSchemaRequest = new UpdateSchemaRequest(
                task.shape,
                task.description(), false,
                task.model,
                task.getParentUID(shapesMap),
                task.shape,
                task.url());

        ResourceResponse resourceResponse = fdpClient.insertSchema(task, updateSchemaRequest);
        task.uuid = resourceResponse.uuid();
    }


    public void updateSchema(ShapeUpdateInsertTask task){
        ShapesMap shapesMap = getAllSchemas();

        UpdateSchemaRequest updateSchemaRequest = new UpdateSchemaRequest(
                task.shape,
                task.description(), false,
                task.model,
                task.getParentUID(shapesMap),
                task.shape,
                task.url());

        fdpClient.updateSchema(task, updateSchemaRequest);
    }

    public void releaseSchema(ShapeUpdateInsertTask task){
        ReleaseSchemaRequest releaseSchemaRequest =  ReleaseSchemaRequest.of(task.shape, false, task.version);

        fdpClient.releaseSchema(task, releaseSchemaRequest);
    }

    public ResourceMap getAllResources() {
        ResourceResponse[] resourceResponseList = fdpClient.fetchResources();
        return new ResourceMap(resourceResponseList);
    }

    public ResourceResponse getResourceById(String resourceId) {
        return fdpClient.fetchResource(resourceId);
    }

    public void createResource(ResourceUpdateInsertTask task){
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

    public void updateResource(ResourceUpdateInsertTask task){
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
