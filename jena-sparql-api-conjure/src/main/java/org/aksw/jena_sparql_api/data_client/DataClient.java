package org.aksw.jena_sparql_api.data_client;

import org.apache.jena.rdf.model.Resource;

public interface DataClient {
    //protected QueryExecutionFactory qef;

//    QueryExecutionFactory findDistributions(Concept datasetFilter);
	
//	/**
//	 * Load a copy of the RDF graph with the given identifier.
//	 * Implementations may 
//	 * 
//	 * 
//	 * @param identifier
//	 * @return
//	 */
//	ModelFlow cloneModel(String identifier);
	
	/**
	 * Load a copy of the RDF based on the given specification
	 * @param spec
	 * @return
	 */
	ModelFlow cloneModelFromWorkflow(Resource spec);
}
