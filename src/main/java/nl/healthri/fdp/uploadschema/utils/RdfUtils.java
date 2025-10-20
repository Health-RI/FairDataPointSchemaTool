package nl.healthri.fdp.uploadschema.utils;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RdfUtils {
    private static final Logger logger = LoggerFactory.getLogger(RdfUtils.class);

    private RdfUtils() {
        //prevents instantiation
    }

    private static void validateNamespaces(Model model) {
        // Collect all namespaces used in the model
        final Set<String> usedNamespaces = model.stream()
                .flatMap(st -> Set.of(st.getSubject(), st.getPredicate(), st.getObject()).stream())
                .filter(st -> st instanceof IRI)
                .map(st -> ((IRI) st).getNamespace())
                .collect(Collectors.toSet());
        // Remove unused namespaces
        Set<Namespace> namespacesToRemove = model.getNamespaces().stream()
                .filter(ns -> !usedNamespaces.contains(ns.getName()))
                .collect(Collectors.toSet());
        logger.info("Following namespace are unused: {}", namespacesToRemove);

        Set<String> prefixes = model.getNamespaces().stream().map(Namespace::getPrefix).collect(Collectors.toSet());
        if (prefixes.size() != model.getNamespaces().size()) {
            logger.warn("Duplicate prefixes found.");
        }
        Set<String> names = model.getNamespaces().stream().map(Namespace::getName).collect(Collectors.toSet());
        if (names.size() != model.getNamespaces().size()) {
            logger.warn("Duplicate namespace found.");
        }
    }

    private static void readFile(URI uri, RDFParser parser) throws IOException {
        logger.debug("reading {}", uri.getPath());
        try {
            InputStream fis = getInputStream(uri);
            parser.parse(fis);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static Model readFiles(List<URI> files) {
        try {
            logger.info("reading shacls from {}", files.getFirst().toString());
            RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE);
            Model model = new LinkedHashModel();
            rdfParser.setRDFHandler(new StatementCollector(model));
            for (URI u : files) {
                readFile(u, rdfParser);
            }
            validateNamespaces(model);
            return model;
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public static void printModelAsTurtle(Model m) {
        saveModelToStream(System.out, m);
    }

    public static String modelAsTurtleString(Model m) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        saveModelToStream(out, m);
        return out.toString();
    }

    public static void safeModel(Path p, Model m) throws IOException {
        saveModelToStream(Files.newOutputStream(p), m);
    }

    private static void saveModelToStream(OutputStream out, Model m) {
        RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, out);
        writer.startRDF();

        for (Namespace ns : m.getNamespaces()) {
            writer.handleNamespace(ns.getPrefix(), ns.getName());
        }
        for (Statement st : m) {
            writer.handleStatement(st);
        }
        writer.endRDF();
    }

    private static InputStream getInputStream(URI uri) throws IOException {

        if (List.of("http", "https").contains(uri.getScheme().toLowerCase())) {
            logger.trace("Fetch from github: {}", uri);
            try (HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).followRedirects(HttpClient.Redirect.NORMAL).build()) {
                HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() / 100 == 2) {
                    return new ByteArrayInputStream(response.body().getBytes());
                } else {
                    throw new IOException("Failed to fetch file: " + response.statusCode());
                }
            } catch (InterruptedException ie) {
                throw new RuntimeException(ie);
            }
        } else {
            return new FileInputStream(Paths.get(uri).toFile());
        }
    }

    public static String schemaToFile(String name) {
        return name.replaceAll(" ", "") + ".ttl";
    }
}
