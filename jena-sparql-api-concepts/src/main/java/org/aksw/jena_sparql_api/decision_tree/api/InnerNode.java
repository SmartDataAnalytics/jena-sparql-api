package org.aksw.jena_sparql_api.decision_tree.api;

import java.util.Collection;

public interface InnerNode<C, V, T>
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
	
	/** Return all child inner nodes */
	Collection<? extends InnerNode<C, V, T>>  getInnerNodes();


	/** Lookup a node for a given value */
	Collection<? extends InnerNode<C, V, T>> getInnerNodes(Object value);

	/** Get the leaf node for the given value; create it if it does not exist */
	LeafNode<C, V, T> getOrCreateLeafNode(V value);

	Collection<? extends LeafNode<C, V, T>> getLeafNodes();
	
	/** Get the leaf node for the given value; returns null if it does not exist */
	LeafNode<C, V, T> getLeafNode(V value);

	@Override
	default <X> X accept(DtVisitor<C, V, T> visitor) {
		X result = visitor.visit(this);
		return result;
	}

}