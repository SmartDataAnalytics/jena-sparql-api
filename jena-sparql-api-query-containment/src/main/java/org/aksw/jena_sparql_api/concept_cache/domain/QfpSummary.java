package org.aksw.jena_sparql_api.concept_cache.domain;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.utils.ClauseUtils;
import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;
import org.aksw.jena_sparql_api.utils.QuadUtils;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.graph.NodeTransform;

public class QfpSummary {
    private Set<Quad> quads;
    private Set<Set<Expr>> filterCnf;

    public QfpSummary(Set<Quad> quads, Set<Set<Expr>> filterCnf) {
        super();
        this.quads = quads;
        this.filterCnf = filterCnf;
    }

    public QfpSummary copySubstitute(Map<? extends Node, ? extends Node> varMap) {

        Set<Quad> newQuads = new HashSet<Quad>();
        for(Quad quad : quads) {
            Quad newQuad = QuadUtils.copySubstitute(quad, varMap);
            newQuads.add(newQuad);
        }

        NodeTransform transform = new NodeTransformRenameMap(varMap);
        //Expr newExpr = expr.applyNodeTransform(transform);
        Set<Set<Expr>> newFilterCnf = ClauseUtils.applyNodeTransformSet(filterCnf, transform);

        QfpSummary result = new QfpSummary(newQuads, newFilterCnf);
        return result;
    }

    public Set<Quad> getQuads() {
        return quads;
    }

    public Set<Set<Expr>> getFilterCnf() {
        return filterCnf;
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
        QfpSummary other = (QfpSummary) obj;
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

    @Override
    public String toString() {
        return "QfpSummary [quads=" + quads + ", filterCnf=" + filterCnf + "]";
    }
}
