package org.aksw.jena_sparql_api.sparql.algebra.mapping;

import org.aksw.commons.collections.trees.Tree;

public interface TreeMatchingStrategyFactoryDecider<A, B> {
    MatchingStrategyFactory<A, B> createFactory(Tree<A> aTree, Tree<B> bTree, A aNode, B bNode);
}
