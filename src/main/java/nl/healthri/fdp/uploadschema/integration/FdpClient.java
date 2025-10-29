package nl.healthri.fdp.uploadschema.integration;

import com.fasterxml.jackson.databind.ObjectMapper;

import nl.healthri.fdp.uploadschema.dto.request.Resource.ResourceRequest;
import nl.healthri.fdp.uploadschema.dto.request.Schema.ReleaseSchemaRequest;
import nl.healthri.fdp.uploadschema.dto.request.Schema.UpdateSchemaRequest;
import nl.healthri.fdp.uploadschema.dto.request.auth.LoginRequest;
import nl.healthri.fdp.uploadschema.dto.response.Resource.ResourceResponse;
import nl.healthri.fdp.uploadschema.dto.response.Schema.SchemaDataResponse;
import nl.healthri.fdp.uploadschema.dto.response.auth.LoginResponse;
import nl.healthri.fdp.uploadschema.tasks.ResourceUpdateInsertTask;
import nl.healthri.fdp.uploadschema.tasks.ShapeUpdateInsertTask;
import nl.healthri.fdp.uploadschema.utils.HttpRequestUtils;

import org.eclipse.rdf4j.query.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse;
import java.util.*;


// TODO: Throw client exception instead of Runtimeexception (otherwise you hide the error encountered)
// TODO: Split Fdp Client and Service into FdpSchemaClient, FdpResourceClient, etc.
// TODO:

@Component
public class FdpClient implements IFdpClient {
    private final HttpClient client;
    private final URI hostname;
    private final ObjectMapper objectMapper;
    private String authToken;

    private static final Logger logger = LoggerFactory.getLogger(FdpClient.class);

