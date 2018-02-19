package org.aksw.jena_sparql_api.query_containment.index;

import org.aksw.commons.collections.trees.Tree;

import com.google.common.collect.Table;

public interface TreeMappingFactory<A, B, S, M, TM extends TreeMapping<A, B, S, M>> {
	TM create(Tree<A> a, Tree<B> b, S s, Table<A, B, M> m);
}
