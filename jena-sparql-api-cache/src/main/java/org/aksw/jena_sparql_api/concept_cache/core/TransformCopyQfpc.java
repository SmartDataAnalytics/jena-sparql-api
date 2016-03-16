package org.aksw.jena_sparql_api.concept_cache.core;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.op.OpFilter;


public class TransformCopyQfpc
    extends TransformCopy
{
    /**
     * If the subOp of the filter is quad pattern,
     * transform it into a qfpc
     */
    @Override
    public Op transform(OpFilter opFilter, Op subOp) {
        Op result = null;
//
//        if(subOp instanceof OpQuadPattern) {
//
//        } else {
//            result = null;
//        }

        return result;
    }
}
