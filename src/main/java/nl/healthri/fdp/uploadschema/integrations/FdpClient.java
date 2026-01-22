package nl.healthri.fdp.uploadschema.integrations;

import com.fasterxml.jackson.databind.ObjectMapper;

import nl.healthri.fdp.uploadschema.domain.ResourceTask;
import nl.healthri.fdp.uploadschema.domain.ShapeTask;
import nl.healthri.fdp.uploadschema.dto.resource.ResourceRequestDto;
import nl.healthri.fdp.uploadschema.dto.schema.ReleaseSchemaRequestDto;
import nl.healthri.fdp.uploadschema.dto.schema.UpdateSchemaRequestDto;
import nl.healthri.fdp.uploadschema.dto.settings.SettingsRequestDto;
import nl.healthri.fdp.uploadschema.dto.settings.SettingsResponseDto;
import nl.healthri.fdp.uploadschema.dto.auth.LoginRequestDto;
import nl.healthri.fdp.uploadschema.dto.resource.ResourceResponseDto;
import nl.healthri.fdp.uploadschema.dto.schema.SchemaDataResponseDto;
import nl.healthri.fdp.uploadschema.dto.auth.LoginResponseDto;
import nl.healthri.fdp.uploadschema.integrations.exceptions.FdpClientException;
import nl.healthri.fdp.uploadschema.utils.HttpRequestUtils;

