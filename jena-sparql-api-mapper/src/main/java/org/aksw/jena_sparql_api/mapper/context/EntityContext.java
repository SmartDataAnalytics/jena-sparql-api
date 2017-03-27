package org.aksw.jena_sparql_api.mapper.context;

import java.util.Map;

/**
 * Interface for associating metadata with entities.
 * 
 * We could replace with with Jena EnhGraph
 *
 * @author raven
 *
 * @param <T>
 */
public interface EntityContext<T> {

	Map<String, Object> get(Object entity);

	Map<String, Object> getOrCreate(T entity);

	Map<String, Object> register(T entity);

	/**
	 *
	 * @param entity
	 * @return
	 */
	Map<String, Object> getState(Object entity);

	boolean isManaged(Object entity);


	/**
	 * getsOrCreates an entity and sets an attribute
	 * @param entity
	 * @param attribute
	 * @param value
	 */
	void setAttribute(T entity, String attribute, Object value);
	<X> X getAttribute(Object entity, String attribute, X defaultValue);
}
