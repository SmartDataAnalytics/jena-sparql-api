package org.aksw.jena_sparql_api.algebra.transform;

import java.util.Map;
import java.util.Map.Entry;

import org.aksw.jena_sparql_api.utils.VarExprListUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;

/**
 * Given extend(subOp, ?var: 'value') it adds an extra filter
 * filter(extend(subOp, { ?var: 'value'}), { ?var: 'value'}) 
 * 
 * This is used to streamline satisfiability checking.
 * 
 * The inverse transformation is {@link TransformRedundantFilterRemoval}
 * 
 * @author raven
 *
 */
public class TransformAddFilterFromExtend
	extends TransformCopy
{	
    public static Op transform(Op op) {
        Transform transform = new TransformAddFilterFromExtend();
        Op result = Transformer.transform(transform, op);
        return result;
    }

	
	public static ExprList addTo(ExprList result, Map<Var, Node> map) {
		for(Entry<Var, Node> e : map.entrySet()) {
			Expr x = new E_Equals(new ExprVar(e.getKey()), NodeValue.makeNode(e.getValue()));
			result.add(x);
		}

		return result;
	}
	
	@Override
	public Op transform(OpExtend opExtend, Op subOp) {
		VarExprList vel = opExtend.getVarExprList();
		Map<Var, Node> map = VarExprListUtils.extractConstants(vel);
		
		ExprList el = addTo(new ExprList(), map);

		Op tmp = super.transform(opExtend, subOp);

		Op result = el.isEmpty()
				? tmp
				: OpFilter.filterBy(el, tmp);

		return result;
	}

}
