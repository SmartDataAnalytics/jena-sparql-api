package org.aksw.jena_sparql_api.decision_tree.api;

public interface DtNode<C, V, T> {
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
	
	<X> X accept(DtVisitor<C, V, T> visitor);
}
