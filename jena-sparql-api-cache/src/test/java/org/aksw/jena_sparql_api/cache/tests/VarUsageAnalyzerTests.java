package org.aksw.jena_sparql_api.cache.tests;

import org.aksw.commons.collections.trees.Tree;
import org.aksw.commons.collections.trees.TreeUtils;
import org.aksw.jena_sparql_api.algebra.analysis.VarUsage;
import org.aksw.jena_sparql_api.algebra.utils.OpUtils;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.junit.Test;

public class VarUsageAnalyzerTests {

	@Test
	public void testVarUsage() {
		Op op = Algebra.toQuadForm(Algebra.compile(QueryFactory.create("PREFIX ex:<http://example.org/> SELECT DISTINCT ?s { { ?s a ex:Foo } { ?s ex:label ?l } }")));
		Tree<Op> tree = OpUtils.createTree(op);
		Op leaf = TreeUtils.getLeafs(tree).iterator().next();

		VarUsage varUsage = OpUtils.analyzeVarUsage(tree, leaf);
		System.out.println(varUsage);
	}
}
