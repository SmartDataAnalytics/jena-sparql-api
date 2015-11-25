package org.aksw.jena_sparql_api.util.frontier;

/**
 * The set of beans that yet need population
 *
 * @author raven
 *
 */
public interface Frontier<T>
{

	/**
	 * Add an entity to the frontier
	 *
	 * @param rdfType
	 * @param bean
	 */
	void add(T item);
	T next();
	boolean isEmpty();


	FrontierStatus getStatus(Object item);
	void setStatus(T item, FrontierStatus status);
	//boolean isDone(T item);
	//void makeDone(T item);
}
