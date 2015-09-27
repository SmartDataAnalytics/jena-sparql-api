package org.aksw.jena_sparql_api.batch.cli.main;

import com.hp.hpl.jena.query.Query;

public class SparqlStmtQuery
    extends SparqlStmtBase
{
    protected Query query;

    public SparqlStmtQuery(Query query) {
        super();
        this.query = query;
    }

    public Query getQuery() {
        return query;
    }

    @Override
    public boolean isQuery() {
        return true;
    }

    @Override
    public SparqlStmtQuery getAsQueryStmt() {
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((query == null) ? 0 : query.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
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
