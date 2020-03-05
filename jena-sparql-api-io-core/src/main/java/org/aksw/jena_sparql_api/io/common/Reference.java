package org.aksw.jena_sparql_api.io.common;

public interface Reference<T>
	extends AutoCloseable
{
	T get();
	
	/**
	 * Aquire a new reference with a given comment object
	 * 
	 * @return
	 */
	Reference<T> aquire(Object purpose);
	
	
	/**
	 * Release the reference
	 */
//	void release();
//	
	boolean isClosed();
	
	/**
	 * Optional operation.
	 * 
	 * References may expose where they were aquired
	 * 
	 * @return
	 */
	StackTraceElement[] getAquisitionStackTrace();
}
