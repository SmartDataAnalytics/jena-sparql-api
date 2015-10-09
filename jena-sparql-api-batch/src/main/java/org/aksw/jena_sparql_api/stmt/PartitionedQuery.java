package org.aksw.jena_sparql_api.stmt;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.Var;

public class PartitionedQuery {
    protected Var partitionVar;
    protected Query query;

    public PartitionedQuery(Var partitionVar, Query query) {
        super();
        this.partitionVar = partitionVar;
        this.query = query;
    }

    public Var getPartitionVar() {
        return partitionVar;
    }

    public Query getQuery() {
        return query;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((partitionVar == null) ? 0 : partitionVar.hashCode());
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
        PartitionedQuery other = (PartitionedQuery) obj;
        if (partitionVar == null) {
            if (other.partitionVar != null)
                return false;
        } else if (!partitionVar.equals(other.partitionVar))
            return false;
        if (query == null) {
            if (other.query != null)
                return false;
        } else if (!query.equals(other.query))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PartitionedQuery [partitionVar=" + partitionVar + ", query="
                + query + "]";
    }


}
