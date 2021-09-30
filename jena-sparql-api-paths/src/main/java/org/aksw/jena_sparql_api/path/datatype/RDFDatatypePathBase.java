package org.aksw.jena_sparql_api.path.datatype;

import org.aksw.commons.path.core.Path;
import org.aksw.commons.path.core.PathOps;
import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprEvalException;

/**
 * A datatype for storing SPARQL property paths in RDF literals.
 *
 * @author Claus Stadler
 *
 */
public class RDFDatatypePathBase<T, P extends Path<T>>
    extends BaseDatatype
{
    // public static final String IRI = "http://jsa.aksw.org/dt/sparql/path";
    // public static final RDFDatatypePathBase INSTANCE = new RDFDatatypePathBase();

    protected PathOps<T, P> pathOps;

    public RDFDatatypePathBase(String uri, PathOps<T, P> pathOps) {
        super(uri);
        this.pathOps = pathOps;
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
                ? ((Path)value).toString()//  PathWriter.asString((Path)valu
                : null;

        return result;
    }

    /**
     * Parse a lexical form of this datatype to a value
     * @throws DatatypeFormatException if the lexical form is not legal
     */
    @Override
    public P parse(String lexicalForm) throws DatatypeFormatException {
        P result;
        try {
            result = pathOps.fromString(lexicalForm);
        } catch(Exception e) {
            // TODO This is not the best place for an expr eval exception; it should go to E_StrDatatype
            throw new ExprEvalException(e);
        }
        return result;
    }

    /**
     * Extract a path from a given node..
     *
     * @return The extracted path or null if none could be extracted.
     *
     */
    public static <T, P extends Path<T>> P extractPath(Node node) {
        P p = null;

        if (node.isLiteral()) {
            Object o = node.getLiteralValue();
            if (o instanceof Path) {
                p = (P)o;
            }
        }

        return p;
    }


}