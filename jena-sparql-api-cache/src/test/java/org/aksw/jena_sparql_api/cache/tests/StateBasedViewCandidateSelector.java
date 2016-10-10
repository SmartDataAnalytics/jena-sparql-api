package org.aksw.jena_sparql_api.cache.tests;
import java.util.Collection;

import org.apache.jena.sparql.expr.Expr;

public interface StateBasedViewCandidateSelector {
    /**
     *
     * @param expr
     */
    Collection<StateBasedViewCandidateSelector> lookup(Expr expr);
}
