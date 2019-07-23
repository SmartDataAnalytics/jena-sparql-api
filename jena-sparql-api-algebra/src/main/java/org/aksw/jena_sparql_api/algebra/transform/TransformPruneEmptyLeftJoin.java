package org.aksw.jena_sparql_api.algebra.transform;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;

public class TransformPruneEmptyLeftJoin
	extends TransformCopy
{
    public static Op transform(Op op) {
        Transform transform = new TransformPruneEmptyLeftJoin();
        Op result = Transformer.transform(transform, op);
        return result;
    }

	@Override
	public Op transform(OpLeftJoin opLeftJoin, Op left, Op right) {
		boolean canPrune = TransformPromoteTableEmptyVarPreserving.isTableEmpty(right)
				|| TransformPromoteTableEmptyVarPreserving.isTableUnit(right);

		Op result = canPrune
				? left
				: super.transform(opLeftJoin, left, right);
		
		return result;
	}

}
