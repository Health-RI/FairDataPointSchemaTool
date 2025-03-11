package nl.healthri.fdp.uploadschema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.healthri.fdp.uploadschema.requestresponses.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

class RequestBuilder {

    private static final Logger logger = LoggerFactory.getLogger(RequestBuilder.class);

    private final ObjectMapper mapper;
    private final HttpClient client;

    private String body = "";
    private Optional<String> bearer = Optional.empty();
    private URI uri = null;

    public RequestBuilder(ObjectMapper mapper, HttpClient client) {
        this.mapper = mapper;
        this.client = client;
    }

    public RequestBuilder setUri(String uri) {
        this.uri = URI.create(uri);
        return this;
    }

    public RequestBuilder setUri(String uri, Object parms) {
        //FIXME, this is ugly
        try {
            String json = mapper.writeValueAsString(parms);
            String p = "?" + json.substring(1, json.length() - 1)
                    .replaceAll(",", "&")
                    .replaceAll(":", "=")
                    .replaceAll("\"", "");
            this.uri = URI.create(uri + p);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return this;
    }


    public RequestBuilder setBody(Object obj) {
        try {
            this.body = mapper.writeValueAsString(obj);
            logger.trace("body: {}", body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public RequestBuilder setToken(TokenResponse token) {
        this.bearer = token == null ? Optional.empty() : Optional.of(token.asHeaderString());
        return this;
    }

    public <T> T post(Class<T> clazz) {
        var b = HttpRequest.newBuilder(uri)
                .POST(HttpRequest.BodyPublishers.ofString(body));
        return run(b, clazz);
    }

    public <T> T get(Class<T> clazz) {
        var b = HttpRequest.newBuilder(uri)
                .GET();
        return run(b, clazz);
    }

    public <T> T put(Class<T> clazz) {

        var b = HttpRequest.newBuilder(uri).PUT(HttpRequest.BodyPublishers.ofString(body));
        return run(b, clazz);
    }

    private <T> T run(HttpRequest.Builder builder, Class<T> clazz) {
        try {

            var b = builder
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json");
            var request = bearer.isPresent() ?
                    b.header("Authorization", bearer.get()).build()
                    : b.build();

            logger.debug("body: {}", body);
            logger.debug("url: {}", uri.toString());
            logger.info("request: {}", request);

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if ((response.statusCode() / 100) == 2) {
                logger.debug("request succes: {}", response.statusCode());
                return mapper.readValue(response.body(), clazz);
            }
            throw new RuntimeException("Invalid request: " + response.statusCode() + " -> " + response.body());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();//
        }
        return null; //you can't get here...
    }
}
