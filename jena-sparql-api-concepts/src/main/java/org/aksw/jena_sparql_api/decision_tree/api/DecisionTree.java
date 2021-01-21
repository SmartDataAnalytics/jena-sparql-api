package org.aksw.jena_sparql_api.decision_tree.api;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.VariableNotBoundException;
import org.apache.jena.sparql.util.ExprUtils;

import com.google.common.base.Optional;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;



interface DtNode<C, V, T> {
	DtNode<C, V, T> getParent();
	V getReachingValue();

	// C getCondition();

	// Collection<T> getValues();
	// Table<V, C, DtNode<C, V, T>> getChildren();
	
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
	C getClassifier();

	/** Get the inner node for the given value and classifier; create it if it does not exist */
	InnerNode<C, V, T> getOrCreateInnerNode(V value, C classifier);

	/** Get the inner node for the given value and classifier; returns null if it does not exist */
	InnerNode<C, V, T> getInnerNode(V value, C classifier);

//	default InnerNode<C, V, T> getOrCreateInnerNode(C classifier) {
//		return getOrCreateInnerNode(null, classifier);
//	}

	/** Lookup a node for a given value */
	Collection<? extends InnerNode<C, V, T>> getInnerNodes(Object value);

	/** Get the leaf node for the given value; create it if it does not exist */
	LeafNode<C, V, T> getOrCreateLeafNode(V value);

	/** Get the leaf node for the given value; returns null if it does not exist */
	LeafNode<C, V, T> getLeafNode(V value);

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
	
	@Override
	public String toString() {
		return "Leaf(" + value + ")";
	}
}

class InnerNodeImpl<C, V, T>
	extends DtNodeBase<C, V, T>
	implements InnerNode<C, V, T>, Serializable
{
	private static final long serialVersionUID = 7046323103753590235L;

	public InnerNodeImpl(InnerNode<C, V, T> parent, V reachingValue, C classifier) {
		super(parent, reachingValue);
		this.classifier = classifier;
		
		// Defer init of maps to first use?
		this.valueToConditionToInnerNode = HashBasedTable.create(); //new LinkedHashMap<>();
		this.valueToLeafNode = new HashMap<>();
	}

	protected C classifier;
	// protected Multimap<V, DtNode<C, V, T>> valueToChild;
	
	/** Beware of the {@link Optional} vs {@link java.util.Optional} because the latter is NOT serializable...
	 * and Table does not allow null values */
	protected Table<Optional<V>, Optional<C>, InnerNode<C, V, T>> valueToConditionToInnerNode;
	protected Map<V, LeafNode<C, V, T>> valueToLeafNode;
	
	@Override
	public InnerNode<C, V, T> getParent() {
		return parent;
	}

	@Override
	public C getClassifier() {
		return classifier;
	}

//	@Override
//	public InnerNode<C, V, T> setCondition(C condition) {
//		this.condition = condition;
//		return this;
//	}
	
	@Override
	public Collection<? extends InnerNode<C, V, T>> getInnerNodes(Object value) {
		Optional<?> rowKey = Optional.fromNullable(value);

		// Ensure that rowKey is in 'valueToConditionToInnerNode' as to prevent the
		// cast '(V)value' from raising an exception
		@SuppressWarnings("unchecked")
		Collection<? extends InnerNode<C, V, T>> result = valueToConditionToInnerNode.rowKeySet().contains(rowKey)
			? valueToConditionToInnerNode.row(Optional.fromNullable((V)value)).values()
			: Collections.emptySet();
		
		return result; 
	}


	@Override
	public InnerNode<C, V, T> getInnerNode(V value, C classifier) {
		InnerNode<C, V, T> result = valueToConditionToInnerNode.get(Optional.fromNullable(value), Optional.fromNullable(classifier));
		return result;
	}
	
	@Override
	public InnerNode<C, V, T> getOrCreateInnerNode(V value, C classifier) {
		InnerNode<C, V, T> result = getInnerNode(value, classifier);

		if (result == null) {
			result = new InnerNodeImpl<>(this, value, classifier);
			valueToConditionToInnerNode.put(Optional.fromNullable(value), Optional.fromNullable(classifier), result);
		}

		return result;
	}

	@Override
	public LeafNode<C, V, T> getLeafNode(Object value) {
		return valueToLeafNode.get(value);
	}

	@Override
	public LeafNode<C, V, T> getOrCreateLeafNode(V value) {
		LeafNode<C, V, T> result = getLeafNode(value);
		
		if (result == null) {
			result = new LeafNodeImpl<>(this, value);
			//valueToLeafNode.put(Optional.fromNullable(value), result);
			valueToLeafNode.put(value, result);
		}
		return result;
	}
	
	@Override
	public String toString() {
		return "Inner(" + classifier + ", " + valueToLeafNode + ", " + valueToConditionToInnerNode + ")";
	}

}

