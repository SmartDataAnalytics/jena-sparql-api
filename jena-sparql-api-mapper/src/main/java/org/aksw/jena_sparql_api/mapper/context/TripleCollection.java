package org.aksw.jena_sparql_api.mapper.context;

import org.apache.jena.sparql.expr.Expr;

public interface TripleCollection {
    /**
     * Projection
     */
    NodeCollection subjects();
    NodeCollection predicates();
    NodeCollection objects();
    
    
    /**
     * add filters on the current triple pattern
     * 
     * @param expr
     */
    TripleCollection addFilter(Expr expr);
}
