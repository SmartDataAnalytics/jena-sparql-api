package org;
import java.util.Collection;

import org.apache.jena.sparql.expr.Expr;

public interface StateBasedViewCandidateSelector {
    /**
     *
     * @param expr
     */
    Collection<StateBasedViewCandidateSelector> lookup(Expr expr);
}
