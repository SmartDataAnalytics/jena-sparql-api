package org.aksw.jena_sparql_api.views.index;

import java.util.Set;

import org.aksw.commons.collections.FeatureMap;
import org.aksw.commons.collections.trees.Tree;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.expr.Expr;

/**
 * Data structure that captures information about a query
 * @author raven
 *
 */
public class OpIndex {
    //protected Node id;

    protected Op op;

    protected Tree<Op> tree;

    // TODO Add leafs

    /**
     * Index over all of a query's quad patterns
     * Allows retrieving any of a query's quad patterns using a given set of features
     *
     */
    protected FeatureMap<Expr, QuadPatternIndex> quadPatternIndex;


    // TODO This attribute is not used and might be removed
    protected Set<Set<String>> featureSets = null;

    public OpIndex(Op op, Tree<Op> tree, FeatureMap<Expr, QuadPatternIndex> quadPatternIndex) { //, Set<Set<String>> featureSets) {
        super();
        this.quadPatternIndex = quadPatternIndex;
        this.op = op;
        this.tree = tree;
        //this.featureSets = featureSets;
    }

    public OpIndex(Op op, Tree<Op> tree, FeatureMap<Expr, QuadPatternIndex> quadPatternIndex, Set<Set<String>> featureSets) {
        super();
        this.quadPatternIndex = quadPatternIndex;
        this.op = op;
        this.tree = tree;
        this.featureSets = featureSets;
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

    public Set<Set<String>> getFeatureSets() {
        return featureSets;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((featureSets == null) ? 0 : featureSets.hashCode());
        result = prime * result + ((op == null) ? 0 : op.hashCode());
        result = prime * result + ((quadPatternIndex == null) ? 0 : quadPatternIndex.hashCode());
        result = prime * result + ((tree == null) ? 0 : tree.hashCode());
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
        OpIndex other = (OpIndex) obj;
        if (featureSets == null) {
            if (other.featureSets != null)
                return false;
        } else if (!featureSets.equals(other.featureSets))
            return false;
        if (op == null) {
            if (other.op != null)
                return false;
        } else if (!op.equals(other.op))
            return false;
        if (quadPatternIndex == null) {
            if (other.quadPatternIndex != null)
                return false;
        } else if (!quadPatternIndex.equals(other.quadPatternIndex))
            return false;
        if (tree == null) {
            if (other.tree != null)
                return false;
        } else if (!tree.equals(other.tree))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "QueryIndex [quadPatternIndex=" + quadPatternIndex + "]";
    }
}