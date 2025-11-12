package nl.healthri.fdp.uploadschema.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import fr.sparna.rdf.xls2rdf.Xls2RdfConverter;
import fr.sparna.rdf.xls2rdf.write.RepositoryModelWriter;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.slf4j.LoggerFactory;

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
        //Disable logging during conversion
        var logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        var oldLevel = logger.getLevel();
        logger.setLevel(Level.WARN);
        var outputRepository = new SailRepository(new MemoryStore());
        outputRepository.init();

        try (Workbook workbook = WorkbookFactory.create(p.toFile());
             RepositoryConnection connection = outputRepository.getConnection();
             var baos = new ByteArrayOutputStream()) {

            var converter = new Xls2RdfConverter(new RepositoryModelWriter(outputRepository));
            converter.setSkipHidden(true);
            //the converter.procesFile, doesn't properly close the resource
            //so we use the workbook instead and take care of closing it.
            converter.processWorkbook(workbook); //converter.processFile, doesn't close resource properly
            connection.export(new TurtleWriter(baos));

            return baos.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            logger.setLevel(oldLevel);
        }
    }
}
