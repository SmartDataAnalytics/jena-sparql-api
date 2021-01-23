package org.aksw.jena_sparql_api.decision_tree.api;

public interface DtVisitor<C, V, T> {
	<X> X visit(InnerNode<C, V, T> innerNode);
	<X> X visit(LeafNode<C, V, T> leafNode);
}
