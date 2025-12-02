package nl.healthri.fdp.uploadschema.utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpResponse;


public class HttpRequestUtils {
    private static final Logger logger = LoggerFactory.getLogger(HttpRequestUtils.class);

    public static void handleResponseStatus(HttpResponse<String> response) throws IOException {
        String method = response.request().method();
        int statusCode = response.statusCode();
        URI uri = response.uri();

        switch (statusCode) {
            case 200 -> logger.info("[" + statusCode + "]" + " successfull request: " + method + " " + uri);
            case 400 -> throw new IllegalArgumentException("[" + statusCode + "]" + " bad request: " + method + " " + uri);
            case 401 -> throw new SecurityException("[" + statusCode + "]" + "Unauthorized: " + method + " "  + uri);
            case 403 -> throw new SecurityException("[" + statusCode + "]" + "Forbidden: " + method + " "  + uri);
            case 404 -> throw new IOException("[" + statusCode + "]" + "Resource Not Found: " + method + " "  + uri);
            case 500 -> throw new IOException("[" + statusCode + "]" + "Internal Server Error: " + method + " "  + uri);
            default -> throw new RuntimeException("[" + statusCode + "]" + "Unexpected HTTP status: " + method + " "  + uri);
        }
    }
}