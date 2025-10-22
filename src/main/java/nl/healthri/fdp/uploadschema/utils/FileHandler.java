package nl.healthri.fdp.uploadschema.utils;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.*;
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

//this class handles RDF-File loading and saving this class should be
//injected in your class when you need it. Having it in a separate class
//simplifies testing.
public class FileHandler {
    private static final Logger logger = LoggerFactory.getLogger(FileHandler.class);

    public void safeModel(Path p, Model m) throws IOException {
        saveModelToStream(Files.newOutputStream(p), m);
    }

    private void saveModelToStream(OutputStream out, Model m) {
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


    public Model readFiles(List<URI> files) {
        logger.info("reading and parsing Shacl from {}", files.getFirst().toString());

        try {
            RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE);
            Model model = new LinkedHashModel();
            rdfParser.setRDFHandler(new StatementCollector(model));
            for (URI u : files) {
                readFile(u, rdfParser);
            }
            validateNamespaces(model);
            return model;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read SHACL files: " + files, e);
        }
    }


    private void readFile(URI uri, RDFParser parser) throws IOException {
        try {
            InputStream fis = getInputStream(uri);
            parser.parse(fis);
        } catch (IOException e) {
            throw new IOException("I/O error while reading the file: " + uri, e);
        } catch (RDFParseException e) {
            throw new IOException("Error parsing RDF file - invalid Turtle syntax: " + uri, e);
        } catch (RDFHandlerException e) {
            throw new IOException("Error while processing RDF content from file: " + uri, e);
        }
    }

    private InputStream getInputStream(URI uri) throws IOException {

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

    private void validateNamespaces(Model model) {
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


}
