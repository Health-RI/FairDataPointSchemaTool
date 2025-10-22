package nl.healthri.fdp.uploadschema.utils;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class RdfUtils {

    private RdfUtils() {
        //prevent instantiation.
    }

    public static Model fromTurtleString(String s) {
        try {
            return Rio.parse(new StringReader(s), RDFFormat.TURTLE);
        } catch (IOException ioe) {
            //will not happen because we are reading from string.
            throw new RuntimeException(ioe);
        }
    }

    public static String modelAsTurtleString(Model m) {
        StringWriter sw = new StringWriter();
        Rio.write(m, sw, RDFFormat.TURTLE);
        return sw.toString();
    }

    public static String schemaToFilename(String name) {
        return name.replaceAll(" ", "") + ".ttl";
    }
}
