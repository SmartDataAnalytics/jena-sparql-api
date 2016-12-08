package org.aksw.jena_sparql_api.core.utils;

import org.apache.jena.query.QueryExecution;

/**
 * TODO Figure out whether we can replace with a Jena internal class
 *
 * @author raven
 *
 */
public class QueryExecutionAndType {
    private QueryExecution queryExecution;
    private int queryType;

    public QueryExecutionAndType(QueryExecution queryExecution, int queryType) {
        super();
        this.queryExecution = queryExecution;
        this.queryType = queryType;
    }

    public int getQueryType() {
        return queryType;
    }

    public QueryExecution getQueryExecution() {
        return queryExecution;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((queryExecution == null) ? 0 : queryExecution.hashCode());
        result = prime * result + queryType;
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
        QueryExecutionAndType other = (QueryExecutionAndType) obj;
        if (queryExecution == null) {
            if (other.queryExecution != null)
                return false;
        } else if (!queryExecution.equals(other.queryExecution))
            return false;
        if (queryType != other.queryType)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "QueryExecutionAndType [queryType=" + queryType
                + ", queryExecution=" + queryExecution + "]";
    }
}