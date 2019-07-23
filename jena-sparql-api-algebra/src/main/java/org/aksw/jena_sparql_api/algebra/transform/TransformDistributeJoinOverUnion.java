package org.aksw.jena_sparql_api.algebra.transform;

import java.util.List;

import org.aksw.jena_sparql_api.algebra.utils.FixpointIteration;
import org.aksw.jena_sparql_api.algebra.utils.OpUtils;
import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpUnion;

/**
 * Turn a join of unions into a union of joins.
 * It is recommended to use this transformation with the triple-based (i.e. NOT the quad based) form
 * and afterwards use another transform to merge BGPs:
 *
 * 		op = TransformDistributeJoinOverUnion.transform(op);
 *		op = Optimize.apply(new TransformMergeBGPs(), op);
 *
 *
 *
 * join(union(...), union(..)) -> union(join(...), join(...), ..., )
 *
 * @author raven
 */
public class TransformDistributeJoinOverUnion
	extends TransformCopy
{
	public static Op transform(Op op) {
		Transform transform = new TransformDistributeJoinOverUnion();
		Op result = FixpointIteration.apply(op, o -> Transformer.transform(transform, o));
        //Op result = Transformer.transform(transform, op);
        return result;
	}

	@Override
	public Op transform(OpJoin opJoin, Op left, Op right) {

		List<Op> as = OpUtils.getUnionMembers(left);
		List<Op> bs = OpUtils.getUnionMembers(right);

		Op result = distribute(as, bs);

		return result;
	}

	public static Op distribute(List<Op> as, List<Op> bs) {
		Op result = ExprUtils.distribute(as, bs, OpJoin::create, OpUnion::create);
		return result;
	}
}
