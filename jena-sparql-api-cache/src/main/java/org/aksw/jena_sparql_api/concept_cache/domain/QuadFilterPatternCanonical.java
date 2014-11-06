package org.aksw.jena_sparql_api.concept_cache.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.jena_sparql_api.utils.ClauseUtils;
import org.aksw.jena_sparql_api.utils.CnfUtils;
import org.aksw.jena_sparql_api.utils.NfUtils;
import org.aksw.jena_sparql_api.utils.QuadPatternUtils;
import org.aksw.jena_sparql_api.utils.QuadUtils;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.algebra.op.OpSequence;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.graph.NodeTransform;

public class QuadFilterPatternCanonical {
    private Set<Quad> quads;
    private Set<Set<Expr>> filterCnf;

    public QuadFilterPatternCanonical(Set<Quad> quads, Set<Set<Expr>> filterCnf) {
        super();
        this.quads = quads;
        this.filterCnf = filterCnf;
    }

    public Op toOp() {

        ExprList exprs = CnfUtils.toExprList(filterCnf);
        QuadPattern qp = QuadPatternUtils.create(quads);

        Map<Node, BasicPattern> index = QuadPatternUtils.indexBasicPattern(qp);

        List<OpQuadPattern> opqs = new ArrayList<OpQuadPattern>();

        for(Entry<Node, BasicPattern> entry : index.entrySet()) {
            OpQuadPattern oqp = new OpQuadPattern(entry.getKey(), entry.getValue());
            opqs.add(oqp);
        }


        Op result;

        if(opqs.size() == 1) {
            result = opqs.iterator().next();
        } else {
            OpSequence op = OpSequence.create();

            for(OpQuadPattern item : opqs) {
                op.add(item);
            }

            result = op;
        }

        if(!exprs.isEmpty()) {
            result = OpFilter.filter(exprs, result);
        }

        return result;
    }

    public Set<Quad> getQuads() {
        return quads;
    }

    public Set<Set<Expr>> getFilterCnf() {
        return filterCnf;
    }

    public Set<Var> getVarsMentioned() {
        Set<Var> result = QuadUtils.getVarsMentioned(quads);

        // Optionally, include the filterCnf, although a filter should never include any vars that are not part
        // of the quads
        Set<Var> extra = NfUtils.getVarsMentioned(filterCnf);
        result.addAll(extra);

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

    @Override
    public String toString() {
        return "QuadFilterPatternNorm [quads=" + quads + ", filterCnf="
                + filterCnf + "]";
    }

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

}
