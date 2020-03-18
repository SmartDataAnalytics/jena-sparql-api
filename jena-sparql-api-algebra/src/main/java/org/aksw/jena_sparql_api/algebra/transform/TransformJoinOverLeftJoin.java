package org.aksw.jena_sparql_api.algebra.transform;

import java.util.Set;

import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.core.Var;

/**
 * Join(LeftJoin(A, B), C) -> LeftJoin(Join(A, C), B) -
 * if all vars of C shared with B are also shared with A
 * 
 * @author raven
 *
 */
public class TransformJoinOverLeftJoin
	extends TransformCopy
{

    public static Op transform(Op op) {
        Transform transform = new TransformJoinOverLeftJoin();
        Op result = Transformer.transform(transform, op);
        return result;
    }

	@Override
	public Op transform(OpJoin opJoin, Op left, Op right) {
		Op result = null;
		if(left instanceof OpLeftJoin) {
			OpLeftJoin l = (OpLeftJoin)left;
			
			Op a = l.getLeft();
			Op b = l.getRight();
			Op c = right;

			Set<Var> avs = OpVars.visibleVars(a);
			Set<Var> bvs = OpVars.visibleVars(b);
			Set<Var> cvs = OpVars.visibleVars(c);

			Set<Var> confictCandidates = Sets.intersection(bvs, cvs);
			Set<Var> conflicts = Sets.difference(confictCandidates, avs);
			if(conflicts.isEmpty()) {
				result = OpLeftJoin.create(
					OpJoin.create(a, c), b, l.getExprs());
			} 
		}
		
		
		if(result == null) {
			result = super.transform(opJoin, left, right);
		}
		
		return result;
	}
}
