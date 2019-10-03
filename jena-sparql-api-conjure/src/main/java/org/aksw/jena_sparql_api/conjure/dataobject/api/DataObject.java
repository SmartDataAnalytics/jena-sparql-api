package org.aksw.jena_sparql_api.conjure.dataobject.api;

public interface DataObject {
	/**
	 * Whether the content of the data object can be modified
	 * 
	 */
	boolean isMutable();
	
	/**
	 * Indicate that the data object is no longer needed
	 * 
	 * 
	 * DataObject x = factory.create(spec);
	 * x.release(); // Behavior depends on the used factory
	 */
	void release();
}
