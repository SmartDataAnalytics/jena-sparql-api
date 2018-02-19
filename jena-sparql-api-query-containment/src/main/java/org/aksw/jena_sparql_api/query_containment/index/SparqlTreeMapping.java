package org.aksw.jena_sparql_api.query_containment.index;

import org.aksw.commons.collections.trees.Tree;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;

import com.google.common.collect.BiMap;
import com.google.common.collect.Table;

public class SparqlTreeMapping<R>
	extends TreeMapping<Op, Op, BiMap<Var, Var>, R>
{
	public SparqlTreeMapping(Tree<Op> aTree, Tree<Op> bTree, BiMap<Var, Var> overallMatching,
			Table<Op, Op, R> nodeMappings) {
		super(aTree, bTree, overallMatching, nodeMappings);
	}
}
