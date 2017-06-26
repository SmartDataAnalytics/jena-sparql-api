package org.aksw.jena_sparql_api.concept_cache.dirty;

import java.util.List;

import org.aksw.jena_sparql_api.algebra.utils.OpUtils;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;

class Containment {
    public static void traverse(Op query, Op cache) {
        Object result;

        Ops type = Ops.valueOf(query.getClass().getSimpleName());
        switch(type) {
        case OpQuadPattern:

        case OpFilter:
        case OpDistinct:
        case OpProject:

        default:
            // Fallback strategy is to require equivalence
            // of the sub trees
            if(!query.getClass().equals(cache.getClass())) {
                result = null;
            } else {
                List<Op> querySubOps = OpUtils.getSubOps(query);
                List<Op> cacheSubOps = OpUtils.getSubOps(cache);


            }

        }
    }
}



public class QueryTraverser {
    public void traverse(Op query, Op cache) {
        Object result;

        Ops type = Ops.valueOf(query.getClass().getSimpleName());
        switch(type) {
        case OpQuadFilterPattern:
            // Find out which cache entries could be used
            // somehow add them to a candidate list

            // if we move to the parent,

        case OpDistinct:
        case OpProject:

        default:
            if(!query.getClass().equals(cache.getClass())) {
                result = null;
            } else {
                List<Op> querySubOps = OpUtils.getSubOps(query);
                List<Op> cacheSubOps = OpUtils.getSubOps(cache);

                //traverse

            }
        }

    }


    public static void travese(OpQuadPattern query, OpQuadPattern cache) {

    }
}
