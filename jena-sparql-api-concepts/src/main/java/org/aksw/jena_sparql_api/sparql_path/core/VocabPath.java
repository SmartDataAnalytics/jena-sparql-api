package org.aksw.jena_sparql_api.sparql_path.core;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class VocabPath {
	public static final String uri = "http://ns.aksw.org/jassa/ontology/";
	
	public static final Resource start = ResourceFactory.createProperty("http://ns.aksw.org/jassa/resource/start");
	public static final Property joinsWith = ResourceFactory.createProperty("http://ns.aksw.org/jassa/ontology/joinsWith");


	public static final Property hasOutgoingPredicate = ResourceFactory.createProperty("http://www.example.org/hasOutgoingPredicate");
	public static final Property isIngoingPredicateOf = ResourceFactory.createProperty("http://www.example.org/isIngoingPredicateOf");

}