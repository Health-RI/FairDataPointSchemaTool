package nl.healthri.fdp.uploadschema.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class Properties {
    public final Map<String, List<String>> schemas = new LinkedHashMap<>();
    public final Map<String, List<String>> parentChild = new LinkedHashMap<>();
    public final Map<String, ResourceProperties> resources = new HashMap<>();
    public String inputDir;
    public String outputDir;
    public String fdpUrl;
    public String fdpUsername;
    public List<String> schemasToPublish;
    public int schemaVersion;

    public static Properties load(File file) throws IOException {
        var mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(file, Properties.class);
    }

    //Running the main will create a Properties.yaml file, it will overwrite the files so manual edit will be lost!
    public static void main(String[] args) throws IOException {
        var p = new Properties();
        p.fdpUrl = "http://localhost:80";
        //password is set as cmd-line option.
        p.fdpUsername = "albert.einstein@example.com";

        p.inputDir = "C:\\Users\\PatrickDekker(Health\\IdeaProjects\\health-ri-metadata\\Formalisation(shacl)\\Core\\PiecesShape\\";
        p.outputDir = "C:\\Users\\PatrickDekker(Health\\IdeaProjects\\health-ri-metadata\\Formalisation(shacl)\\Core\\FairDataPointShape";

        //target = Schema name in the FDP, files: are the files that need to be merged.
        p.addFile("Catalog", "Catalog.ttl", "Agent.ttl", "Kind.ttl", "PeriodOfTime.ttl");
        p.addFile("Dataset", "Dataset.ttl", "Agent.ttl", "Kind.ttl", "PeriodOfTime.ttl");
        p.addFile("Dataset Series", "DatasetSeries.ttl", "Agent.ttl", "Kind.ttl");
        p.addFile("Resource", "Resource.ttl");
        p.addFile("Distribution", "Distribution.ttl", "PeriodOfTime.ttl", "Checksum.ttl");
        p.addFile("Project", "Project.ttl", "Agent.ttl");
        p.addFile("Study", "Study.ttl");
        p.addFile("Data Service", "DataService.ttl", "Agent.ttl", "Kind.ttl");

        //this defines the "extends" in the schema definition.
        p.addParent("Resource", "Dataset", "Catalog", "Data Service", "Project", "Study");

        //this is list schema to publish, Make sure Parents are places first in the list(!)
        p.schemasToPublish = List.of("Resource", "Catalog", "Dataset", "Dataset Series", "Distribution", "Data Service", "Project", "Study");
        p.addResourceDescription("Project", "FAIR Data Point", "http://foaf.project.com");

        p.schemaVersion = 2;

        var mapper = new ObjectMapper(new YAMLFactory());
        mapper.writeValue(new File("Properties.yaml"), p);
    }

    public void addFile(String target, String... files) {
        schemas.put(target, List.of(files));
    }

    public void addResourceDescription(String name, String parentResource, String parentLinkIRI) {
        resources.put(name, new ResourceProperties(parentResource, parentLinkIRI));
    }

    public void addParent(String parent, String... children) {
        parentChild.put(parent, List.of(children));
    }

    @JsonIgnore
    public Map<String, List<File>> getFiles() {
        Map<String, List<File>> files = new HashMap<>();

        for (var e : schemas.entrySet()) {
            var inputFiles = e.getValue().stream().map(f -> new File(inputDir, f)).toList();
            files.put(e.getKey(), inputFiles);
        }
        return files;
    }

    @JsonIgnore
    public Set<String> getParents(String child) {
        return parentChild.entrySet().stream()
                .filter(e -> e.getValue().contains(child))
                .map(Map.Entry::getKey).collect(Collectors.toSet());
    }

    public record ResourceProperties(
            String parentResource,
            String parentRelationIri) {
    }
}

