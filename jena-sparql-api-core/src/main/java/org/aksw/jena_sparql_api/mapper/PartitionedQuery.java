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
}
