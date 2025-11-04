package nl.healthri.fdp.uploadschema.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import nl.healthri.fdp.uploadschema.domain.Version;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;


public class Properties {

    public final Map<String, List<String>> schemas = new LinkedHashMap<>();
    public final Map<String, List<String>> parentChild = new LinkedHashMap<>();
    public final Map<String, ResourceProperties> resources = new HashMap<>();
    public List<String> schemasToPublish;
    public String schemaVersion;
    public String inputDir;
    public String templateDir;
    public String outputRoot;
    public String piecesDir;
    public String fairDataPointDir;
    public String validationDir;

    public record ResourceProperties(
            String parentResource,
            String parentRelationIri,
            String schema) {
    }

    public static Properties load(File file) throws IOException {
        if (!file.exists() || !file.isFile()) {
            throw new FileNotFoundException("Properties file not found: " + file.getAbsolutePath());
        }

        try {
            var mapper = new ObjectMapper(new YAMLFactory());
            return mapper.readValue(file, Properties.class);
        } catch (StreamReadException e) {
            // Malformed YAML (e.g., bad indentation, syntax error)
            throw new IOException("Failed to read the YAML contents from file: " + file.getAbsolutePath(), e);
        } catch (DatabindException e) {
            // Valid YAML, but doesn't mattch the Properties structure
            throw new IOException("Failed to bind YAML content to Properties object from file: " + file.getAbsolutePath(), e);
        } catch (IOException e) {
            // Other IO issues
            throw new IOException("Error while reading properties from file: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * You can run this class to create a default Propertie.yaml. it will overwrite existing one!
     *
     * @param args arguments are ignored.
     * @throws IOException when properties can't be written.
     */
    public static void main(String[] args) throws IOException {
        var p = new Properties();
        p.inputDir = "https://raw.githubusercontent.com/Health-RI/health-ri-metadata/v2.0.0/Formalisation(shacl)/Core/PiecesShape/";
        //NOTE: extra / in front drive letter!
//        p.inputDir = "file:///C:/Users/PatrickDekker(Health/IdeaProjects/health-ri-metadata/Formalisation(shacl)/Core/PiecesShape/";
        p.templateDir = "C:\\Users\\PatrickDekker(Health\\";

        p.outputRoot = "C:\\Users\\PatrickDekker(Health\\IdeaProjects\\health-ri-metadata\\Formalisation(shacl)\\Core\\";
        p.fairDataPointDir = "FairDataPointShape";
        p.piecesDir = "PiecesShape";
        p.validationDir = "ValidationShape";

        //target = Schema name in the FDP, files: are the files that need to be merged.
        p.addFile("Catalog", "Catalog.ttl", "Agent.ttl", "Kind.ttl", "PeriodOfTime.ttl");
        p.addFile("Dataset", "Dataset.ttl", "Agent.ttl", "Kind.ttl", "PeriodOfTime.ttl");
        p.addFile("Dataset Series", "DatasetSeries.ttl", "Agent.ttl", "Kind.ttl");
        p.addFile("Resource", "Resource.ttl");
        p.addFile("Distribution", "Distribution.ttl", "PeriodOfTime.ttl", "Checksum.ttl");
        p.addFile("Data Service", "DataService.ttl", "Agent.ttl", "Kind.ttl");

        //this defines the "extends" in the schema definition.
        p.addParent("Resource", "Dataset", "Catalog", "Data Service");

        //this is list schema to publish, Make sure Parents are places first in the list(!)
        p.schemasToPublish = List.of("Resource", "Catalog", "Dataset", "Dataset Series", "Distribution", "Data Service");
        p.schemaVersion = "2.0.0";

        p.addResourceDescription("Dataset Series", "Dataset", "http://www.w3.org/ns/dcat#inSeries", "Dataset Series");
        p.addResourceDescription("Sample Distribution", "Dataset", "http://www.w3.org/ns/adms#sample", "Distribution");
        p.addResourceDescription("Analytics Distribution", "Dataset", "http://healthdataportal.eu/ns/health#analytics", "Distribution");

        var mapper = new ObjectMapper(new YAMLFactory());
        mapper.writeValue(new File("Properties.yaml"), p);
    }

    private URI uri(String file) {
        try {
            return new URI(inputDir + file);
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    @JsonIgnore
    public Version getVersion() {
        return new Version(schemaVersion);
    }

    @JsonIgnore
    public Path getPiecesDir() {
        return Path.of(outputRoot, piecesDir);
    }

    @JsonIgnore
    public Path getValidationDir() {
        return Path.of(outputRoot, validationDir);
    }

    @JsonIgnore
    public Path getFairDataPointDir() {
        return Path.of(outputRoot, fairDataPointDir);
    }

    public void addFile(String target, String... files) {
        schemas.put(target, List.of(files));
    }

    public void addResourceDescription(String name, String parentResource, String parentLinkIRI, String schema) {
        resources.put(name, new ResourceProperties(parentResource, parentLinkIRI, schema));
    }

    public void addParent(String parent, String... children) {
        parentChild.put(parent, List.of(children));
    }

    /**
     * @return a map where each key is a schema name and the corresponding value is a list of URIs representing the files associated with that schema.
     */
    @JsonIgnore
    public Map<String, List<URI>> getFiles() {
        return schemas.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream()
                                .map(this::uri)
                                .toList()
                ));
    }

    /**
     * @param dir, location where the shema files are located.
     * @return a map where each key is a schema name and the corresponding value is a list of URIs \
     * representing the files associated with that schema in the given directory.
     */
    @JsonIgnore
    public Map<String, List<URI>> getFiles(Path dir) {
        return schemas.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream()
                                .map(f -> dir.resolve(f).toFile().toURI())
                                .toList()
                ));
    }


    /**
     * @return a set with all files from the pieces folder
     */
    @JsonIgnore
    public Set<URI> getAllFiles() {
        return schemas.values().stream().flatMap(Collection::stream).map(f -> getPiecesDir().resolve(f).toFile().toURI()).collect(Collectors.toSet());
    }

    @JsonIgnore
    public Set<String> getParents(String child) {
        return parentChild.entrySet().stream()
                .filter(e -> e.getValue().contains(child))
                .map(Map.Entry::getKey).collect(Collectors.toSet());
    }
}

