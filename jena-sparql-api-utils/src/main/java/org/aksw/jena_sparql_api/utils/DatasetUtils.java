package org.aksw.jena_sparql_api.utils;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Resource;

public class DatasetUtils {
	/**
	 * Create a dataset from an IRI resource by placing its associated model
	 * into a named model with that resource's IRI.  
	 * 
	 * @param resource The resource. Must be an IRI.
	 * @return The dataset
	 */
	public static Dataset createFromResource(Resource resource) {
		Dataset result = DatasetFactory.create();
		result.addNamedModel(resource.getURI(), resource.getModel());
		return result;
	}

}
