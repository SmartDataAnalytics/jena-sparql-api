package org.aksw.jena_sparql_api.mapper;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.Var;

public class PartitionedQuery {
    protected Query query;
    protected Var var;

    public PartitionedQuery(Query query, Var var) {
        super();
        this.query = query;
        this.var = var;
    }

    public Query getQuery() {
        return query;
    }

    public Var getVar() {
        return var;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((query == null) ? 0 : query.hashCode());
        result = prime * result + ((var == null) ? 0 : var.hashCode());
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
        if (query == null) {
            if (other.query != null)
                return false;
        } else if (!query.equals(other.query))
            return false;
        if (var == null) {
            if (other.var != null)
                return false;
        } else if (!var.equals(other.var))
            return false;
        return true;
    }

    @Override
    public String toString() {
        String result = "" + var + " | " + query;
        return result;
    }


}
