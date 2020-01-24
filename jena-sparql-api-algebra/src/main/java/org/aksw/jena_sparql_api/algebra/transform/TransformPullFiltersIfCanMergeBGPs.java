package org.aksw.jena_sparql_api.algebra.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.optimize.TransformMergeBGPs;
import org.apache.jena.sparql.expr.ExprList;

/**
 * TransformMergeBGPS only works if the operands of a join are BGPS.
 * 
 * This version pulls up filters if it results in a
 * subsequent TransformMergeBGP to be applicable.
 * OpJoin(OpFilter[e](OpBgp[a](), OpBgp[b]())) -&gt; OpFilter[e](Join(OpBgp[a], OpBgp[b]())
 * 
 * 
 * 
 * @author raven
 *
 */
public class TransformPullFiltersIfCanMergeBGPs
	extends TransformMergeBGPs
{
	
    public static Op transform(Op op) {
        Transform transform = new TransformPullFiltersIfCanMergeBGPs();
        Op result = Transformer.transform(transform, op);
        return result;
    }

   
	@Override
	public Op transform(OpJoin opJoin, Op left, Op right) {
		Op tmp = xtransform(Arrays.asList(left, right), subOps -> OpJoin.create(subOps.get(0), subOps.get(1)));
	
		Op result = tmp == null
				? super.transform(opJoin, left, right)
				: tmp;
				
		return result;
	}


	@Override
	public Op transform(OpSequence opSequence, List<Op> elts) {
		Op tmp = xtransform(elts, opSequence::copy);

		Op result = tmp == null
				? super.transform(opSequence, elts)
				: tmp;
				
		return result;
	}
	
	
	public static Op xtransform(Collection<? extends Op> subOps, Function<? super List<Op>, ? extends Op> joinCtor) {
		// For every subOp tidy the filters
		ExprList exprs = new ExprList();
		List<Op> newSubOps = new ArrayList<>();
		for(Op subOp : subOps) {
			
			if(subOp instanceof OpFilter) {
				OpFilter tidied = OpFilter.tidy((OpFilter)subOp);
				exprs.addAll(tidied.getExprs());
				
				subOp = tidied.getSubOp();
			}
				
				
			if(subOp instanceof OpBGP) { // || subOp instanceof OpLeftJoin) {
				newSubOps.add(subOp);
			} else {
				newSubOps = null;
				break;
			}
		}

		Op result = newSubOps == null
			? null
			: OpFilter.filterBy(exprs, joinCtor.apply(newSubOps));
			
		return result;
	}
}
