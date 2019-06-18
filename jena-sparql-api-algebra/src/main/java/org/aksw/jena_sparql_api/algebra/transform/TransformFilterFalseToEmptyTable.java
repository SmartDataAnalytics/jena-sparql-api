package org.aksw.jena_sparql_api.algebra.transform;

import org.aksw.jena_sparql_api.algebra.utils.OpUtils;
import org.aksw.jena_sparql_api.utils.ExprListUtils;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;

/**
 * Transform OpFilter(false, subOp) to OpTable(empty) 
 * 
 * @author raven
 *
 */
public class TransformFilterFalseToEmptyTable
	extends TransformCopy
{
    public static Op transform(Op op) {
        Transform transform = new TransformFilterFalseToEmptyTable();
        Op result = Transformer.transform(transform, op);
        return result;
    }
    
	@Override
	public Op transform(OpFilter opFilter, Op subOp) {
		ExprList exprs = opFilter.getExprs();
		boolean containsFalse = ExprListUtils.contains(exprs, NodeValue.FALSE);
		Op result = containsFalse ? OpUtils.createEmptyTableUnionVars(subOp) : super.transform(opFilter, subOp);

		return result;
	}
}
