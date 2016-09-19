package org.aksw.jena_sparql_api.views.index;

import org.aksw.commons.collections.trees.Tree;
import org.aksw.jena_sparql_api.concept_cache.collection.FeatureMap;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.expr.Expr;

/**
 * Data structure that captures information about a query
 * @author raven
 *
 */
public class QueryIndex {
	//protected Node id;

    protected Op op;

    protected Tree<Op> tree;

    /**
     * Index over all of a query's quad patterns
     * Allows retrieving any of a query's quad patterns using a given set of features
     *
     */
    protected FeatureMap<Expr, QuadPatternIndex> quadPatternIndex;

    public QueryIndex(Op op, Tree<Op> tree, FeatureMap<Expr, QuadPatternIndex> quadPatternIndex) {
        super();
        this.quadPatternIndex = quadPatternIndex;
        this.op = op;
        this.tree = tree;
    }

//    public Node getId() {
//    	return id;
//    }

    public Op getOp() {
        return op;
    }

    public Tree<Op> getTree() {
        return tree;
    }

    public FeatureMap<Expr, QuadPatternIndex> getQuadPatternIndex() {
        return quadPatternIndex;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((quadPatternIndex == null) ? 0
                : quadPatternIndex.hashCode());
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
        QueryIndex other = (QueryIndex) obj;
        if (quadPatternIndex == null) {
            if (other.quadPatternIndex != null)
                return false;
        } else if (!quadPatternIndex.equals(other.quadPatternIndex))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "QueryIndex [quadPatternIndex=" + quadPatternIndex + "]";
    }
}