package org.aksw.jena_sparql_api.rx.io.resultset;

import java.util.Collection;

import org.aksw.jena_sparql_api.rx.RDFLanguagesEx;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.resultset.ResultSetLang;

public class OutputFormatSpec {
	/* The output mode - triples, quads, bindings or json */
    protected OutputMode outputMode;

    /* The output language for rdf - may be described in more detail by the out*Format */
    protected Lang outRdfLang = null;

    /* The RDF format if applicable */
    protected RDFFormat outRdfFormat = null;

    /* TODO Add support for result set langs */
    // protected ResultSetLang outResultSetLang = null;

    
    public OutputFormatSpec(OutputMode outputMode, RDFFormat outRdfFormat, Lang outLang) {
        super();
        this.outputMode = outputMode;
        this.outRdfFormat = outRdfFormat;
        this.outRdfLang = outLang;
    }

    public OutputMode getOutputMode() {
        return outputMode;
    }

    public RDFFormat getOutRdfFormat() {
        return outRdfFormat;
    }

    public Lang getOutLang() {
        return outRdfLang;
    }

    /**
     * Determine the output format.
     * The 'outFormat' parameter forces a specific output format.
     * As a falback Analyze a given collection of statements and
     * 
     * @param outFormat
     * @param tripleFormat
     * @param quadFormat
     * @param stmts
     * @param jqMode
     * @return
     */
    public static OutputFormatSpec create(
            String outFormat,
            RDFFormat tripleFormat,
            RDFFormat quadFormat,
            Collection<? extends SparqlStmt> stmts,
            boolean jqMode
        ) {
        OutputMode outputMode;
        RDFFormat outRdfFormat = null;
        Lang outLang = null;

        if (outFormat != null) {
            if ("json".equalsIgnoreCase(outFormat)) {
                outputMode = OutputMode.JSON;
            } else {

                try {
                    outRdfFormat = RDFLanguagesEx.findRdfFormat(outFormat);
                    outLang = outRdfFormat.getLang();
                } catch (Exception e) {
                    outLang = RDFLanguagesEx.findLang(outFormat);
                }

                outputMode = OutputModes.determineOutputMode(outLang);
            }
        } else {
            outputMode = jqMode ? OutputMode.JSON : OutputModes.detectOutputMode(stmts);

            switch (outputMode) {
            case BINDING:
                outLang = ResultSetLang.SPARQLResultSetJSON;
                // outRdfFormat = new RDFFormat(outLang);
                break;
            case TRIPLE:
                outRdfFormat = tripleFormat;
                outLang = outRdfFormat.getLang();
                break;
            case QUAD:
                outRdfFormat = quadFormat;
                outLang = outRdfFormat.getLang();
                break;
            case JSON:
                // Nothing to do
                break;
            default:
                throw new IllegalStateException("Unknown output mode");
            }

        }

        return new OutputFormatSpec(outputMode, outRdfFormat, outLang);
    }

    /** Return the file extension that corresponds to the output language */
    public String getFileExtension() {
        String result;
        if (outputMode.equals(OutputMode.JSON)) {
            result = "json";
        } else {
            Lang lang;
            if (outRdfFormat != null) {
                lang = outRdfFormat.getLang();
            } else {
                lang = outRdfLang;
            }

            if (lang == null) {
                result = null;
            } else {
                result = Iterables.getFirst(lang.getFileExtensions(), null);
            }
        }

        return result;
    }
}