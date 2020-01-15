package org.aksw.jena_sparql_api.conjure.datapod.impl;

public interface Reference<T> {
	T getValue();
	
	/**
	 * Aquire a new reference using
	 * 
	 * @return
	 */
	Reference<T> aquire(Object purpose);
	
	
	/**
	 * Release the reference
	 */
	void release();
}
