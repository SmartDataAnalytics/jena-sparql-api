package org.aksw.jena_sparql_api.path.datatype;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathParser;
import org.apache.jena.sparql.path.PathWriter;

/**
 * A datatype for storing SPARQL property paths in RDF literals.
 *
 * @author Claus Stadler
 *
 */
public class RDFDatatypePath
    extends BaseDatatype
{
    public static final String IRI = "http://jsa.aksw.org/dt/sparql/path";
    public static final RDFDatatypePath INSTANCE = new RDFDatatypePath();

    public RDFDatatypePath() {
        this(IRI);
    }

    public RDFDatatypePath(String uri) {
        super(uri);
    }

    @Override
    public Class<?> getJavaClass() {
        return Path.class;
    }

    /**
     * Convert a value of this datatype out
     * to lexical form.
     */
    @Override
    public String unparse(Object value) {
        String result = value instanceof Path
                ? PathWriter.asString((Path)value)
                : null;

        return result;
    }

    /**
     * Parse a lexical form of this datatype to a value
     * @throws DatatypeFormatException if the lexical form is not legal
     */
    @Override
    public Path parse(String lexicalForm) throws DatatypeFormatException {
        Path result;
        try {
            result = PathParser.parse(lexicalForm, PrefixMapping.Extended);
        } catch(Exception e) {
            // TODO This is not the best place for an expr eval exception; it should go to E_StrDatatype
            throw new ExprEvalException(e);
        }
        return result;
    }
}