    public FdpClient(HttpClient client, URI hostname, ObjectMapper objectMapper) {
        this.client = Objects.requireNonNull(client, "HttpClient must not be null");
        this.hostname = Objects.requireNonNull(hostname, "URL must not be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "ObjectMapper must not be null");
    }

    public void setAuthToken(LoginResponse loginResponse) {
        this.authToken = loginResponse.asHeaderString();
    }

    private void isAuthenticated() {
        if (this.authToken == null || this.authToken.isBlank()) {
            throw new IllegalStateException("FdpClient is not authenticated, authorization token is null or empty.");
        }
    }

    public LoginResponse getAuthToken(LoginRequest loginRequest) {
        logger.info("Connecting to FDP at {} as {} ", hostname, loginRequest.email());

        try {
            URI uri = new URI(this.hostname + "/tokens");

            HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(
                    this.objectMapper.writeValueAsString(loginRequest)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(uri)
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .build();


            // Sends request
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

            // Handle each response based on Fair Data Point (FDP) Swagger documentation.
            HttpRequestUtils.handleResponseStatus(response);

            // Maps response body to object
            return this.objectMapper.readValue(response.body(), LoginResponse.class);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SchemaDataResponse[] fetchSchemas() {
        logger.info("Fetching metadata schemas from FDP");

        try {
            isAuthenticated();

            URI uri = new URI(this.hostname + "/metadata-schemas");

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", this.authToken)
                    .build();

            // Sends request created through the client
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

            // Handle each response based on Fair Data Point (FDP) Swagger documentation.
            HttpRequestUtils.handleResponseStatus(response);

            // Maps response body to object
            return objectMapper.readValue(response.body(), SchemaDataResponse[].class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param task task, with info about the shape to create,
     *          when the shapes are created it will update this parameter by setting the UUID!
     */
    public ResourceResponse insertSchema(ShapeUpdateInsertTask task, UpdateSchemaRequest updateSchemaRequest) {
        logger.info("Inserting {} schema into FDP", task.shape);

        try {
            isAuthenticated();

            URI uri = new URI(this.hostname + "/metadata-schemas");

            HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(
                    this.objectMapper.writeValueAsString(updateSchemaRequest)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(uri)
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", this.authToken)
                    .build();

            // Sends request
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

            // Handle each response based on Fair Data Point (FDP) Swagger documentation.
            HttpRequestUtils.handleResponseStatus(response);

            // Maps response body to object
            return objectMapper.readValue(response.body(), ResourceResponse.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }




    public void updateSchema(ShapeUpdateInsertTask task, UpdateSchemaRequest updateSchemaRequest) {
        logger.info("Updating shape {} in FDP", task.shape);

        try {
            isAuthenticated();

            URI uri = new URI(this.hostname + "/metadata-schemas/" + task.uuid + "/draft");

            HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(
                    this.objectMapper.writeValueAsString(updateSchemaRequest)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .PUT(body)
                    .uri(uri)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", this.authToken)
                    .build();

            // Sends request
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

            // Handle each response based on Fair Data Point (FDP) Swagger documentation.
            HttpRequestUtils.handleResponseStatus(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void releaseSchema(ShapeUpdateInsertTask task, ReleaseSchemaRequest releaseSchemaRequest) {
        logger.info("Releasing {} into FDP", task.shape);

        try {
            isAuthenticated();

            URI uri = new URI(this.hostname + "/metadata-schemas/" + task.uuid + "/versions");

            HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(
                    this.objectMapper.writeValueAsString(releaseSchemaRequest)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(uri)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", this.authToken)
                    .build();

            // Sends request
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

            // Handle each response based on Fair Data Point (FDP) Swagger documentation.
            HttpRequestUtils.handleResponseStatus(response);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ResourceResponse[] fetchResources() {
        logger.info("Fetching resources from fdp");

        try {
            isAuthenticated();

            URI uri = new URI(this.hostname + "/resource-definitions");

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", this.authToken)
                    .build();

            // Sends request
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

            // Handle each response based on Fair Data Point (FDP) Swagger documentation.
            HttpRequestUtils.handleResponseStatus(response);

            // Map response to body
            return objectMapper.readValue(response.body(), ResourceResponse[].class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ResourceResponse fetchResource(String resourceId){
        logger.info("fetching resource {} from FDP", resourceId);

        try {
            isAuthenticated();

            URI uri = new URI(this.hostname + "/resource-definitions/" + resourceId);

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", this.authToken)
                    .build();

            // Sends request
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

            // Handle each response based on Fair Data Point (FDP) Swagger documentation.
            HttpRequestUtils.handleResponseStatus(response);

            // Maps response body to object
            return objectMapper.readValue(response.body(), ResourceResponse.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ResourceResponse insertResource(ResourceUpdateInsertTask task, ResourceRequest resourceRequest) {
        logger.info("Inserting {} resources into FDP", task.resource);

        try {
            isAuthenticated();

            URI uri = new URI(this.hostname + "/resource-definitions");

            HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(
                    this.objectMapper.writeValueAsString(resourceRequest)
            );


            // Creates request
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(uri)
                    .header("accept", "application/json")
                    .header("content-type", "application/json")
                    .header("authorization", this.authToken)
                    .build();

            // Sends request
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

            // Handle each response based on Fair Data Point (FDP) Swagger documentation.
            HttpRequestUtils.handleResponseStatus(response);

            // Maps response body to object
            return objectMapper.readValue(response.body(), ResourceResponse.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateResource(ResourceUpdateInsertTask task, ResourceResponse resourceResponse) {
        logger.info("updating resource {} in FDP", task.resource);

        try {
            isAuthenticated();

            URI uri = new URI(this.hostname + "/resource-definitions/" + task.UUID);

            HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(
                    this.objectMapper.writeValueAsString(resourceResponse)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .PUT(body)
                    .uri(uri)
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", this.authToken)
                    .build();

            // Sends request
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

            // Handle each response based on Fair Data Point (FDP) Swagger documentation.
            HttpRequestUtils.handleResponseStatus(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
