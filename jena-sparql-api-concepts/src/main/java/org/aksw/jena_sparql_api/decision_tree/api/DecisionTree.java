package org.aksw.jena_sparql_api.decision_tree.api;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;

interface DtNode<C, V, T> {
	DtNode<C, V, T> getParent();
	V getReachingValue();
	
	default boolean isInnerNode() {
		return this instanceof InnerNode;
	}
	
	default boolean isLeafNode() {
		return this instanceof LeafNode;
	}
	
	default InnerNode<C, V, T> asInnerNode() {
		return (InnerNode<C, V, T>)this;
	}

	default LeafNode<C, V, T> asLeafNode() {
		return (LeafNode<C, V, T>)this;
	}
}

interface LeafNode<C, V, T>
	extends DtNode<C, V, T>
{
	T getValue();
	DtNode<C, V, T> setValue(T value);
}


interface InnerNode<C, V, T>
	extends DtNode<C, V, T>
{
	C getCondition();

	InnerNode<C, V, T> setCondition(C condition);
	
	/** Create a new inner node for that value. Replaces any existing one for that value */
	InnerNode<C, V, T> newInnerNode(V value);

	/** Create a new leaf node for that value. Replaces any existing one for that value */
	LeafNode<C, V, T> newLeafNode(V value);
	
	/** Lookup a node for a given value */
	DtNode<C, V, T> getNode(Object value);
}

class DtNodeBase<C, V, T>
	implements DtNode<C, V, T>
{
	public DtNodeBase(InnerNode<C, V, T> parent, V reachingValue) {
		super();
		this.parent = parent;
		this.reachingValue = reachingValue;
	}
	
	protected InnerNode<C, V, T> parent;
	protected V reachingValue;
	
	@Override
	public InnerNode<C, V, T> getParent() {
		return parent;
	}
	
	@Override
	public V getReachingValue() {
		return reachingValue;
	}
}

class LeafNodeImpl<C, V, T>
	extends DtNodeBase<C, V, T>
	implements LeafNode<C, V, T>, Serializable
{
	private static final long serialVersionUID = 2175371135954988521L;

	protected T value;
	
	public LeafNodeImpl(InnerNode<C, V, T> parent, V reachingValue) {
		super(parent, reachingValue);
	}

	@Override
	public T getValue() {
		return value;
	}
	
	
	@Override
	public DtNode<C, V, T> setValue(T value) {
		this.value = value;
		return this;
	}
}

class InnerNodeImpl<C, V, T>
	extends DtNodeBase<C, V, T>
	implements InnerNode<C, V, T>, Serializable
{
	private static final long serialVersionUID = 7046323103753590235L;

	public InnerNodeImpl(InnerNode<C, V, T> parent, V reachingValue) {
		super(parent, reachingValue);
		this.condition = null;
		this.valueToChild = new LinkedHashMap<>();
	}

	protected C condition;
	protected Map<V, DtNode<C, V, T>> valueToChild;
	
	@Override
	public InnerNode<C, V, T> getParent() {
		return parent;
	}

	@Override
	public C getCondition() {
		return condition;
	}

	@Override
	public InnerNode<C, V, T> setCondition(C condition) {
		this.condition = condition;
		return this;
	}
	
	@Override
	public DtNode<C, V, T> getNode(Object value) {
		return valueToChild.get(value);
	}

	@Override
	public InnerNode<C, V, T> newInnerNode(V value) {
		InnerNode<C, V, T> result = new InnerNodeImpl<>(this, value);
		valueToChild.put(value, result);
		return result;
	}

	@Override
	public LeafNode<C, V, T> newLeafNode(V value) {
		LeafNode<C, V, T> result = new LeafNodeImpl<>(this, value);
		valueToChild.put(value, result);
		return result;
	}	
}


class DecisionTreeSparqlBase<T>
	implements Serializable
{
	protected InnerNode<Expr, Node, T> root = new InnerNodeImpl<>(null, null);
	
	public InnerNode<Expr, Node, T> getRoot() {
		return root;
	}
	
	public void recursiveFind(Binding binding, DtNode<Expr, Node, T> node, Collection<LeafNode<Expr, Node, T>> outResults) {
		if (node.isLeafNode()) {
			outResults.add(node.asLeafNode());
		} else {
			InnerNode<Expr, Node, T> innerNode = node.asInnerNode();
			Expr cond = innerNode.getCondition();
			
			// TODO Check whether all mentioned variables are bound
			NodeValue nv = ExprUtils.eval(cond, binding);
			Node eval = nv.asNode();
			
			DtNode<Expr, Node, T> child = innerNode.getNode(eval);
			
			recursiveFind(binding, child, outResults);
		}
	}
	
	public Collection<LeafNode<Expr, Node, T>> findLeafNodes(Binding binding) {
		Collection<LeafNode<Expr, Node, T>> result = new LinkedHashSet<>();
		recursiveFind(binding, getRoot(), result);
		return result;
	}
}

/** Decision tree where leaf nodes only carry a single expression */
class DecisionTreeSparqlExpr
	extends DecisionTreeSparqlBase<Expr>
{
	private static final long serialVersionUID = 4857543919330707414L;

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


/** Decision tree where leafs carry sets of expressions */
class DecisionTreeSparqlExprSet
	extends DecisionTreeSparqlBase<Set<Expr>>
{
	private static final long serialVersionUID = 5760959713235187882L;

	public Node eval(Binding binding) {
		Collection<LeafNode<Expr, Node, Set<Expr>>> leafs = findLeafNodes(binding);
		
		Node result;
		if (leafs.isEmpty()) {
			result = null;
		} else {
			LeafNode<Expr, Node, Set<Expr>> leaf = Iterables.getOnlyElement(leafs);
			Set<Expr> exprs = leaf.getValue();
			
//			Set<Node> vals = new HashSet<>();
			result = null;
			for (Expr expr : exprs) {
				NodeValue nv = ExprUtils.eval(expr, binding);
				Node node = nv == null ? null : nv.asNode();
				
				if (node != null) {
					result = node;
					break;
				}
			}
		}
		
		return result;
	}
}



/**
 *  
 * 
 * @author raven
 *
 * @param <I>
 * @param <T>
 */
public class DecisionTree {// <I, C, T, N extends DtNode<C, T>> {

	public static void main(String[] args) {
		DecisionTreeSparqlExpr dt = new DecisionTreeSparqlExpr();
		
		dt.getRoot().setCondition(ExprUtils.parse("BOUND(?p)"));
		LeafNode<Expr, Node, Expr> yes = dt.getRoot().newLeafNode(NodeValue.TRUE.asNode());
		yes.setValue(ExprUtils.parse("'yay'"));

		LeafNode<Expr, Node, Expr> no = dt.getRoot().newLeafNode(NodeValue.FALSE.asNode());
		no.setValue(ExprUtils.parse("'nah'"));

		Node result1 = dt.eval(BindingFactory.binding(Vars.p, NodeFactory.createLiteral("test")));
		System.out.println(result1);

		Node result2 = dt.eval(BindingFactory.binding(Vars.o, NodeFactory.createLiteral("test")));
		System.out.println(result2);

	}
	// Stream<> find(I input);
}