import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

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

    public void setAuthToken(LoginResponseDto loginResponse) {
        this.authToken = loginResponse.asHeaderString();
    }

    private void isAuthenticated() {
        if (this.authToken == null || this.authToken.isBlank()) {
            throw new IllegalStateException("FdpClient is not authenticated, authorization token is null or empty.");
        }
    }

    public LoginResponseDto getAuthToken(LoginRequestDto loginRequest) {
        logger.info("Connecting to FDP at {} as {} ", hostname, loginRequest.email());

        try {
            URI uri = new URI(this.hostname + "/tokens");

            HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(
                    this.objectMapper.writeValueAsString(loginRequest)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(uri)
                    .header(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString())
                    .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                    .build();


            // Sends request
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

            // Handle each response based on Fair Data Point (FDP) Swagger documentation.
            HttpRequestUtils.handleResponseStatus(response);

            // Maps response body to object
            return this.objectMapper.readValue(response.body(), LoginResponseDto.class);

        } catch (IOException | URISyntaxException e) {
            throw new FdpClientException("Failed to reach FDP during authentication", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FdpClientException("Authentication process was interrupted", e);
        }
    }

    public List<SchemaDataResponseDto> fetchSchemas() {
        logger.info("Fetching metadata schemas from FDP");

        try {
            isAuthenticated();

            URI uri = new URI(this.hostname + "/metadata-schemas");

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .header(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString())
                    .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                    .header(HttpHeaders.AUTHORIZATION, this.authToken)
                    .build();

            // Sends request created through the client
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

            // Handle each response based on Fair Data Point (FDP) Swagger documentation.
            HttpRequestUtils.handleResponseStatus(response);

            // Maps response body to object
            return List.of(objectMapper.readValue(response.body(), SchemaDataResponseDto[].class));

        } catch (IOException | URISyntaxException e) {
            throw new FdpClientException("Failed to reach FDP while fetching schemas", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FdpClientException("Request to fetch schemas was interrupted", e);
        }
    }

    /**
     * @param task task, with info about the shape to create,
     *          when the shapes are created it will update this parameter by setting the UUID!
     */
    public ResourceResponseDto insertSchema(ShapeTask task, UpdateSchemaRequestDto updateSchemaRequest) {
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
                    .header(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString())
                    .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                    .header(HttpHeaders.AUTHORIZATION, this.authToken)
                    .build();

            // Sends request
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

            // Handle each response based on Fair Data Point (FDP) Swagger documentation.
            HttpRequestUtils.handleResponseStatus(response);

            // Maps response body to object
            return objectMapper.readValue(response.body(), ResourceResponseDto.class);
        } catch (IOException | URISyntaxException e) {
            throw new FdpClientException("Failed to reach FDP while inserting schema for " + task.shape, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FdpClientException("Schema insertion was interrupted", e);
        }
    }




    public void updateSchema(ShapeTask task, UpdateSchemaRequestDto updateSchemaRequest) {
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
                    .header(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString())
                    .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                    .header(HttpHeaders.AUTHORIZATION, this.authToken)
                    .build();

            // Sends request
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

            // Handle each response based on Fair Data Point (FDP) Swagger documentation.
            HttpRequestUtils.handleResponseStatus(response);
        } catch (IOException | URISyntaxException e) {
            throw new FdpClientException("Failed to reach FDP while updating schema for " + task.shape, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FdpClientException("Schema update was interrupted", e);
        }
    }

    public void releaseSchema(ShapeTask task, ReleaseSchemaRequestDto releaseSchemaRequest) {
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
                    .header(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString())
                    .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                    .header(HttpHeaders.AUTHORIZATION, this.authToken)
                    .build();

            // Sends request
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

            // Handle each response based on Fair Data Point (FDP) Swagger documentation.
            HttpRequestUtils.handleResponseStatus(response);

        } catch (IOException | URISyntaxException e) {
            throw new FdpClientException("Failed to reach FDP while releasing schema " + task.shape, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FdpClientException("Schema release was interrupted", e);
        }
    }

    public List<ResourceResponseDto> fetchResources() {
        logger.info("Fetching resources from fdp");

        try {
            isAuthenticated();

            URI uri = new URI(this.hostname + "/resource-definitions");

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .header(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString())
                    .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                    .header(HttpHeaders.AUTHORIZATION, this.authToken)
                    .build();

            // Sends request
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

            // Handle each response based on Fair Data Point (FDP) Swagger documentation.
            HttpRequestUtils.handleResponseStatus(response);

            // Map response to body
            return List.of(objectMapper.readValue(response.body(), ResourceResponseDto[].class));
        } catch (IOException | URISyntaxException e) {
            throw new FdpClientException("Failed to reach FDP while fetching resources", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FdpClientException("Resource fetch was interrupted", e);
        }
    }

    public ResourceResponseDto fetchResource(String resourceId){
        logger.info("fetching resource {} from FDP", resourceId);

        try {
            isAuthenticated();

            URI uri = new URI(this.hostname + "/resource-definitions/" + resourceId);

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .header(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString())
                    .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                    .header(HttpHeaders.AUTHORIZATION, this.authToken)
                    .build();

            // Sends request
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

            // Handle each response based on Fair Data Point (FDP) Swagger documentation.
            HttpRequestUtils.handleResponseStatus(response);

            // Maps response body to object
            return objectMapper.readValue(response.body(), ResourceResponseDto.class);
        } catch (IOException | URISyntaxException e) {
            throw new FdpClientException("Failed to reach FDP while fetching resource " + resourceId, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FdpClientException("Resource fetch was interrupted", e);
        }
    }

    public ResourceResponseDto insertResource(ResourceTask task, ResourceRequestDto resourceRequest) {
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
                    .header(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString())
                    .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                    .header(HttpHeaders.AUTHORIZATION, this.authToken)
                    .build();

            // Sends request
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

            // Handle each response based on Fair Data Point (FDP) Swagger documentation.
            HttpRequestUtils.handleResponseStatus(response);

            // Maps response body to object
            return objectMapper.readValue(response.body(), ResourceResponseDto.class);
        } catch (IOException | URISyntaxException e) {
            throw new FdpClientException("Failed to reach FDP while inserting resource " + task.resource, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FdpClientException("Resource insertion was interrupted", e);
        }
    }

    public void updateResource(ResourceTask task, ResourceResponseDto resourceResponse) {
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
                    .header(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString())
                    .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                    .header(HttpHeaders.AUTHORIZATION, this.authToken)
                    .build();

            // Sends request
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

            // Handle each response based on Fair Data Point (FDP) Swagger documentation.
            HttpRequestUtils.handleResponseStatus(response);
        } catch (IOException | URISyntaxException e) {
            throw new FdpClientException("Failed to reach FDP while updating resource " + task.resource, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FdpClientException("Resource update was interrupted", e);
        }
    }


    public SettingsResponseDto getSettings() {
        logger.info("getting settings from FDP");

        try {
            isAuthenticated();

            URI uri = new URI(this.hostname + "/settings");

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .header(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString())
                    .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                    .header(HttpHeaders.AUTHORIZATION, this.authToken)
                    .build();

            // Sends request
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

            // Handle each response based on Fair Data Point (FDP) Swagger documentation.
            HttpRequestUtils.handleResponseStatus(response);

            // Maps response body to object
            return objectMapper.readValue(response.body(), SettingsResponseDto.class);
        } catch (IOException | URISyntaxException e) {
            throw new FdpClientException("Failed to get FDP settings", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FdpClientException("Get settings was interrupted", e);
        }
    }

    public void updateSettings(SettingsRequestDto settingsRequestDto) {
        logger.info("updating settings in FDP");

        try {
            isAuthenticated();

            URI uri = new URI(this.hostname + "/settings");

            HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(
                    this.objectMapper.writeValueAsString(settingsRequestDto)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .PUT(body)
                    .uri(uri)
                    .header(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString())
                    .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                    .header(HttpHeaders.AUTHORIZATION, this.authToken)
                    .build();

            // Sends request
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

            // Handle each response based on Fair Data Point (FDP) Swagger documentation.
            HttpRequestUtils.handleResponseStatus(response);
        } catch (IOException | URISyntaxException e) {
            throw new FdpClientException("Failed to update settings", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FdpClientException("Update settings was interrupted", e);
        }
    }

}
