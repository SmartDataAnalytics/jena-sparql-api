package org.aksw.jena_sparql_api.mapper;

import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;

public class PartitionedQueryN
    implements PartitionedQuery
{
    protected Query query;
    protected List<Var> partitionVars;

    public PartitionedQueryN(Query query, List<Var> partitionVars) {
        super();
        this.query = query;
        this.partitionVars = partitionVars;
    }

    public Query getQuery() {
        return query;
    }

    public List<Var> getPartitionVars() {
        return partitionVars;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((partitionVars == null) ? 0 : partitionVars.hashCode());
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
        PartitionedQueryN other = (PartitionedQueryN) obj;
        if (partitionVars == null) {
            if (other.partitionVars != null)
                return false;
        } else if (!partitionVars.equals(other.partitionVars))
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
        return "PartitionedQueryN [query=" + query + ", partitionVars="
                + partitionVars + "]";
    }
}
