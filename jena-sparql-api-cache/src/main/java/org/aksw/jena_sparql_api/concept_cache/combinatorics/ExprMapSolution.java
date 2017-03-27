package org.aksw.jena_sparql_api.concept_cache.combinatorics;

import java.util.Map;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;

public class ExprMapSolution {
    protected Map<Var, Var> varMap;
    protected Expr needleExpr;
    protected Expr haystackExpr;

    protected Expr haystackMatchExpr;

    public ExprMapSolution(Map<Var, Var> varMap, Expr needleExpr,
            Expr haystackExpr, Expr haystackMatchExpr) {
        super();
        this.varMap = varMap;
        this.needleExpr = needleExpr;
        this.haystackExpr = haystackExpr;
        this.haystackMatchExpr = haystackMatchExpr;
    }

    public Map<Var, Var> getVarMap() {
        return varMap;
    }

    public Expr getNeedleExpr() {
        return needleExpr;
    }

    public Expr getHaystackExpr() {
        return haystackExpr;
    }

    public Expr getHaystackMatchExpr() {
        return haystackMatchExpr;
    }

    @Override
    public String toString() {
        return "ExprMapSolution [varMap=" + varMap + ", needleExpr="
                + needleExpr + ", haystackExpr=" + haystackExpr
                + ", haystackMatchExpr=" + haystackMatchExpr + "]";
    }

}