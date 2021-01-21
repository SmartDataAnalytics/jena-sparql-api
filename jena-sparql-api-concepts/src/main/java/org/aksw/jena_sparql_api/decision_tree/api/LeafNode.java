package org.aksw.jena_sparql_api.decision_tree.api;

public interface LeafNode<C, V, T>
	extends DtNode<C, V, T>
{
	T getValue();
	DtNode<C, V, T> setValue(T value);
	
	@Override
	default <X> X accept(DtVisitor<C, V, T> visitor) {
		X result = visitor.visit(this);
		return result;
	}
}