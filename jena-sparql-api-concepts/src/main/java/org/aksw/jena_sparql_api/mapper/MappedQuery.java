package org.aksw.jena_sparql_api.mapper;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;

public class MappedQuery<T> {

    protected PartitionedQuery1 partQuery;
    protected Agg<T> agg;

    public MappedQuery(PartitionedQuery1 partQuery, Agg<T> agg) {
        super();
        this.partQuery = partQuery;
        this.agg = agg;
    }

    public PartitionedQuery1 getPartQuery() {
        return partQuery;
    }

    public Agg<T> getAgg() {
        return agg;
    }

    public static <T> MappedQuery<T> create(Query query, Var partitionVar, Agg<T> agg) {
        MappedQuery<T> result = new MappedQuery<T>(new PartitionedQuery1Impl(query, partitionVar), agg);
        return result;
    }

    @Override
    public String toString() {
        return "" + partQuery + " with " + agg;
    }


}
