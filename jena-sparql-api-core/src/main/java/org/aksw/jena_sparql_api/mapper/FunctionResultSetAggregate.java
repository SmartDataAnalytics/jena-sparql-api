package org.aksw.jena_sparql_api.mapper;

import java.util.function.Function;

import org.apache.jena.sparql.algebra.Table;

public class FunctionResultSetAggregate<T>
    implements Function<Table, T>
{
    private Agg<T> agg;

    public FunctionResultSetAggregate(Agg<T> agg) {
        this.agg = agg;
    }

    @Override
    public T apply(Table table) {
        Acc<T> acc = agg.createAccumulator();

        table.rows().forEachRemaining(acc::accumulate);

        T result = acc.getValue();

        return result;
    }

    public static <T> FunctionResultSetAggregate<T> create(Agg<T> agg) {
        FunctionResultSetAggregate<T> result = new FunctionResultSetAggregate<T>(agg);
        return result;
    }
}