//class SparqlInnerNode<T>
//
//{	
//}

class DecisionTreeSparqlBase<T>
	implements Serializable
{
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
				} catch (VariableNotBoundException e) {
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


/**
 * Decision tree where leafs carry sets of expressions
 * This is de-facto a convenience representation where expressions in the leaf nodes
 * implicitly have BOUND(?var) conditions for each variable mentioned in the expressions.
 * 
 * For this reason, this class may become deprecated.
 * 
 */
//class DecisionTreeSparqlExprSet
//	extends DecisionTreeSparqlBase<Set<Expr>>
//{
//	private static final long serialVersionUID = 5760959713235187882L;
//
//	public Node eval(Binding binding) {
//		Collection<LeafNode<Expr, Node, Set<Expr>>> leafs = findLeafNodes(binding);
//		
//		Node result;
//		if (leafs.isEmpty()) {
//			result = null;
//		} else {
//			LeafNode<Expr, Node, Set<Expr>> leaf = Iterables.getOnlyElement(leafs);
//			Set<Expr> exprs = leaf.getValue();
//			
////			Set<Node> vals = new HashSet<>();
//			result = null;
//			for (Expr expr : exprs) {
//				NodeValue nv = ExprUtils.eval(expr, binding);
//				Node node = nv == null ? null : nv.asNode();
//				
//				if (node != null) {
//					result = node;
//					break;
//				}
//			}
//		}
//		
//		return result;
//	}
//}



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
		
		InnerNode<Expr, Node, Expr> dn = dt.getRoot().getOrCreateInnerNode(null, ExprUtils.parse("?p = 'test'"));
		
		
		InnerNode<Expr, Node, Expr> on = dn.getOrCreateInnerNode(NodeValue.TRUE.asNode(), ExprUtils.parse("?o = 'hello'"));
		on.getOrCreateLeafNode(NodeValue.TRUE.asNode()).setValue(ExprUtils.parse("'oho'"));
		
		
//		LeafNode<Expr, Node, Expr> yes = dn.getOrCreateLeafNode(NodeValue.TRUE.asNode());
//		yes.setValue(ExprUtils.parse("'yay'"));

		LeafNode<Expr, Node, Expr> no = dn.getOrCreateLeafNode(NodeValue.FALSE.asNode());
		no.setValue(ExprUtils.parse("'nah'"));

		LeafNode<Expr, Node, Expr> otherwise = dn.getOrCreateLeafNode(null);
		otherwise.setValue(ExprUtils.parse("'fail'"));

		
		Node result1 = dt.eval(BindingFactory.binding(Vars.p, NodeFactory.createLiteral("test")));
		System.out.println(result1);

		Node result2 = dt.eval(BindingFactory.binding(Vars.o, NodeFactory.createLiteral("test")));
		System.out.println(result2);
		

		BindingMap bm = BindingFactory.create();
		bm.add(Vars.p, NodeFactory.createLiteral("test"));
		bm.add(Vars.o, NodeFactory.createLiteral("hello"));
		Node result3 = dt.eval(bm);
		System.out.println(result3);


	}
	// Stream<> find(I input);
}


/**
 * Var definitions based on decision tree structures.
 * This allows for the use of 'discriminator' columns, such is
 * ?x != (if (?p = 1) then IRI(STR(?o)) if (?p = 0) then ?o) 
 * 
 * 
 * @author raven
 *
 */
class ConditionalVarDefinition {
	protected Map<Var, DecisionTreeSparqlExpr> definitions;
	
	protected ConditionalVarDefinition() {
		this(new LinkedHashMap<>());
	}

	public ConditionalVarDefinition(Map<Var, DecisionTreeSparqlExpr> definitions) {
		super();
		this.definitions = definitions;
	}
	
	public ConditionalVarDefinition put(Var var, DecisionTreeSparqlExpr definition) {
		definitions.put(var, definition);
		return this;
	}
	
}
