package org.aksw.jena_sparql_api.query_containment.index;

import org.aksw.commons.collections.trees.Tree;

import com.google.common.collect.Table;

public class TreeMapping<A, B, S, M> {
    protected Tree<A> aTree;
    protected Tree<B> bTree;
    protected S overallMatching;
    protected Table<A, B, M> nodeMappings;

    public TreeMapping(Tree<A> aTree, Tree<B> bTree, S overallMatching, Table<A, B, M> nodeMappings) {
        super();
        this.aTree = aTree;
        this.bTree = bTree;
        this.overallMatching = overallMatching;
        this.nodeMappings = nodeMappings;
    }

    public Tree<A> getaTree() {
        return aTree;
    }

    public Tree<B> getbTree() {
        return bTree;
    }

    public S getOverallMatching() {
        return overallMatching;
    }

    public Table<A, B, M> getNodeMappings() {
        return nodeMappings;
    }
}