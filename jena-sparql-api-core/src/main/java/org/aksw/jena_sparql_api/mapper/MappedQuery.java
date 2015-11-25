package org.aksw.jena_sparql_api.mapper;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.Var;

public class MappedQuery<T> {

    protected PartitionedQuery partQuery;
    protected Agg<T> agg;

    public MappedQuery(PartitionedQuery partQuery, Agg<T> agg) {
        super();
        this.partQuery = partQuery;
        this.agg = agg;
    }

    public PartitionedQuery getPartQuery() {
        return partQuery;
    }

    public Agg<T> getAgg() {
        return agg;
    }

    public static <T> MappedQuery<T> create(Query query, Var partitionVar, Agg<T> agg) {
        MappedQuery<T> result = new MappedQuery<T>(new PartitionedQuery(query, partitionVar), agg);
        return result;
    }

    @Override
    public String toString() {
        return "" + partQuery + " with " + agg;
    }


}
