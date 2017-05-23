package org.aksw.jena_sparql_api.algebra.transform;

import java.util.List;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.expr.ExprList;

public class TransformSetToLeftJoin
    extends TransformCopy
{
    public static final TransformSetToLeftJoin fn = new TransformSetToLeftJoin();

    @Override
    public Op transform(OpExt op) {
    	Op result;

    	if(op instanceof OpExtLeftJoinSet) {
    		OpExtLeftJoinSet e = (OpExtLeftJoinSet)op;

    		List<Op> items = e.getElements();
    		result = items.get(0);
    		for(int i = 1; i < items.size(); ++i) {
    			Op right = items.get(i);
    			result = OpLeftJoin.create(result, right, (ExprList)null);
    		}
    	} else {
    		result = op;
    	}

        return result;
    }
}
