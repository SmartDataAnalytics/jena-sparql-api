package org.aksw.jena_sparql_api.algebra.transform;

import java.util.List;

import org.aksw.commons.util.range.RangeUtils;
import org.aksw.jena_sparql_api.algebra.utils.OpUtils;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.OpSlice;

import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;


/**
 * Transformation that pushes slices further down and into service clauses.
 *
 * @author raven
 *
 */
public class TransformPushSlice
    extends TransformCopy
{
    public static final TransformPushSlice fn = new TransformPushSlice();

    @Override
    public Op transform(OpSlice opSlice, Op subOp) {
        Op result = null;

        List<Op> subSubOps = OpUtils.getSubOps(subOp);
        if(subSubOps.size() == 1) {
            Op subSubOp = subSubOps.iterator().next();

            OpSlice replacement = new OpSlice(subSubOp, opSlice.getStart(), opSlice.getLength());

            if(subOp instanceof OpService) {
                OpService x = (OpService)subOp;
                result = new OpService(x.getService(), replacement, x.getServiceElement(), x.getSilent());
            } else if(subOp instanceof OpProject) {
                OpProject x = (OpProject)subOp;
                result = new OpProject(replacement, x.getVars());
            } else if(subOp instanceof OpExtend){
                OpExtend x = (OpExtend)subOp;
                result = OpExtend.create(replacement, x.getVarExprList());
            } else if(subOp instanceof OpSlice) {
                OpSlice x = (OpSlice)subOp;
                Range<Long> outer = QueryUtils.toRange(x);
                Range<Long> inner = QueryUtils.toRange(opSlice);
                // Merge
                Range<Long> combined = RangeUtils.makeAbsolute(outer, inner, DiscreteDomain.longs(), (a, b) -> a + b);
                result = QueryUtils.applyRange(x.getSubOp(), combined);
            }
        }


        if(result == null) {
            result = super.transform(opSlice, subOp);
        }

        return result;
    }
}
