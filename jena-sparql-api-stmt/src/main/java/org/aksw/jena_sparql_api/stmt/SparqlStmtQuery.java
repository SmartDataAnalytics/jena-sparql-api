package org.aksw.jena_sparql_api.stmt;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.shared.PrefixMapping;

public class SparqlStmtQuery
    extends SparqlStmtBase
{
    private static final long serialVersionUID = 1L;

    protected transient Query query;

    public SparqlStmtQuery(Query query) {
        this(query, query.toString());
    }

    public SparqlStmtQuery(String queryString) {
        this(null, queryString);
    }

    public SparqlStmtQuery(Query query, String queryString) {
        this(query, queryString, null);
    }

    public SparqlStmtQuery(String queryString, QueryParseException parseException) {
        this(null, queryString, parseException);
    }

    public SparqlStmtQuery(Query query, String queryString, QueryParseException parseException) {
        super(queryString, parseException);
        this.query = query;
    }

    private Object readResolve() {
        Query query = null;
        QueryParseException parseException = null;
        try {
            query = SparqlQueryParserImpl.createAsGiven().apply(originalString);
        } catch (QueryParseException e) {
            parseException = e;
        }
        return new SparqlStmtQuery(query, originalString, parseException);
    }


    @Override
    public SparqlStmtQuery clone() {
        Query clone = query != null
                ? query.cloneQuery()
                : null;

        return new SparqlStmtQuery(clone, originalString, parseException);
    }

    public Query getQuery() {
        return query;
    }

//    public String getQueryString() {
//        return this.queryString;
//    }

    @Override
    public boolean isQuery() {
        return true;
    }

    @Override
    public boolean isParsed() {
        return this.query != null;
    }

    @Override
    public SparqlStmtQuery getAsQueryStmt() {
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((query == null) ? 0 : query.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        SparqlStmtQuery other = (SparqlStmtQuery) obj;
        if (query == null) {
            if (other.query != null)
                return false;
        } else if (!query.equals(other.query))
            return false;
        return true;
    }

    @Override
    public String toString() {
        String result = query != null
                ? query.toString()
                : super.toString();

        return result;
    }

    @Override
    public PrefixMapping getPrefixMapping() {
        PrefixMapping result = query != null
                ? query.getPrefixMapping()
                : null;
        return result;
    }
}
