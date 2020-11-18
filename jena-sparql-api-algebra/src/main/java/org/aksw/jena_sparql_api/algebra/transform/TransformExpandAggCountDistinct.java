package org.aksw.jena_sparql_api.algebra.transform;

import java.util.Collections;
import java.util.List;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpGroup;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.aggregate.AggCount;
import org.apache.jena.sparql.expr.aggregate.AggCountDistinct;
import org.apache.jena.sparql.expr.aggregate.Aggregator;

/**
 * Transform GROUP([count(distinct *) AS ?.0], subOp)
 * to GROUP([count(*) AS ?.0], DISTINCT(subOp))
 *
 * Used to mitigate a bug in Virtuoso
 *
 * @author raven
 *
 */
public class TransformExpandAggCountDistinct
    extends TransformCopy
{
    @Override
    public Op transform(OpGroup op, Op subOp) {

        Op tmp = null;

        List<ExprAggregator> eas = op.getAggregators();
        if (eas.size() == 1) {
            ExprAggregator ea = eas.get(0);
            Var ev = ea.getVar();
            Aggregator a = ea.getAggregator();
            if (a instanceof AggCountDistinct) {
                tmp = new OpGroup(
                        new OpDistinct(subOp),
                        op.getGroupVars(),
                        Collections.singletonList(new ExprAggregator(ev, new AggCount())));
            }
        }

        Op result = tmp != null
                ? tmp
                : super.transform(op, subOp);

        return result;
    }
}
