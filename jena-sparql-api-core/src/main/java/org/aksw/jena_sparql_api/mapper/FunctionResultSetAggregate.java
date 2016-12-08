package org.aksw.jena_sparql_api.mapper;

import org.aksw.jena_sparql_api.utils.ResultSetPart;

import com.google.common.base.Function;
import org.apache.jena.sparql.engine.binding.Binding;

public class FunctionResultSetAggregate<T>
    implements Function<ResultSetPart, T>
{
    private Agg<T> agg;

    public FunctionResultSetAggregate(Agg<T> agg) {
        this.agg = agg;
    }

    @Override
    public T apply(ResultSetPart rs) {
        Acc<T> acc = agg.createAccumulator();
        
        for(Binding binding : rs.getBindings()) {
            acc.accumulate(binding);
        }

        T result = acc.getValue();

        return result;
    }
    
    public static <T> FunctionResultSetAggregate<T> create(Agg<T> agg) {
        FunctionResultSetAggregate<T> result = new FunctionResultSetAggregate<T>(agg);
        return result;
    }
}