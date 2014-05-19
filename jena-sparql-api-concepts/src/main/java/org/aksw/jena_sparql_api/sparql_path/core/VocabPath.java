package org.aksw.jena_sparql_api.sparql_path.core;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class VocabPath {
	public static final Resource start = ResourceFactory.createProperty("http://ns.aksw.org/jassa/resource/start");
	public static final Property joinsWith = ResourceFactory.createProperty("http://ns.aksw.org/jassa/ontology/joinsWith");
}