package nl.healthri.fdp.uploadschema;

import fr.sparna.rdf.xls2rdf.Xls2RdfConverter;
import fr.sparna.rdf.xls2rdf.write.RepositoryModelWriter;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class XlsToRdfUtils {

    private static final String PREFIX = "SHACL-";
    private static final String EXTENSION = ".xlsx";

    private static final Predicate<Path> fileNameFilter = (path) -> {
        String f = path.getFileName().toString();
        return f.startsWith(PREFIX) && f.toLowerCase().endsWith(EXTENSION);
    };

    private XlsToRdfUtils() {
    }

    public static Map<String, Path> getTemplateFiles(String path) throws IOException {
        Path p = Path.of(path);
        try (var s = Files.list(p)) {
            return s.filter(fileNameFilter).collect(Collectors.toMap(XlsToRdfUtils::getSchemaName, v -> v));
        }
    }

    private static String getSchemaName(Path p) {
        return p.getFileName().toString().substring(PREFIX.length(), p.getFileName().toString().length() - EXTENSION.length());
    }

    public static String createShacl(Path p) {
        var outputRepository = new SailRepository(new MemoryStore());
        outputRepository.init();

        var converter = new Xls2RdfConverter(new RepositoryModelWriter(outputRepository));
        converter.setSkipHidden(true);
        converter.processFile(p.toFile());
        try (RepositoryConnection connection = outputRepository.getConnection();
             var baos = new ByteArrayOutputStream()) {
            connection.export(new TurtleWriter(baos));
            return baos.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
