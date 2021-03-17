package org.aksw.jena_sparql_api.algebra.transform;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.op.Op0;
import org.apache.jena.sparql.algebra.op.Op1;
import org.apache.jena.sparql.algebra.op.Op2;
import org.apache.jena.sparql.algebra.op.OpAssign;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpConditional;
import org.apache.jena.sparql.algebra.op.OpDiff;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.algebra.op.OpGroup;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLabel;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpList;
import org.apache.jena.sparql.algebra.op.OpMinus;
import org.apache.jena.sparql.algebra.op.OpN;
import org.apache.jena.sparql.algebra.op.OpOrder;
import org.apache.jena.sparql.algebra.op.OpProcedure;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpPropFunc;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.algebra.op.OpReduced;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.algebra.op.OpTopN;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.algebra.op.OpUnion;

/**
 * An implementation of {@link TransformCopy} that forwards all calls to transformOpXXX methods
 * with the appropriate signature.
 *
 * @author raven
 *
 */
public class TransformCopyAdapter
    extends TransformCopy
{
    public interface TriFunction<A, B, C, R> {
        R apply(A a, B b, C c);
    }

    protected <OP0 extends Op0> Op transformOp0(
            OP0 op,
            Function<? super OP0, ? extends Op> fallback) {
        return fallback.apply(op);
    }

    protected <OP1 extends Op1> Op transformOp1(
            OP1 op,
            Op subOp,
            BiFunction<? super OP1, ? super Op, ? extends Op> fallback) {
        return fallback.apply(op, subOp);
    }

    protected <OP2 extends Op2> Op transformOp2(
            OP2 op,
            Op left,
            Op right,
            TriFunction<? super OP2, ? super Op, ? super Op, ? extends Op> fallback) {
        return fallback.apply(op, left, right);
    }

    protected <OPN extends OpN> Op transformOpN(
            OPN op,
            List<Op> subOps,
            BiFunction<? super OPN, ? super List<Op>, ? extends Op> fallback) {
        return fallback.apply(op, subOps);
    }

    protected Op transformOpExt(
            OpExt op,
            Function<? super OpExt, ? extends Op> fallback) {
        return fallback.apply(op);
    }

    
    /*
     * Op0
     */
    
    @Override
    public Op transform(OpTriple op) {
        return transformOp0(op, super::transform);
    }

    @Override
    public Op transform(OpBGP op) {
        return transformOp0(op, super::transform);
    }

    @Override
    public Op transform(OpQuadPattern op) {
        return transformOp0(op, super::transform);
    }

    @Override
    public Op transform(OpQuadBlock op) {
        return transformOp0(op, super::transform);
    }

    @Override
    public Op transform(OpTable op) {
        return transformOp0(op, super::transform);
    }
    
    
    /*
     * Op1
     */

    @Override
    public Op transform(OpFilter op, Op subOp) {
        return transformOp1(op, subOp, super::transform);
    }

    @Override
    public Op transform(OpGraph op, Op subOp) {
        return transformOp1(op, subOp, super::transform);
    }

    @Override
    public Op transform(OpProcedure op, Op subOp) {
        return transformOp1(op, subOp, super::transform);
    }

    @Override
    public Op transform(OpPropFunc op, Op subOp) {
        return transformOp1(op, subOp, super::transform);
    }

    @Override
    public Op transform(OpLabel op, Op subOp) {
        return transformOp1(op, subOp, super::transform);
    }

    @Override
    public Op transform(OpAssign op, Op subOp) {
        return transformOp1(op, subOp, super::transform);
    }

    @Override
    public Op transform(OpExtend op, Op subOp) {
        return transformOp1(op, subOp, super::transform);
    }

    /* OpModifier */
    
    @Override
    public Op transform(OpList op, Op subOp) {
        return transformOp1(op, subOp, super::transform);
    }

    @Override
    public Op transform(OpOrder op, Op subOp) {
        return transformOp1(op, subOp, super::transform);
    }

    @Override
    public Op transform(OpTopN op, Op subOp) {
        return transformOp1(op, subOp, super::transform);
    }

    @Override
    public Op transform(OpProject op, Op subOp) {
        return transformOp1(op, subOp, super::transform);
    }

    @Override
    public Op transform(OpDistinct op, Op subOp) {
        return transformOp1(op, subOp, super::transform);
    }

    @Override
    public Op transform(OpReduced op, Op subOp) {
        return transformOp1(op, subOp, super::transform);
    }

    @Override
    public Op transform(OpSlice op, Op subOp) {
        return transformOp1(op, subOp, super::transform);
    }

    @Override
    public Op transform(OpGroup op, Op subOp) {
        return transformOp1(op, subOp, super::transform);
    }
    
    /*
     * Op2
     */
    
    @Override
    public Op transform(OpJoin op, Op left, Op right) {
        return transformOp2(op, left, right, super::transform);
    }

    @Override
    public Op transform(OpLeftJoin op, Op left, Op right) {
        return transformOp2(op, left, right, super::transform);
    }

    @Override
    public Op transform(OpDiff op, Op left, Op right) {
        return transformOp2(op, left, right, super::transform);
    }

    @Override
    public Op transform(OpMinus op, Op left, Op right) {
        return transformOp2(op, left, right, super::transform);
    }

    @Override
    public Op transform(OpUnion op, Op left, Op right) {
        return transformOp2(op, left, right, super::transform);
    }

    @Override
    public Op transform(OpConditional op, Op left, Op right) {
        return transformOp2(op, left, right, super::transform);
    }

    
    /*
     * OpN
     */

    @Override
    public Op transform(OpSequence op, List<Op> elts) {
        return transformOpN(op, elts, super::transform);
    }

    @Override
    public Op transform(OpDisjunction op, List<Op> elts) {
        return transformOpN(op, elts, super::transform);
    }

    
    /*
     * OpExt
     */

    @Override
    public Op transform(OpExt opExt) {
        return transformOpExt(opExt, super::transform);
    }
}
