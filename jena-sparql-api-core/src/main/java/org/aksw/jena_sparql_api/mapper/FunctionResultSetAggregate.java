package org.aksw.jena_sparql_api.mapper;

import org.aksw.jena_sparql_api.lookup.ResultSetPart;

import com.google.common.base.Function;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.aggregate.Accumulator;
import com.hp.hpl.jena.sparql.expr.aggregate.Aggregator;
import com.hp.hpl.jena.sparql.function.FunctionEnv;

public class FunctionResultSetAggregate
    implements Function<ResultSetPart, NodeValue>
{
    private Aggregator aggregator;
    private FunctionEnv env;
    
    public FunctionResultSetAggregate(Aggregator aggregator) {
        this(aggregator, null);
    }
    
    public FunctionResultSetAggregate(Aggregator aggregator, FunctionEnv env) {
        this.aggregator = aggregator;
        this.env = env;
    }
    
    @Override
    public NodeValue apply(ResultSetPart rs) {
        Accumulator acc = aggregator.createAccumulator();
        
        //while(rs.hasNext()) {
        for(Binding binding : rs.getRows()) {
            //Binding binding = rs.nextBinding();
            acc.accumulate(binding, null);
        }
    
        NodeValue result = acc.getValue();
        return result;
    }    
}