package org.aksw.commons.graph.index.jena.transform;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.core.Var;

public class OpTransformNormalizeUnaryOps
    extends TransformCopy
{
    protected OpDistinctExtendFilter wrap(Op subOp) {
        Set<Var> acc = new LinkedHashSet<>();
        if(subOp instanceof OpExt) {
            Op tmp = ((OpExt) subOp).effectiveOp();
            OpVars.visibleVars(tmp, acc);
        } else {
            OpVars.visibleVars(subOp, acc);
        }

        OpDistinctExtendFilter result = subOp instanceof OpDistinctExtendFilter
            ? (OpDistinctExtendFilter)subOp
            : new OpDistinctExtendFilter(subOp, acc)
            ;

        return result;
    }

    @Override
    public Op transform(OpFilter opFilter, Op subOp) {
        OpDistinctExtendFilter result = wrap(subOp);
        result.getDef().applyFilter(opFilter.getExprs());
        return result;
    }


    /**
     * Distinct has implications in up (towards parent) and down (towards children) directions of the tree:
     * upwards: the current set of projected variables is distinct
     * downwards: substituting the currently visible variables with an expression where they
     * are distinct would be valid. Hence, we can propagate the information that the visible variables
     * are allowed to be distinct.
     *
     *
     */
    @Override
    public Op transform(OpDistinct op, Op subOp) {
        OpDistinctExtendFilter result = wrap(subOp);
        result.getDef().applyDistinct();
        return result;
    }

    @Override
    public Op transform(OpProject op, Op subOp) {
        OpDistinctExtendFilter result = wrap(subOp);
        result.getDef().applyProject(op.getVars());
        return result;
    }

    @Override
    public Op transform(OpExtend op, Op subOp) {
        OpDistinctExtendFilter result = wrap(subOp);
        result.getDef().applyExtend(op.getVarExprList());
        return result;
    }

    @Override
    public Op transform(OpExt subOp) {
        OpDistinctExtendFilter result = wrap(subOp);
        return result;
    }

//    @Override
//    public Op transform(OpDisjunction opDisjunction, List<Op> elts) {
//
//    }

}
