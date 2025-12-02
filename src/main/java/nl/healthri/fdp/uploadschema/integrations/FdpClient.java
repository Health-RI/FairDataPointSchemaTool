package nl.healthri.fdp.uploadschema.integrations;

import com.fasterxml.jackson.databind.ObjectMapper;

import nl.healthri.fdp.uploadschema.config.fdp.Settings;
import nl.healthri.fdp.uploadschema.domain.ResourceTask;
import nl.healthri.fdp.uploadschema.domain.ShapeTask;
import nl.healthri.fdp.uploadschema.dto.Resource.ResourceRequest;
import nl.healthri.fdp.uploadschema.dto.Schema.ReleaseSchemaRequest;
import nl.healthri.fdp.uploadschema.dto.Schema.UpdateSchemaRequest;
import nl.healthri.fdp.uploadschema.dto.Settings.SettingsResponse;
import nl.healthri.fdp.uploadschema.dto.auth.LoginRequest;
import nl.healthri.fdp.uploadschema.dto.Resource.ResourceResponse;
import nl.healthri.fdp.uploadschema.dto.Schema.SchemaDataResponse;
import nl.healthri.fdp.uploadschema.dto.auth.LoginResponse;
import nl.healthri.fdp.uploadschema.utils.HttpRequestUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;


// TODO: Throw client exception instead of Runtimeexception (otherwise you hide the error encountered)

@Component
public class FdpClient implements FdpClientInterface {
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

    public List<SchemaDataResponse> fetchSchemas() {
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
            return List.of(objectMapper.readValue(response.body(), SchemaDataResponse[].class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param task task, with info about the shape to create,
     *          when the shapes are created it will update this parameter by setting the UUID!
     */
    public ResourceResponse insertSchema(ShapeTask task, UpdateSchemaRequest updateSchemaRequest) {
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




    public void updateSchema(ShapeTask task, UpdateSchemaRequest updateSchemaRequest) {
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

    public void releaseSchema(ShapeTask task, ReleaseSchemaRequest releaseSchemaRequest) {
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

    public List<ResourceResponse> fetchResources() {
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
            return List.of(objectMapper.readValue(response.body(), ResourceResponse[].class));
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

    public ResourceResponse insertResource(ResourceTask task, ResourceRequest resourceRequest) {
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

    public void updateResource(ResourceTask task, ResourceResponse resourceResponse) {
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


    public SettingsResponse getSettings() {
        logger.info("getting settings from FDP");

        try {
            isAuthenticated();

            URI uri = new URI(this.hostname + "/settings");

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
            return objectMapper.readValue(response.body(), SettingsResponse.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateSettings(Settings settings) {
        logger.info("updating settings in FDP");

        try {
            isAuthenticated();

            URI uri = new URI(this.hostname + "/settings");

            HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(
                    this.objectMapper.writeValueAsString(settings)
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
