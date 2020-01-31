package org.aksw.jena_sparql_api.utils.views.map;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Basic properties for realizing a Map<?, ?> in RDF.
 * 
 * @author raven
 *
 */
public class MapVocab {
	public static final String ns = "http://www.example.org/";
	public static Property property(String localName) {
		return ResourceFactory.createProperty(ns + localName);
	}

	public static final Property key = property("key");
	public static final Property value = property("value");
	public static final Property entry = property("entry");
}
