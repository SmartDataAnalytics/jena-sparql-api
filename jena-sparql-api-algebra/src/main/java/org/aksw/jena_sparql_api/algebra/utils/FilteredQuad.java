package org.aksw.jena_sparql_api.algebra.utils;

import org.apache.jena.sparql.core.Quad;

public class FilteredQuad {
    protected Quad quad;
    protected ExprHolder expr;

    public Quad getQuad() {
        return quad;
    }
    public ExprHolder getExpr() {
        return expr;
    }


}
