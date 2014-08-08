package org.aksw.jena_sparql_api.mapper;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.aggregate.Aggregator;
import com.hp.hpl.jena.sparql.expr.aggregate.AggregatorBase;

public abstract class AggBase
    extends AggregatorBase
{
    @Override
    public Aggregator copy(Expr expr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Node getValueEmpty() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String toPrefixString() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean equals(Object other) {
        // TODO Auto-generated method stub
        return false;
    }
    
}