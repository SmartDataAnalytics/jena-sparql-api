package org.aksw.jena_sparql_api.io.binseach;

public interface Reference<T>
	extends AutoCloseable
{
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
//	void release();
//	
	boolean isReleased();
	
	/**
	 * Optional operation.
	 * 
	 * References may expose where they were aquired
	 * 
	 * @return
	 */
	StackTraceElement[] getAquisitionStackTrace();
}
