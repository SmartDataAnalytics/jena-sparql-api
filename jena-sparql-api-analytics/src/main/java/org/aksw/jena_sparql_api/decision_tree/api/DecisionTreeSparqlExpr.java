package org.aksw.jena_sparql_api.decision_tree.api;

import java.util.Collection;

import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;

/** Decision tree where leaf nodes only carry a single expression */
public class DecisionTreeSparqlExpr
	extends DecisionTreeSparqlBase<Expr>
{
	private static final long serialVersionUID = 0;

	public Node eval(Binding binding) {
		Collection<LeafNode<Expr, Node, Expr>> leafs = findLeafNodes(binding);
		
		Node result;
		if (leafs.isEmpty()) {
			result = null;
		} else {
			LeafNode<Expr, Node, Expr> leaf = Iterables.getOnlyElement(leafs);
			Expr expr = leaf.getValue();
			
			NodeValue nv = ExprUtils.eval(expr, binding);
			result = nv == null ? null : nv.asNode();
		}
		
		return result;
	}
	
}