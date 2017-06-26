package org.aksw.jena_sparql_api.algebra.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.utils.ClauseUtils;
import org.aksw.jena_sparql_api.utils.CnfUtils;
import org.aksw.jena_sparql_api.utils.DnfUtils;
import org.aksw.jena_sparql_api.utils.NfUtils;
import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;
import org.aksw.jena_sparql_api.utils.QuadPatternUtils;
import org.aksw.jena_sparql_api.utils.QuadUtils;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.graph.NodeTransform;

import com.google.common.collect.Sets;

public class QuadFilterPatternCanonical {
    protected Set<Quad> quads;


    // TODO Replace with exprHolder
//    protected Set<Set<Expr>> filterCnf;
//    protected Set<Set<Expr>> filterDnf;
    protected ExprHolder exprHolder;

//    public QuadFilterPatternCanonical(Set<Quad> quads, Set<Set<Expr>> filterCnf) {
//        this(quads, filterCnf, false);
//    }

    public QuadFilterPatternCanonical(Set<Quad> quads, ExprHolder exprHolder) {
        super();
        this.quads = quads;
        this.exprHolder = exprHolder;
    }


    public boolean isEmpty() {
        boolean result = quads.isEmpty() && exprHolder.isEmpty();
        return result;
    }

    public QuadFilterPattern toQfp()
    {
        Expr expr = DnfUtils.toExpr(exprHolder.getCnf()); //CnfUtils.toExpr(filterDnf);
        QuadFilterPattern result = new QuadFilterPattern(new ArrayList<>(quads), expr);
        return result;
    }

    //@Deprecated // Use OpUtils.toOp(...) instead
    public Op toOp() {

        ExprList exprs = CnfUtils.toExprList(exprHolder.getCnf());
        //QuadPattern qp = QuadPatternUtils.create(quads);

        Op result = OpUtils.toOp(quads, OpQuadPattern::new);
        result = OpFilter.filterBy(exprs,  result);
        return result;
    }

    public Set<Quad> getQuads() {
        return quads;
    }

    public ExprHolder getExprHolder() {
        return exprHolder;
    }

    public Set<Set<Expr>> getFilterCnf() {
        return exprHolder.getCnf();
    }

    public Set<Set<Expr>> getFilterDnf() {
        return exprHolder.getDnf();
    }

    public Set<Var> getVarsMentioned() {
        Set<Var> result = QuadPatternUtils.getVarsMentioned(quads);

        // Optionally, include the filterCnf, although a filter should never include any vars that are not part
        // of the quads
        Set<Var> extra = NfUtils.getVarsMentioned(exprHolder.getCnf());
        result.addAll(extra);

        return result;
    }

    public static QuadFilterPatternCanonical applyVarMapping(QuadFilterPatternCanonical qfpc, Map<Var, Var> varMap) {
        NodeTransform nodeTransform = new NodeTransformRenameMap(varMap);
        QuadFilterPatternCanonical result = qfpc.applyNodeTransform(nodeTransform);
        return result;
    }


    public QuadFilterPatternCanonical applyNodeTransform(NodeTransform nodeTransform) {
        Set<Quad> newQuads = QuadUtils.applyNodeTransform(quads, nodeTransform);
        Set<Set<Expr>> newExprs = ClauseUtils.applyNodeTransformSet(exprHolder.getCnf(), nodeTransform);

        QuadFilterPatternCanonical result = new QuadFilterPatternCanonical(newQuads, ExprHolder.fromCnf(newExprs));
        return result;
    }

    public QuadFilterPatternCanonical diff(QuadFilterPatternCanonical other) {
        Set<Quad> newQuads = new HashSet<Quad>(Sets.difference(quads, other.quads));
        Set<Set<Expr>> newCnf = new HashSet<Set<Expr>>(Sets.difference(exprHolder.getCnf(), other.exprHolder.getCnf()));

        QuadFilterPatternCanonical result = new QuadFilterPatternCanonical(newQuads, ExprHolder.fromCnf(newCnf));
        return result;
    }

    public boolean isSubsumedBy(QuadFilterPatternCanonical other) {
        boolean containsAllQuads = other.getQuads().containsAll(quads);

        boolean result = containsAllQuads
            ? CnfUtils.isSubsumedBy(other.getFilterCnf(), getFilterCnf())
            : false;

        return result;
    }

//    @Override
//    public String toString() {
//        return "QuadFilterPatternNorm [quads=" + quads + ", filterCnf="
//                + filterCnf + "]";
//    }



    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((exprHolder == null) ? 0 : exprHolder.hashCode());
        result = prime * result + ((quads == null) ? 0 : quads.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "<quads=" + quads + ", filterDnf="
                + exprHolder.getDnf() + ">";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        QuadFilterPatternCanonical other = (QuadFilterPatternCanonical) obj;
        if (exprHolder == null) {
            if (other.exprHolder != null)
                return false;
        } else if (!exprHolder.equals(other.exprHolder))
            return false;
        if (quads == null) {
            if (other.quads != null)
                return false;
        } else if (!quads.equals(other.quads))
            return false;
        return true;
    }

    /*
    public static void create(OpQuadPattern op) {

        Set<Quad> quads = new HashSet<Quad>(op.getPattern().getList());


        Set<Set<Expr>> filterCnf = CnfUtils.toSetCnf(expr);

        // This is part of the result
        //List<Set<Set<Expr>>> quadCnfList = new ArrayList<Set<Set<Expr>>>(quads.size());
        IBiSetMultimap<Quad, Set<Set<Expr>>> quadToCnf = new BiHashMultimap<Quad, Set<Set<Expr>>>();


        for(Quad quad : quads) {
            Set<Var> quadVars = QuadUtils.getVarsMentioned(quad);

            Set<Set<Expr>> cnf = new HashSet<Set<Expr>>(); //new HashSet<Clause>();

            for(Set<Expr> clause : filterCnf) {
                Set<Var> clauseVars = ClauseUtils.getVarsMentioned(clause);

                boolean containsAll = quadVars.containsAll(clauseVars);
                if(containsAll) {
                    cnf.add(clause);
                }
            }


            Set<Set<Expr>> quadCnf = normalize(quad, cnf);
            //quadCnfList.add(quadCnf);
            quadToCnf.put(quad, quadCnf);
        }

    }
*/
    public static void addFilter(QuadFilterPatternCanonical qfpc, ExprList exprs) {

    }

    /*
    public static QuadFilterPatternCanonical rename(QuadFilterPatternCanonical pattern, Map<Var, Var> varMap) {
        NodeTransform rename = new NodeTransformRenameMap(varMap);
        QuadFilterPatternCanonical result = pattern.applyNodeTransform(rename);

        return result;
    }
    */
}
