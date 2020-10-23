package org.aksw.jena_sparql_api.rx;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprTransformCopy;
import org.apache.jena.sparql.expr.aggregate.Aggregator;

public class ExprTransformAllocAggregate
    extends ExprTransformCopy
{
    protected Query query;

    public ExprTransformAllocAggregate(Query query) {
        super();
        this.query = query;
    }

    @Override
    public Expr transform(ExprAggregator eAgg) {
        ExprAggregator newExpr = (ExprAggregator)super.transform(eAgg);
        Aggregator agg = newExpr.getAggregator();
        Expr result = query.allocAggregate(agg);
        return result;
    }
}
