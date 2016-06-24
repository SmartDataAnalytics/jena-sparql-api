package org.aksw.jena_sparql_api.concept_cache.domain;

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
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.graph.NodeTransform;

import com.google.common.collect.Sets;

public class QuadFilterPatternCanonical {
    private Set<Quad> quads;

    private Set<Set<Expr>> filterCnf;
    private Set<Set<Expr>> filterDnf;

    public QuadFilterPatternCanonical(Set<Quad> quads, Set<Set<Expr>> filterCnf) {
        super();
        this.quads = quads;
        this.filterCnf = filterCnf;
        this.filterDnf = DnfUtils.toSetDnf(CnfUtils.toExpr(filterCnf));

    }

    public boolean isEmpty() {
        boolean result = quads.isEmpty() && filterCnf.isEmpty();
        return result;
    }

    public QuadFilterPattern toQfp()
    {
        Expr expr = DnfUtils.toExpr(filterCnf); //CnfUtils.toExpr(filterDnf);
        QuadFilterPattern result = new QuadFilterPattern(new ArrayList<>(quads), expr);
        return result;
    }

    @Deprecated // Use OpUtils.toOp(...) instead
    public Op toOp() {

        ExprList exprs = CnfUtils.toExprList(filterCnf);
        //QuadPattern qp = QuadPatternUtils.create(quads);

        Op result = OpUtils.toOp(quads, exprs);
        return result;
    }

    public Set<Quad> getQuads() {
        return quads;
    }

    public Set<Set<Expr>> getFilterCnf() {
        return filterCnf;
    }

    public Set<Set<Expr>> getFilterDnf() {
        return filterDnf;
    }

    public Set<Var> getVarsMentioned() {
        Set<Var> result = QuadPatternUtils.getVarsMentioned(quads);

        // Optionally, include the filterCnf, although a filter should never include any vars that are not part
        // of the quads
        Set<Var> extra = NfUtils.getVarsMentioned(filterCnf);
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
        Set<Set<Expr>> newExprs = ClauseUtils.applyNodeTransformSet(filterCnf, nodeTransform);

        QuadFilterPatternCanonical result = new QuadFilterPatternCanonical(newQuads, newExprs);
        return result;
    }

    public QuadFilterPatternCanonical diff(QuadFilterPatternCanonical other) {
        Set<Quad> newQuads = new HashSet<Quad>(Sets.difference(other.quads, quads));
        Set<Set<Expr>> newCnf = new HashSet<Set<Expr>>(Sets.difference(other.filterCnf, filterCnf));

        QuadFilterPatternCanonical result = new QuadFilterPatternCanonical(newQuads, newCnf);
        return result;
    }

    public boolean isSubsumedBy(QuadFilterPatternCanonical other) {
        boolean containsAllQuads = other.getQuads().containsAll(quads);

        boolean result = containsAllQuads
            ? CnfUtils.isSubsumedBy(other.filterCnf, filterCnf)
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
                + ((filterCnf == null) ? 0 : filterCnf.hashCode());
        result = prime * result + ((quads == null) ? 0 : quads.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "QuadFilterPatternCanonical [quads=" + quads + ", filterCnf="
                + filterCnf + ", filterDnf=" + filterDnf + "]";
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
        if (filterCnf == null) {
            if (other.filterCnf != null)
                return false;
        } else if (!filterCnf.equals(other.filterCnf))
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
