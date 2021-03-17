package org.aksw.jena_sparql_api.algebra.transform;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.Op0;
import org.apache.jena.sparql.algebra.op.Op1;
import org.apache.jena.sparql.algebra.op.Op2;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.algebra.op.OpN;

/**
 * A transformer that performs an identity transform and adds all encountered ops to a
 * result collection. This includes ops from (NOT) exists expressions.
 * 
 * @author raven
 *
 */
public class TransformCollectOps
	extends TransformCopyAdapter
{
	protected Collection<Op> result;

	public TransformCollectOps() {
		this(new LinkedHashSet<>());
	}

	public TransformCollectOps(Collection<Op> result) {
		super();
		this.result = Objects.requireNonNull(result);
	}
	
	/**
	 * Get the set of encountered ops
	 * 
	 * @return
	 */
	public Collection<Op> getResult() {
		return result;
	}

	
	/**
	 * 
	 * @param start The starting {@link Op}
	 * @param skipService If true then internally use {@link Transformer#transformSkipService(org.apache.jena.sparql.algebra.Transform, Op)} instead of {@link Transformer#transform(org.apache.jena.sparql.algebra.Transform, Op)}
	 * @return
	 */
	public static Set<Op> collect(Op start, boolean skipService) {
		TransformCollectOps transform = new TransformCollectOps();
		
		if (skipService) {
			Transformer.transformSkipService(transform, start);			
		} else {
			Transformer.transform(transform, start);
		}
		
		Set<Op> result = (Set<Op>)transform.getResult();
		return result;
	}
	
	
	@Override
    protected <OP0 extends Op0> Op transformOp0(
            OP0 op,
            Function<? super OP0, ? extends Op> fallback) {

		result.add(op);
		
    	return super.transformOp0(op, fallback);
    }

	@Override
    protected <OP1 extends Op1> Op transformOp1(
            OP1 op,
            Op subOp,
            BiFunction<? super OP1, ? super Op, ? extends Op> fallback) {

		result.add(op);

    	return super.transformOp1(op, subOp, fallback);
    }
	
	@Override
    protected <OP2 extends Op2> Op transformOp2(
            OP2 op,
            Op left,
            Op right,
            TriFunction<? super OP2, ? super Op, ? super Op, ? extends Op> fallback) {

		result.add(op);

    	return super.transformOp2(op, left, right, fallback);
    }
	
	@Override
    protected <OPN extends OpN> Op transformOpN(
            OPN op,
            List<Op> subOps,
            BiFunction<? super OPN, ? super List<Op>, ? extends Op> fallback) {
        
		result.add(op);

		return super.transformOpN(op, subOps, fallback);
    }


	@Override
    protected Op transformOpExt(
            OpExt op,
            Function<? super OpExt, ? extends Op> fallback) {

		result.add(op);

		return super.transformOpExt(op, fallback);
    }
}
