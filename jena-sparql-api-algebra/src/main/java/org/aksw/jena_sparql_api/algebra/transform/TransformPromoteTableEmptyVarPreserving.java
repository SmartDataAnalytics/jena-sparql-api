package org.aksw.jena_sparql_api.algebra.transform;

import org.aksw.jena_sparql_api.algebra.utils.OpUtils;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpMinus;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.algebra.optimize.TransformPromoteTableEmpty;
import org.apache.jena.sparql.algebra.table.TableUnit;

/**
 * 
 * 
 * @author raven
 *
 */
public class TransformPromoteTableEmptyVarPreserving
	extends TransformPromoteTableEmpty
{
    public static Op transform(Op op) {
        Transform transform = new TransformPromoteTableEmptyVarPreserving();
        Op result = Transformer.transform(transform, op);
        return result;
    }

    @Override
    public Op transform(OpFilter opFilter, Op subOp) {
        if(isTableEmpty(subOp)) {
            return subOp;
        }
        return super.transform(opFilter, subOp);
    }

    @Override
    public Op transform(OpJoin opJoin, Op left, Op right) {
        // If either side is table empty return table empty
        if (isTableEmpty(left) || isTableEmpty(right)) {
            return OpUtils.createEmptyTableUnionVars(left, right);
        }
        return super.transform(opJoin, left, right);
    }

    @Override
    public Op transform(OpLeftJoin opLeftJoin, Op left, Op right) {
        // If LHS is table empty return table empty
        // If RHS is table empty can eliminate left join and just leave LHS
        if (isTableEmpty(left)) {
            return OpUtils.createEmptyTableUnionVars(left, right);
        } else if (isTableEmpty(right)) {
            return OpUtils.createEmptyTableUnionVars(left, right);
        }
        return super.transform(opLeftJoin, left, right);
    }

    @Override
    public Op transform(OpMinus opMinus, Op left, Op right) {
        // If LHS is table empty return table empty
        // If RHS is table empty can eliminate minus and just leave LHS since no
        // shared variables means no effect
        if (isTableEmpty(left)) {
            return left;
        } else if (isTableEmpty(right)) {
            return left;
        }
        return super.transform(opMinus, left, right);
    }

    @Override
    public Op transform(OpUnion opUnion, Op left, Op right) {
        // If one and only one side is table empty return other side
        // If both are table empty return table empty
        if (isTableEmpty(left)) {
            if (isTableEmpty(right)) {
                return OpUtils.createEmptyTableUnionVars(left, right);
            } else {
                return right;
            }
        } else if (isTableEmpty(right)) {
            return left;
        }
        return super.transform(opUnion, left, right);
    }
    

    public static boolean isTableEmpty(Op op) {
        boolean result;
    	if (op instanceof OpTable) {
    		Table table = ((OpTable)op).getTable();
            result = table.isEmpty();
        } else {
            result = false;
        }
    	return result;
    }
    
    public static boolean isTableUnit(Op op) {
        boolean result;
    	if (op instanceof OpTable) {
    		Table table = ((OpTable)op).getTable();
            result = table instanceof TableUnit;
        } else {
            result = false;
        }
    	return result;
    }
}
