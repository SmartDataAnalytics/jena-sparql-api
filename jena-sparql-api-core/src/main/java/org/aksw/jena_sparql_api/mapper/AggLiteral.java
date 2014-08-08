package org.aksw.jena_sparql_api.mapper;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.aggregate.Accumulator;

public class AggLiteral
    extends AggBase
{
    private Expr expr;
    
    public AggLiteral(Expr expr) {
        this.expr = expr;
    }
    
    @Override
    public Expr getExpr() {
        return expr;
    }

    @Override
    public Accumulator createAccumulator() {
        BindingMapper<NodeValue> bindingMapper = new BindingMapperExpr(expr);
        
        Accumulator result = new AccLiteral(bindingMapper);
        return result;
    }   
}