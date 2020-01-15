package org.aksw.jena_sparql_api.data_client;

import java.util.Iterator;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.update.UpdateRequest;

public interface ModelFlow {
	/**
	 * In place transformation of the dataset referred to by this ModelFlow
	 * 
	 * @param updateRequest
	 * @return
	 */
	ModelFlow execUpdate(UpdateRequest updateRequest);
	
	/**
	 * Create a new ModelFlow that wraps the result of the given construct query
	 * 
	 * @param query
	 * @return
	 */
	ModelFlow execConstruct(Query query);

	Model toModel();
	Iterator<Triple> toTriples();

}
