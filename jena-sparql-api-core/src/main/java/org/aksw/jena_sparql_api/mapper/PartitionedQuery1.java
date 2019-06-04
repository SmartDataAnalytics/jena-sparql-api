package org.aksw.jena_sparql_api.mapper;

import java.util.Collections;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;

public class PartitionedQuery1
    implements PartitionedQuery
{
    protected Query query;
    protected Var partitionVar;

    public PartitionedQuery1(Query query, Var partitionVar) {
        super();
        this.query = query;
        this.partitionVar = partitionVar;
    }

    public Query getQuery() {
        return query;
    }

    public Var getPartitionVar() {
        return partitionVar;
    }

    @Override
    public List<Var> getPartitionVars() {
        return Collections.singletonList(partitionVar);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((query == null) ? 0 : query.hashCode());
        result = prime * result + ((partitionVar == null) ? 0 : partitionVar.hashCode());
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
        PartitionedQuery1 other = (PartitionedQuery1) obj;
        if (query == null) {
            if (other.query != null)
                return false;
        } else if (!query.equals(other.query))
            return false;
        if (partitionVar == null) {
            if (other.partitionVar != null)
                return false;
        } else if (!partitionVar.equals(other.partitionVar))
            return false;
        return true;
    }

    @Override
    public String toString() {
        String result = "" + partitionVar + " | " + query;
        return result;
    }
    
    public static PartitionedQuery1 from(Query view, Var viewVar) {
    	return new PartitionedQuery1(view, viewVar);
    }
}
