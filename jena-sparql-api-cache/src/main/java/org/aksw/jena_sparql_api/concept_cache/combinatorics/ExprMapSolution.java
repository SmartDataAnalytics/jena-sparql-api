package org.aksw.jena_sparql_api.concept_cache.combinatorics;

import java.util.Map;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;

public class ExprMapSolution {
    protected Map<Var, Var> varMap;
    protected Expr sourceExpr;
    protected Expr targetExpr;

    protected Expr targetMatchExpr;

    public ExprMapSolution(Map<Var, Var> varMap, Expr sourceExpr,
            Expr targetExpr, Expr targetMatchExpr) {
        super();
        this.varMap = varMap;
        this.sourceExpr = sourceExpr;
        this.targetExpr = targetExpr;
        this.targetMatchExpr = targetMatchExpr;
    }

    public Map<Var, Var> getVarMap() {
        return varMap;
    }

    public Expr getSourceExpr() {
        return sourceExpr;
    }

    public Expr getTargetExpr() {
        return targetExpr;
    }

    public Expr getTargetMatchExpr() {
        return targetMatchExpr;
    }
}