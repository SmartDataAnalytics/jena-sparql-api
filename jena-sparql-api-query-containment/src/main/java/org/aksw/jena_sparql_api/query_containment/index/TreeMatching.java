package org.aksw.jena_sparql_api.query_containment.index;

import org.aksw.jena_sparql_api.algebra.utils.ExprHolder;

public class TreeMatching {
    protected ExprHolder residualExpr;

//  protected Set<Var> distinct;


    public TreeMatching(ExprHolder residualExpr) {
        super();
        this.residualExpr = residualExpr;
    }

    public ExprHolder getResidualExpr() {
        return residualExpr;
    }


}
