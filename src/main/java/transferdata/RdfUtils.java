package transferdata;

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
import java.util.List;

public class RdfUtils {
    private static final Logger logger = LoggerFactory.getLogger(RdfUtils.class);

    private RdfUtils() {
        //prevents instantiation
    }

    private static void readFile(File f, RDFParser parser) throws IOException {
        logger.debug("reading {}", f.getName());
        FileInputStream fis = new FileInputStream(f);
        parser.parse(fis);
    }

    public static Model readFiles(List<File> files) throws IOException {
        RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE);
        Model model = new LinkedHashModel();
        rdfParser.setRDFHandler(new StatementCollector(model));
        for (File f : files) {
            readFile(f, rdfParser);
        }
        return model;
    }

    public static void printModelAsTurtle(Model m) {
        saveModelToStream(System.out, m);
    }

    public static String modelAsTurtleString(Model m) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        saveModelToStream(out, m);
        return out.toString();
    }

    public static void safeModel(File f, Model m) throws FileNotFoundException {
        saveModelToStream(new FileOutputStream(f), m);
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

}
