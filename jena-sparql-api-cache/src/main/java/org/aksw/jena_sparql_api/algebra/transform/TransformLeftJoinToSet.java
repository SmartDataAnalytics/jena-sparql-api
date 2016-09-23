package org.aksw.jena_sparql_api.algebra.transform;

import java.util.List;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.expr.ExprList;


/**
 * Transform an expression of
 *
 *
 * LeftJoin(
 *   LeftJoin(bl, br, be),
 *   ar, ae)
 *   with the expression e and ae TRUE
 *
 * to
 *
 * LeftJoinSet(bl, ar, br)
 *
 * Further:
 *   LeftJoinSet(X,
 *     LeftJoin(l, r, e))
 * becomes:
 *   LeftJoinSet(l, X, r)
 *

 *
 * @author raven
 *
 */
public class TransformLeftJoinToSet
	extends TransformCopy
{
	public static final TransformLeftJoinToSet fn = new TransformLeftJoinToSet();


	@Override
	public Op transform(OpLeftJoin opLeftJoin, Op left, Op right) {
		ExprList exprs = opLeftJoin.getExprs();
		Op result;
		if(exprs == null) {
			OpExtLeftJoinSet r = OpExtLeftJoinSet.create();

		    add(r, left);
		    add(r, right);

		    result = r;
		} else {
			result = OpLeftJoin.create(left, right, exprs);
		}


	    return result;
	}

	public static void add(OpExtLeftJoinSet dest, Op op) {
	    if(op instanceof OpExtLeftJoinSet) {
	    	OpExtLeftJoinSet o = (OpExtLeftJoinSet)op;
	        for(Op subOp : o.getElements()) {
	            dest.add(subOp);
	        }
	    } else if(op instanceof OpLeftJoin) {
	    	OpLeftJoin o = (OpLeftJoin)op;
	    	ExprList exprs = o.getExprs();
	    	if(exprs == null) {
		    	List<Op> elts = dest.getElements();
		    	elts.listIterator(0).add(o.getLeft());

		        //dest.add(o.getLeft());
		        elts.listIterator(elts.size() - 1).add(o.getRight());
	    	} else {
	    		dest.add(op);
	    	}
	    } else {
	        dest.add(op);
	    }
	}
}