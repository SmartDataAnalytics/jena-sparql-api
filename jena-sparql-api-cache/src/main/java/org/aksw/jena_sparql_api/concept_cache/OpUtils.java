package org.aksw.jena_sparql_api.concept_cache;

import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.Op0;
import com.hp.hpl.jena.sparql.algebra.op.Op1;
import com.hp.hpl.jena.sparql.algebra.op.Op2;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpN;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadBlock;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.algebra.op.OpTriple;

class OpUtils {

    /**
     * Traverse an op structure and create a map from each subOp to its immediate parent
     *
     * NOTE It must be ensured that common sub expressions are different objects,
     * since we are using an identity hash map for mapping children to parents
     *
     *
     * @param op
     * @return
     */
    public static Map<Op, Op> parentMap(Op rootOp) {
        Map<Op, Op> result = new IdentityHashMap<Op, Op>();

        result.put(rootOp, null);

        parentMap(rootOp, result);
        return result;
    }

    public static void parentMap(Op op, Map<Op, Op> result) {
        List<Op> subOps = getSubOps(op);

        for(Op subOp : subOps) {
            result.put(subOp, op);

            parentMap(subOp, result);
        }
    }


    public static List<Op> getSubOps(Op op) {
        List<Op> result;

        if(op instanceof Op0) {
            result = Collections.emptyList();
        } else if (op instanceof Op1) {
            result = Collections.singletonList(((Op1)op).getSubOp());
        } else if (op instanceof Op2) {
            Op2 tmp = (Op2)op;
            result = Arrays.asList(tmp.getLeft(), tmp.getRight());
        } else if (op instanceof OpN) {
            result = ((OpN)op).getElements();
        } else {
            throw new RuntimeException("Should not happen");
        }

        return result;
    }

    public static boolean isPatternFree(Op op) {
        boolean isPattern =
                op instanceof OpQuadPattern ||
                op instanceof OpQuadBlock ||
                op instanceof OpTriple ||
                op instanceof OpBGP;

        boolean result;

        if(isPattern) {
            result = false;
        } else {
            List<Op> subOps = getSubOps(op);

            result = true;
            for(Op subOp : subOps) {
                boolean tmp = isPatternFree(subOp);
                if(tmp == false) {
                    result = false;
                    break;
                }
            }
        }

        return result;
    }
}