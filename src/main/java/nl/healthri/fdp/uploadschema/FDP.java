package nl.healthri.fdp.uploadschema;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.healthri.fdp.uploadschema.requestbodies.EditSchemaParms;
import nl.healthri.fdp.uploadschema.requestbodies.ReleaseSchemaParms;
import nl.healthri.fdp.uploadschema.requestbodies.SchemaParms;
import nl.healthri.fdp.uploadschema.requestbodies.loginParms;
import nl.healthri.fdp.uploadschema.requestresponses.SchemaDataResponse;
import nl.healthri.fdp.uploadschema.requestresponses.SchemaEdit;
import nl.healthri.fdp.uploadschema.requestresponses.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class FDP {
    private static final Logger logger = LoggerFactory.getLogger(FDP.class);

    private final String url;
    private final ObjectMapper mapper;
    private final HttpClient client;
    private TokenResponse token;

    private FDP(String url) {
        this.url = url;
        this.mapper = new ObjectMapper(new JsonFactory());
        this.client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.of(500, ChronoUnit.MILLIS))
                .build();
    }

    public static FDP connectToFdp(String url, String username, String password) {
        FDP fdp = new FDP(url);

        var mp = new loginParms(username, password);
        var token = fdp.request().setUri(url + "/tokens").setBody(mp).post(TokenResponse.class);

        logger.info("Token received: {}", token);
        fdp.setToken(token);
        return fdp;
    }

    private Set<String> getParentUID(Set<String> schema) {
        if (schema.isEmpty()) {
            return Collections.emptySet();
        }
        var map = fetchSchemaFromFDP();

        return schema.stream()
                .map(map::get)
                .filter(Objects::nonNull)
                .map(entity -> entity.uuid)
                .collect(Collectors.toSet());
    }

    private void setToken(TokenResponse token) {
        this.token = token;
    }

    private RequestBuilder request() {
        return new RequestBuilder(mapper, client);

    }

    public Map<String, SchemaInfo> fetchSchemaFromFDP() {
        var sp = new SchemaParms(false, true);
        SchemaDataResponse[] schemas = request().setUri(url + "/metadata-schemas", sp)
                .setBody(sp).setToken(token).get(SchemaDataResponse[].class);

        return Arrays.stream(schemas).collect(Collectors.toMap(SchemaDataResponse::name, SchemaInfo::new));
    }

    /**
     * @param t task, with info about the resource to create.
     * @return uuid of the newly created resource.
     */
    public String insertSchema(SchemaTools.Task t) {
        logger.info("Insert {} into the fdp", t.resource);
        EditSchemaParms esp = new EditSchemaParms(t.resource,
                t.description(), false,
                t.model, getParentUID(t.parents),
                t.resource,
                t.url());

        SchemaEdit se = request().setUri(url + "/metadata-schemas")
                .setBody(esp)
                .setToken(token)
                .post(SchemaEdit.class);
        return se.uuid();
    }

    /**
     * update exisiting resource on the fdp
     *
     * @param t task, with resource information
     * @return uuid, of the update resource, should be same as in resource information.
     */

    public String updateSchema(SchemaTools.Task t) {
        logger.info("Update {} into the fdp", t.resource);
        EditSchemaParms esp = new EditSchemaParms(t.resource,
                t.description(), false,
                t.model, getParentUID(t.parents),
                t.resource,
                t.url());

        SchemaEdit se = request().setUri(url + "/metadata-schemas/" + t.uuid + "/draft")
                .setBody(esp)
                .setToken(token)
                .put(SchemaEdit.class);
        return se.uuid();
    }

    public String releaseSchema(SchemaTools.Task t) {
        logger.info("Release {} into the fdp", t.resource);
        ReleaseSchemaParms rsp = ReleaseSchemaParms.of(t.resource,
                false, t.version);

        SchemaDataResponse se = request().setUri(url + "/metadata-schemas/" + t.uuid + "/versions")
                .setBody(rsp)
                .setToken(token)
                .post(SchemaDataResponse.class);
        return se.uuid();
    }

    public static class SchemaInfo {
        Version version;
        String uuid;

        private SchemaInfo(SchemaDataResponse sdr) {
            this.version = new Version(sdr.latest().version());
            this.uuid = sdr.latest().uuid();
        }

        @Override
        public String toString() {
            return "SchemaInfo{" +
                    "version=" + version +
                    ", uuid='" + uuid + '\'' +
                    '}';
        }
    }
}
