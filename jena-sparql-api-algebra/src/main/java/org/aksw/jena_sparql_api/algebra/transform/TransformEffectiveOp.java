package org.aksw.jena_sparql_api.algebra.transform;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.op.OpExt;

public class TransformEffectiveOp
	extends TransformCopy
{
	@Override
	public Op transform(OpExt opExt) {
		Op tmp = opExt.effectiveOp();
		Op result = tmp == null ? opExt : tmp;

		return result;
	}
}
