package transferdata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Properties {
    public String inputDir;
    public Map<String, List<String>> schemas = new HashMap<>();
    public String fdpUrl;
    public String fdpUsername;

    public Map<String, List<String>> parentChild = new HashMap<>();

    public List<String> resourcesToPublish;

    public static Properties load(File file) throws IOException {
        var mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(file, Properties.class);
    }

    //Running the main will create a Properties.yaml file, it will overwrite the files so manual edit will be lost!
    public static void main(String[] args) throws IOException {
        var p = new Properties();
        p.inputDir = "C:\\Users\\PatrickDekker(Health\\IdeaProjects\\health-ri-metadata\\Formalisation(shacl)\\Core\\PiecesShape\\";

        //target = output files, files: are the files that need to be merged.
        p.addFile("Catalog", "Catalog.ttl", "Agent.ttl", "Kind.ttl", "PeriodOfTime.ttl");
        p.addFile("Dataset", "Dataset.ttl", "Agent.ttl", "Kind.ttl", "PeriodOfTime.ttl");
        p.addFile("DatasetSeries", "DatasetSeries.ttl", "Agent.ttl", "Kind.ttl");
        p.addFile("Resource", "Resource.ttl");
        p.addFile("Distribution", "Distribution.ttl", "PeriodOfTime.ttl", "Checksum.ttl");
        p.addFile("Project", "Project.ttl", "Agent.ttl");
        p.addFile("Study", "Study.ttl");
        p.addFile("DataService", "DataService.ttl", "Agent.ttl", "Kind.ttl");

        p.addParent("Resource", "Dataset", "Catalog");
        p.fdpUrl = "http://localhost:80";
        p.fdpUsername = "albert.einstein@example.com";

        p.resourcesToPublish = List.of("Project", "Study");

        var mapper = new ObjectMapper(new YAMLFactory());
        mapper.writeValue(new File("Properties.yaml"), p);
    }

    public void addFile(String target, String... files) {
        schemas.put(target, List.of(files));
    }

    public void addParent(String parent, String... childeren) {
        parentChild.put(parent, List.of(childeren));
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
}

