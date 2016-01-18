package org.aksw.jena_sparql_api.stmt;

import com.hp.hpl.jena.query.Query;

public class SparqlStmtQuery
    extends SparqlStmtBase
{
    protected Query query;

    public SparqlStmtQuery(Query query) {
        this(query, query.toString());
    }

    public SparqlStmtQuery(String queryString) {
        this(null, queryString);
    }

    public SparqlStmtQuery(Query query, String queryString) {
        this(query, queryString, null);
    }

    public SparqlStmtQuery(String queryString, Exception parseException) {
        this(null, queryString, parseException);
    }

    public SparqlStmtQuery(Query query, String queryString, Exception parseException) {
        super(queryString, parseException);
        this.query = query;
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
        return "SparqlStmtQuery [query=" + query + "]";
    }
}
