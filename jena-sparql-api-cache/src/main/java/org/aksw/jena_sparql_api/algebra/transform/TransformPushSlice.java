package org.aksw.jena_sparql_api.algebra.transform;

import java.util.List;

import org.aksw.jena_sparql_api.concept_cache.op.OpUtils;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.OpSlice;


/**
 * Transformation that pushes slices further down and into service clauses.
 *
 * @author raven
 *
 */
public class TransformPushSlice
	extends TransformCopy
{
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
			}
		}


		if(result == null) {
			result = super.transform(opSlice, subOp);
		}

		return result;
	}
}
