package nl.healthri.fdp.uploadschema;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.healthri.fdp.uploadschema.requestbodies.*;
import nl.healthri.fdp.uploadschema.requestresponses.*;
import nl.healthri.fdp.uploadschema.tasks.ResourceUpdateInsertTask;
import nl.healthri.fdp.uploadschema.tasks.ShapeUpdateInsertTask;
import nl.healthri.fdp.uploadschema.utils.RequestBuilder;
import nl.healthri.fdp.uploadschema.utils.ResourceMap;
import nl.healthri.fdp.uploadschema.utils.ShapesMap;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class FDP implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(FDP.class);

    private final String url;
    private final ObjectMapper mapper;
    private final HttpClient client;
    private TokenResponse token;

    private FDP(URI url) {
        this.url = url.toString();
        this.mapper = new ObjectMapper(new JsonFactory());
        this.client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.of(500, ChronoUnit.MILLIS))
                .build();
    }

    public static FDP connectToFdp(URI url, String username, String password) {
        logger.info("Connecting to FDP at {} as {} ", url, username);

        FDP fdp = new FDP(url);
        var info = fdp.request().setUri(fdp.url("actuator/info")).get(FDPInfoResponse.class);
        logger.info("FDP info: {}", info.toString());

        var mp = new loginParms(username, password);
        var token = fdp.request().setUri(fdp.url("tokens")).setBody(mp).post(TokenResponse.class);

        logger.info("Token received: {}", token);
        fdp.token = token;

        return fdp;
    }

    private String url(String path) {
        return MessageFormatter.format("{}/{}", url, path).getMessage();
    }

    private String url(String path, String uuid) {
        return MessageFormatter.arrayFormat("{}/{}/{}", new Object[]{url, path, uuid}).getMessage();
    }

    private Set<String> getParentUID(Set<String> shapes) {
        if (shapes.isEmpty()) {
            return Collections.emptySet();
        }
        var shapesOnFdp = fetchSchemaFromFDP();
        return shapes.stream()
                .map(shapesOnFdp::getUUID).flatMap(Optional::stream)
                .collect(Collectors.toSet());
    }

    public RequestBuilder request() {
        return new RequestBuilder(mapper, client);

    }

    public ShapesMap fetchSchemaFromFDP() {
        logger.info("Fetch schema info from fdp");
        var sp = new SchemaParms(false, true);
        SchemaDataResponse[] schemas = request().setUri(url + "/metadata-schemas", sp)
                .setBody(sp).setToken(token).get(SchemaDataResponse[].class);
        return new ShapesMap(schemas);
    }

    public ResourceMap fetchResourceFromFDP() {
        logger.info("Fetch resource info from fdp");
        ResourceResponse[] resources = request().setUri(url + "/resource-definitions")
                .setToken(token).get(ResourceResponse[].class);

        return new ResourceMap(resources);
    }

    public void insertResource(ResourceUpdateInsertTask task) {
        logger.info("Insert {} resource into the fdp", task.resource);
        ResourceParms RP = new ResourceParms(
                task.resource,
                task.url(),
                new ArrayList<>(List.of(task.shapeUUUID)),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>());
        ResourceResponse rer = request().setUri(url("resource-definitions"))
                .setBody(RP)
                .setToken(token).post(ResourceResponse.class);
        task.UUID = rer.uuid();
    }

    public void updateResource(ResourceUpdateInsertTask task) {
        logger.info("fetch resource {} from fdp", task.resource);
        var rr = request()
                .setToken(token)
                .setUri(url("resource-definitions", task.UUID))
                .get(ResourceResponse.class);

        if (rr.children().stream().anyMatch(c -> c.resourceDefinitionUuid().equals(task.childUUuid))) {
            logger.warn("resource {} already has link to child {}", rr.name(), task.childName);
        } else {
            //FIXME TagsURI is hardcoded..
            var child = new ResourceResponse.Child(task.childUUuid, task.childRelationIri,
                    new ResourceResponse.ListView(task.pluralName(), "http://www.w3.org/ns/dcat#themeTaxonomy", new ArrayList<>()));
            rr.children().add(child);
        }

        logger.info("update resource {} on the fdp", task.resource);
        request().setToken(token)
                .setUri(url("resource-definitions", task.UUID))
                .setBody(rr)
                .put(ResourceResponse.class);
    }

    /**
     * @param t task, with info about the shape to create,
     *          when the shapes are created it will update this parameter by setting the UUID!
     */
    public void insertSchema(ShapeUpdateInsertTask t) {
        logger.info("Insert {} shape into the fdp", t.shape);
        EditSchemaParms esp = new EditSchemaParms(t.shape,
                t.description(), false,
                t.model, getParentUID(t.parents),
                t.shape,
                t.url());

        SchemaEdit se = request().setUri(url + "/metadata-schemas")
                .setBody(esp)
                .setToken(token)
                .post(SchemaEdit.class);
        t.uuid = se.uuid();
    }

    /**
     * update exiting shape on the fdp
     *
     * @param t task, with shape information
     */

    public void updateSchema(ShapeUpdateInsertTask t) {
        logger.info("Update {} into the fdp", t.shape);
        EditSchemaParms esp = new EditSchemaParms(t.shape,
                t.description(), false,
                t.model, getParentUID(t.parents),
                t.shape,
                t.url());

        //result of request is not needed
        request().setUri(url + "/metadata-schemas/" + t.uuid + "/draft")
                .setBody(esp)
                .setToken(token)
                .put(SchemaEdit.class);
    }

    public void releaseSchema(ShapeUpdateInsertTask t) {
        logger.info("Release {} into the fdp", t.shape);
        ReleaseSchemaParms rsp = ReleaseSchemaParms.of(t.shape,
                false, t.version);
//      result of request is not needed.
        request().setUri(url + "/metadata-schemas/" + t.uuid + "/versions")
                .setBody(rsp)
                .setToken(token)
                .post(SchemaDataResponse.class);
    }


    public List<SchemaDataResponse> GetAllSchemas(){
        logger.info("Getting all metadata schemas");
        try {
            HttpClient client = HttpClient.newBuilder()
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(new URI(url + "/metadata-schemas/"))
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", String.valueOf(token))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Handle each response based on Fair Data Point (FDP) Swagger documentation.
            switch (response.statusCode()) {
                case 200 -> logger.info("Successfully received all schemas from FDP");
                case 400 ->
                        throw new IllegalArgumentException(String.valueOf(HttpStatus.SC_BAD_REQUEST));
                case 401 ->
                        throw new SecurityException(String.valueOf(HttpStatus.SC_UNAUTHORIZED));
                case 403 ->
                        throw new SecurityException(String.valueOf(HttpStatus.SC_FORBIDDEN));
                case 404 ->
                        throw new IOException(String.valueOf(HttpStatus.SC_NOT_FOUND));
                case 500 ->
                        throw new IOException(String.valueOf(HttpStatus.SC_INTERNAL_SERVER_ERROR));
                default -> {
                    throw new RuntimeException("Unexpected HTTP status: " + response.statusCode());
                }
            }

            ObjectMapper objectMapper = new ObjectMapper();
            TypeReference<List<SchemaDataResponse>> schemaDataTypeReference = new TypeReference<List<SchemaDataResponse>>(){};
            List<SchemaDataResponse> schemaDataResponseList = objectMapper.readValue(response.body(), schemaDataTypeReference);

            return schemaDataResponseList;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        if (client != null) {
            client.close();
        }
    }
}
