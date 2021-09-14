package org.aksw.jena_sparql_api.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.function.Function;

import org.aksw.commons.util.reflect.MultiMethod;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.talis.rdfwriters.json.JSONJenaWriter;

/**
 * getFormatter(Text, Model).format(Obje)
 *
 * @author raven
 *
 */
public class SparqlFormatterUtils {
    public static final Logger logger = LoggerFactory.getLogger(SparqlFormatterUtils.class);

    public static Model triplesToModel(Iterator<Triple> iterator) {
        Model model = ModelFactory.createDefaultModel();

        while(iterator.hasNext()) {
            Triple triple = iterator.next();
            Statement stmt = org.apache.jena.sparql.util.ModelUtils.tripleToStatement(model, triple);
            if(stmt == null) {
                logger.warn("Invalid triple detected: " + triple);
                continue;
            }

            model.add(stmt);
        }

        return model;
    }


    public static final String FORMAT_XML = "Xml";
    public static final String FORMAT_Text = "Text";
    public static final String FORMAT_Turtle = "Turtle";

    public static final String FORMAT_RdfXml = "RdfXml";
    // public static final String FORMAT_ = "";
    public static final String FORMAT_Json = "Json";

    public static <I, O> Function<I, O> wrapMethod(final Method method) {
        return method == null ? null : new Function<I, O>() {
            @SuppressWarnings("unchecked")
            @Override
            public O apply(I input) {
                try {
                    return (O) method.invoke(null, input);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    public static Function<Model, String> getModelFormatter(String format) {
        Method method = MultiMethod.findMethodByParams(
                SparqlFormatterUtils.class, "_format" + format, Model.class);
        return wrapMethod(method);
    }

    public static Function<Boolean, String> getBooleanFormatter(String format) {
        Method method = MultiMethod.findMethodByParams(
                SparqlFormatterUtils.class, "_format" + format, Boolean.class);
        return wrapMethod(method);
    }

    public static Function<ResultSet, String> getResultSetFormatter(
            String format) {
        Method method = MultiMethod
                .findMethodByParams(SparqlFormatterUtils.class, "_format"
                        + format, ResultSet.class);
        return wrapMethod(method);
    }

    public static <T> Writer<T> wrapAsWriter(final Method method) {
        return method == null ? null : new Writer<T>() {
            @Override
            public void write(OutputStream out, T obj) {
                try {
                    method.invoke(null, out, obj);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    public static Writer<Iterator<Triple>> getTripleWriter(String format) {
        Method method = MultiMethod.findMethodByParams(
                SparqlFormatterUtils.class, "write" + format,
                OutputStream.class, Iterator.class);
        return wrapAsWriter(method);
    }

    public static Writer<Model> getModelWriter(String format) {
        Method method = MultiMethod.findMethodByParams(
                SparqlFormatterUtils.class, "write" + format,
                OutputStream.class, Model.class);
        return wrapAsWriter(method);
    }

    public static Writer<Boolean> getBooleanWriter(String format) {
        Method method = MultiMethod.findMethodByParams(
                SparqlFormatterUtils.class, "write" + format,
                OutputStream.class, Boolean.class);
        return wrapAsWriter(method);
    }

    public static Writer<ResultSet> getResultSetWriter(String format) {
        Method method = MultiMethod.findMethodByParams(
                SparqlFormatterUtils.class, "write" + format,
                OutputStream.class, ResultSet.class);
        return wrapAsWriter(method);
    }

    /*************************************************************************
     * XML
     *************************************************************************/

    // String

//	public static String formatXml(Object o) {
//		return MultiMethod.invokeStatic(HttpSparqlEndpoint.class, "_formatXml", o);
//	}

    public static String _formatXml(Boolean value) {
        String result = "<sparql xmlns='http://www.w3.org/2005/sparql-results#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.w3.org/2001/sw/DataAccess/rf1/result2.xsd'>\n"
                + "<head></head>\n"
                + "<boolean>"
                + value
                + "</boolean>"
                + "</sparql>";

        return result;
    }

    public static String _formatXml(Model model) {
        return _formatRdfXml(model);
    }

    public static String _formatRdfXml(Model model) {
        return ModelUtils.toString(model, "RDF/XML");
    }

    public static String _formatXml(ResultSet rs) {
        return ResultSetFormatter.asXMLString(rs);
    }

    // OutputStream
//	public static String _writeXml(OutputStream out, Object o) {
//		return MultiMethod.invokeStatic(HttpSparqlEndpoint.class, "writeXml",
//				out, o);
//	}

    /**
     *
     * TODO Add streaming support
     *
     * @param out
     * @param iterator
     */
    public static void writeXml(OutputStream out, Iterator<Triple> iterator) {

        Model model = triplesToModel(iterator);

        writeXml(out, model);
    }

    public static void writeXml(OutputStream out, Model model) {
        writeRdfXml(out, model);
    }


    public static void writeRdfXml(OutputStream out, Iterator<Triple> iterator) {

        Model model = triplesToModel(iterator);

        writeXml(out, model);
    }


    public static void writeRdfXml(OutputStream out, Model model) {
        model.write(out, "RDF/XML");
    }

    public static void writeRdfXml(OutputStream out, ResultSet rs) {
        ResultSetFormatter.outputAsXML(out, rs);
    }

    public static void writeXml(OutputStream out, ResultSet rs) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsXML(baos, rs);

        String str = baos.toString();

        PrintStream ps = new PrintStream(out);
        ps.println(str);

        // TODO Hack: Writing from Jena directly seems to block for same reason

        //ResultSetFormatter.outputAsXML(out, rs);
    }

    public static void writeXml(OutputStream out, Boolean value) throws IOException {
        ResultSetFormatter.outputAsXML(out, value);
        //out.flush();
    }

    /*************************************************************************
     * Text
     *************************************************************************/

//	public static String formatText(Object o) {
//		return MultiMethod.invokeStatic(HttpSparqlEndpoint.class,
//				"_formatText", o);
//	}

    public static String _formatText(Boolean value) {
        String result = "<sparql xmlns='http://www.w3.org/2005/sparql-results#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.w3.org/2001/sw/DataAccess/rf1/result2.xsd'>\n"
                + "<head></head>\n"
                + "<boolean>"
                + value
                + "</boolean>"
                + "</sparql>";

        return result;
    }

    public static String _formatText(Model model) {
        return ModelUtils.toString(model, "N-TRIPLES");
    }

    public static String _formatText(ResultSet rs) {
        return ResultSetFormatter.asText(rs);
    }

    public static void writeText(OutputStream out, Model model) {
        model.write(out, "N-TRIPLES");
    }

    public static void writeText(OutputStream out, Iterator<Triple> it) {
        StreamRDF sink = StreamRDFLib.writer(out);
        sink.start();
        while(it.hasNext()) {
            Triple triple = it.next();
            sink.triple(triple);
        }
        sink.finish();
        IO.flush(out);
        //sink.close();
    }


    public static void writeText(OutputStream out, ResultSet rs) {
        ResultSetFormatter.outputAsTSV(out, rs);
    }

    /*************************************************************************
     * JSON
     *************************************************************************/

    // String

//	public static String formatJson(Object o) {
//		return MultiMethod.invokeStatic(HttpSparqlEndpoint.class,
//				"_formatJson", o);
//	}

    public static String _formatJson(Boolean value) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeJson(out, value);
        return out.toString();
    }

    public static String _formatJson(Model model) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeJson(out, model);
        return out.toString();
    }

    public static String _formatJson(ResultSet rs) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeJson(out, rs);
        return out.toString();
    }

    // OutputStream
//	public static String _writeJson(OutputStream out, Object o) {
//		return MultiMethod.invokeStatic(HttpSparqlEndpoint.class, "writeJson",
//				out, o);
//	}

    public static void writeJson(OutputStream out, Boolean value) {
        ResultSetFormatter.outputAsJSON(out, value);
    }

    public static void writeJson(OutputStream out, Iterator<Triple> triples) {
        Model model = triplesToModel(triples);

        JSONJenaWriter writer = new JSONJenaWriter();
        writer.write(model, out, null);
    }

    public static void writeJson(OutputStream out, Model model) {
        JSONJenaWriter writer = new JSONJenaWriter();
        writer.write(model, out, null);
    }

    public static void writeJson(OutputStream out, ResultSet rs) {
        ResultSetFormatter.outputAsJSON(out, rs);
    }

    /*************************************************************************
     * TURTLE
     *************************************************************************/

//	public static String formatTurtle(Object o) {
//		// throw new RuntimeException("JSON is not supported yet");
//		return MultiMethod.invokeStatic(HttpSparqlEndpoint.class,
//				"_formatTurtle", o);
//	}

    public static String _formatTurtle(Model model) {
        return ModelUtils.toString(model, "TTL");
    }

    public static void writeTurtle(OutputStream out, Iterator<Triple> triples) {
        Model model = triplesToModel(triples);

        writeTurtle(out, model);
    }

    public static void writeTurtle(OutputStream out, Model model) {
        model.write(out, "TTL");
    }
}
