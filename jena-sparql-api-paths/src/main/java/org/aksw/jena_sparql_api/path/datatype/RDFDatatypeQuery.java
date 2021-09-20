package org.aksw.jena_sparql_api.path.datatype;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.expr.ExprEvalException;

/**
 * A datatype for storing SPARQL property paths in RDF literals.
 *
 * @author Claus Stadler
 *
 */
public class RDFDatatypeQuery
    extends BaseDatatype
{
    public static final String IRI = "http://jsa.aksw.org/dt/sparql/query";
    public static final RDFDatatypeQuery INSTANCE = new RDFDatatypeQuery();

    public RDFDatatypeQuery() {
        this(IRI);
    }

    public RDFDatatypeQuery(String uri) {
        super(uri);
    }

    @Override
    public Class<?> getJavaClass() {
        return Query.class;
    }

    /**
     * Convert a value of this datatype out
     * to lexical form.
     */
    @Override
    public String unparse(Object value) {
        String result = value instanceof Query
                ? ((Query)value).toString()
                : null;

        return result;
    }

    /**
     * Parse a lexical form of this datatype to a value
     * @throws DatatypeFormatException if the lexical form is not legal
     */
    @Override
    public Query parse(String lexicalForm) throws DatatypeFormatException {
        Query result;
        try {
            result = QueryFactory.create(lexicalForm);
        } catch(Exception e) {
            // TODO This is not the best place for an expr eval exception; it should go to E_StrDatatype
            throw new ExprEvalException(e);
        }
        return result;
    }
}