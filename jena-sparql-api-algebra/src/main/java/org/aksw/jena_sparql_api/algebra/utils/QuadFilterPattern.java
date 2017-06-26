package org.aksw.jena_sparql_api.algebra.utils;

import java.util.ArrayList;
import java.util.List;

import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.jena_sparql_api.utils.QuadUtils;

import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.QuadPattern;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.graph.NodeTransform;


public class QuadFilterPattern {
    private List<Quad> quads;
    private Expr expr;

    public QuadFilterPattern(List<Quad> quads, Expr expr) {
        super();
        this.quads = quads;
        this.expr = expr;
    }

    public List<Quad> getQuads() {
        return quads;
    }

    public Expr getExpr() {
        return expr;
    }

    public QuadFilterPattern applyNodeTransform(NodeTransform nodeTransform) {
        List<Quad> newQuads = new ArrayList<Quad>(quads.size());
        for(Quad quad : quads) {
            Quad newQuad = QuadUtils.applyNodeTransform(quad, nodeTransform);
            newQuads.add(newQuad);
        }

        Expr newExpr = expr.applyNodeTransform(nodeTransform);
        QuadFilterPattern result = new QuadFilterPattern(newQuads, newExpr);

        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((expr == null) ? 0 : expr.hashCode());
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
        QuadFilterPattern other = (QuadFilterPattern) obj;
        if (expr == null) {
            if (other.expr != null)
                return false;
        } else if (!expr.equals(other.expr))
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
        return "QuadFilterPattern [quads=" + quads + ", expr=" + expr + "]";
    }

}
