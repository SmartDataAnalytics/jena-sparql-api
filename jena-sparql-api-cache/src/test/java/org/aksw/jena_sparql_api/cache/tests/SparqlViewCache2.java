package org.aksw.jena_sparql_api.cache.tests;

import java.util.Map;

import org.apache.jena.sparql.algebra.Op;

public interface SparqlViewCache2 {
    /**
     * Given an algebra expression, return a mapping of substitutions
     *
     * @param op
     * @return
     */
    Map<Op, Op> lookup(Op op);
}
