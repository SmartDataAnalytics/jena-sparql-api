package org.aksw.jena_sparql_api.decision_tree.api;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.aksw.jena_sparql_api.decision_tree.impl.jena.DtVisitorToString;

import com.google.common.base.Optional;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class InnerNodeImpl<C, V, T>
	extends DtNodeBase<C, V, T>
	implements InnerNode<C, V, T>, Serializable
{
	private static final long serialVersionUID = 7046323103753590235L;

	/* For deserialization */
//	public InnerNodeImpl() {
//		super(null, null);
//	}
	
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
	
	/** Get all child inner nodes */
	@Override
	public Collection<? extends InnerNode<C, V, T>>  getInnerNodes() {
		return valueToConditionToInnerNode.values();
	}
	
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
	public Collection<? extends LeafNode<C, V, T>> getLeafNodes() {
		return valueToLeafNode.values();
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
		DtVisitorToString<C, V, T> visitor = new DtVisitorToString<>();
		visitor.visit(this);
		String result = visitor.getResult();		
		return result;
	}

}