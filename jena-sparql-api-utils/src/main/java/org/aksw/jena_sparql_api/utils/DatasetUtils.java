package org.aksw.jena_sparql_api.utils;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Quad;

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

//	public static Dataset createFromResourceInDefaultGraph(Resource resource) {
//		Dataset result = DatasetFactory.create();
//		result.getDefaultModel().add(resource.getURI(), resource.getModel());
//		return result;
//	}

	/**
	 * Helper method that retrieves the default model if
	 * Quad.isDefaultGraph's yields true for the given graphName
	 * 
	 * @param dataset
	 * @param graphName
	 * @return
	 */
	public static Model getDefaultOrNamedModel(Dataset dataset, Node graphNameNode) {
		String graphName = graphNameNode.getURI();
		Model result = getDefaultOrNamedModel(dataset, graphName);

		return result;
	}

	public static Model getDefaultOrNamedModel(Dataset dataset, String graphName) {
		Node g = NodeFactory.createURI(graphName);
		boolean isDefaultGraph = Quad.isDefaultGraph(g);

		Model result = isDefaultGraph
			? dataset.getDefaultModel()
			: dataset.getNamedModel(graphName);
			
		return result;
	}

}
