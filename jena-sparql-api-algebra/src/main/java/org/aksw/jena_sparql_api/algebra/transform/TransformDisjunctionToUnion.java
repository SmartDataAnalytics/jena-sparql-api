package org.aksw.jena_sparql_api.algebra.transform;

import java.util.List;

import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpNull;
import org.apache.jena.sparql.algebra.op.OpUnion;

public class TransformDisjunctionToUnion
    extends TransformCopy
{
    public static final TransformDisjunctionToUnion fn = new TransformDisjunctionToUnion();

    @Override
    public Op transform(OpDisjunction opDisjunction, List<Op> elts) {
    	Op result = ExprUtils.opify(elts, OpUnion::create).orElse(OpNull.create());
    	return result;
    }
}
