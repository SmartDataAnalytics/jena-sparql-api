package org.aksw.jena_sparql_api.algebra.transform;

import java.util.LinkedHashSet;
import java.util.Set;

import org.aksw.jena_sparql_api.utils.VarExprListUtils;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.ExprList;

/**
 * Pulls up Extend over joins
 * join(extend(?foo...), extend(?bar...)) becomes
 * extend(?foo... ?bar..., join(...)) 
 * 
 * If for example the resulting join is between two BGPs, it allows for nicely merging them afterwards.
 * In conjunction with TransformPullFiltersIfCanMergeBGPs this is even more powerful.
 * 
 * 
 * 
 * @author raven
 *
 */
public class TransformPullExtend
	extends TransformCopy
{
	public static Op transform(Op op) {
		Transform transform = new TransformPullExtend();
		Op result = Transformer.transform(transform, op);
		//Op result = FixpointIteration.apply(op, o -> Transformer.transform(transform, o));
        //Op result = Transformer.transform(transform, op);
        return result;
	}

	// Filter(Extend(X, e), f) -> Extend(Filter(X, f), e) if filter does not depend on any variable
	// of e
	@Override
	public Op transform(OpFilter opFilter, Op subOp) {
		Op result = null;
		if(subOp instanceof OpExtend) {
			OpExtend e = (OpExtend)subOp;
			
			VarExprList vel = e.getVarExprList();
			Set<Var> extendVars = VarExprListUtils.getVarsMentioned(vel);
			
			ExprList el = opFilter.getExprs();
			Set<Var> usedVars = el.getVarsMentioned();

			
			Set<Var> conflicts = Sets.intersection(extendVars, usedVars);
			if(conflicts.isEmpty()) {
				boolean containsSpecialVars =
						TransformPullFiltersIfCanMergeBGPs.containsSpecialVar(extendVars) ||
						TransformPullFiltersIfCanMergeBGPs.containsSpecialVar(usedVars);
				
				if(!containsSpecialVars) {
					result = OpExtend.create(OpFilter.filterBy(el, e.getSubOp()), vel);
				}
			}
			
		}
		
		if(result == null) {
			result = super.transform(opFilter, subOp);
		}
		
		return result;
	}

	@Override
	public Op transform(OpJoin opJoin, Op left, Op right) {
		OpExtend ol = left instanceof OpExtend ? ((OpExtend)left) : null;
		OpExtend or = right instanceof OpExtend ? ((OpExtend)right) : null;
		VarExprList velLeft = ol != null ? ol.getVarExprList() : null;
		VarExprList velRight = or != null ? or.getVarExprList() : null;

		Op result;
		if(velLeft != null && velRight != null) {
			Set<Var> conflicts = Sets.intersection(
				new LinkedHashSet<>(velLeft.getVars()),
				new LinkedHashSet<>(velRight.getVars()));
			
			if(conflicts.isEmpty()) {
				VarExprList combined = new VarExprList();
				combined.addAll(velLeft);
				combined.addAll(velRight);
				
				result = OpExtend.extend(OpJoin.create(ol.getSubOp(), or.getSubOp()), combined);
			} else {
				// TODO We could pull up all non conflicting binds
				// Also, we could even create a filter FALSE for conflicting vars
				// But for now we don't bother
				result = super.transform(opJoin, left, right); 				
			}
		} else if(velLeft != null) {
			result = OpExtend.extend(OpJoin.create(ol.getSubOp(), right), velLeft);
		} else if(velRight != null) {
			result = OpExtend.extend(OpJoin.create(left, or.getSubOp()), velRight);			
		} else {
			result = super.transform(opJoin, left, right); 
		}
		
		return result;
	}

}
