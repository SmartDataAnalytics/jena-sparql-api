package org.aksw.jena_sparql_api.mapper.context;

import org.aksw.jena_sparql_api.mapper.model.RdfType;

/**
 * The set of beans that yet need population
 *
 * @author raven
 *
 */
public interface RdfPopulationFrontier {

	/**
	 * Add a bean that needs population under a given RdfType
	 *
	 * @param rdfType
	 * @param bean
	 */
	void add(RdfType rdfType, Object bean);
	
	
	
}
