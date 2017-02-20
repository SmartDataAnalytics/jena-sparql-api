package org.aksw.jena_sparql_api.mapper.impl.type;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.concepts.RelationUtils;

public interface PathResolver {
	
	/**
	 * The property name leading to this path resolver - null for the root path
	 * 
	 * @return
	 */
	PathResolver getParent();
	PathFragment getPathFragment();

	PathResolver resolve(String propertyName);
	
	default Relation getOverallRelation() {
		PathResolver parent = getParent();
		//PathResolver grandParent = parent != null ? parent.getParent() : null;

		Relation parentRelationContrib = parent == null
			? null
			: parent.getOverallRelation();

		Relation relationContrib = getPathFragment().getRelation();
		
		Relation result = parentRelationContrib == null
			? relationContrib
			: RelationUtils.and(parentRelationContrib, relationContrib, false);
				
		return result;
	}
}
