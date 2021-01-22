package org.aksw.jena_sparql_api.decision_tree.api;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;

public class DecisionTreeSparqlBase<T>
	implements Serializable
{
	private static final long serialVersionUID = 0L;

	protected InnerNode<Expr, Node, T> root = new InnerNodeImpl<>(null, null, null);
	
	public InnerNode<Expr, Node, T> getRoot() {
		return root;
	}
	
	public void recursiveFind(Binding binding, DtNode<Expr, Node, T> node, Collection<LeafNode<Expr, Node, T>> outResults) {
		if (node.isLeafNode()) {
			outResults.add(node.asLeafNode());
		} else {
			InnerNode<Expr, Node, T> innerNode = node.asInnerNode();
			Expr cond = innerNode.getClassifier();
			
			Node eval;
			if (cond == null) {
				eval = null;
			} else {
				// FIXME Check whether all mentioned variables are bound?
				try {
					NodeValue nv = ExprUtils.eval(cond, binding);
					eval = nv.asNode();
				// } catch (VariableNotBoundException e) {
				} catch (ExprEvalException e) {
					eval = null;
				}
			}			

			LeafNode<Expr, Node, T> leaf = innerNode.getLeafNode(eval);
			if (leaf != null) {
				recursiveFind(binding, leaf, outResults);
			}

			Collection<? extends InnerNode<Expr, Node, T>> inners = innerNode.getInnerNodes(eval);			
			for (DtNode<Expr, Node, T> child : inners) {
				recursiveFind(binding, child, outResults);
			}
		}
	}
	
	public Collection<LeafNode<Expr, Node, T>> findLeafNodes(Binding binding) {
		Collection<LeafNode<Expr, Node, T>> result = new LinkedHashSet<>();
		recursiveFind(binding, getRoot(), result);
		return result;
	}

	@Override
	public String toString() {
		// return "root=" + root + "]";
		return root.toString();		
	}
}