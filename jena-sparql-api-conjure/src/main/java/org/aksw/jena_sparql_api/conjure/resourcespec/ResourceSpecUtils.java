package org.aksw.jena_sparql_api.conjure.resourcespec;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;

public class ResourceSpecUtils {
	/**
	 * Find references to placeholders in a model and resolve them
	 * 
	 * @param model
	 */
	public void resolve(Model model) {
		// Resource ResourceSpec = ResourceFactory.createResource("ResourceSpec");
		// model.listSubjectsWithProperty(RDF.type, o)
		// List all resource spec instances
		// ideally only by predicate...
		// 
	}
}
