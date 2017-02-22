package org.aksw.jena_sparql_api.mapper.impl.type;

import org.aksw.jena_sparql_api.concepts.Relation;

public interface PathResolver {
	
	/**
	 * The property name leading to this path resolver - null for the root path
	 * 
	 * @return
	 */
	PathResolver getParent();
//	PathFragment getPathFragment();

	PathResolver resolve(String propertyName);

	Relation getOverallRelation();	
}
