package org.aksw.jena_sparql_api.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_GreaterThan;
import org.apache.jena.sparql.expr.E_GreaterThanOrEqual;
import org.apache.jena.sparql.expr.E_LessThan;
import org.apache.jena.sparql.expr.E_LessThanOrEqual;
import org.apache.jena.sparql.expr.Expr;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

public class RangeUtils {

    // Note: This method is tied to Jena, whereas all other RangeUtils only depend on Java/Guava.
    // So we should separate these utils
    public static Expr createExpr(Node node, Range<? extends NodeHolder> range) {
        Expr n = ExprUtils.makeNode(node);

        List<Expr> parts = new ArrayList<>();

        if (org.aksw.commons.util.range.RangeUtils.isSingleton(range)) {
            parts.add(new E_Equals(n, ExprUtils.makeNode(range.lowerEndpoint().getNode())));
        } else {

            if(range.hasLowerBound()) {
                if(range.lowerBoundType().equals(BoundType.OPEN)) {
                    parts.add(new E_GreaterThan(n, ExprUtils.makeNode(range.lowerEndpoint().getNode())));
                } else {
                    parts.add(new E_GreaterThanOrEqual(n, ExprUtils.makeNode(range.lowerEndpoint().getNode())));
                }
            }

            if(range.hasUpperBound()) {
                if(range.upperBoundType().equals(BoundType.OPEN)) {
                    parts.add(new E_LessThan(n, ExprUtils.makeNode(range.upperEndpoint().getNode())));
                } else {
                    parts.add(new E_LessThanOrEqual(n, ExprUtils.makeNode(range.upperEndpoint().getNode())));
                }
            }
        }

        Expr result = ExprUtils.andifyBalanced(parts);
        return result;
    }
}
