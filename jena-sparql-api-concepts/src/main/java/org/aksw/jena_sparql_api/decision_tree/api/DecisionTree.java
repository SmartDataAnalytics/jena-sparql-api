package org.aksw.jena_sparql_api.decision_tree.api;

interface Node<C, T> {
	C getCondition();
	T getValue();
	
	// Stream<> find();
}

/**
 *  
 * 
 * @author raven
 *
 * @param <I>
 * @param <T>
 */
public interface DecisionTree<I, C, T, N extends Node<C, T>> {

	
	N getRoot();

	
	// Stream<> find(I input);
}
