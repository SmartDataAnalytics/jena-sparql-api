package org.aksw.jena_sparql_api.mapper.impl.type;

public interface PathResolver {
	
	/**
	 * The property name leading to this path resolver - null for the root path
	 * 
	 * @return
	 */
	PathFragment getPathFragment();

	PathResolver resolve(String propertyName);
}
