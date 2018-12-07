package org.aksw.jena_sparql_api.sparql_path.core;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class VocabPath {
	public static final String uri = "http://ns.aksw.org/jassa/ontology/";
	
	public static final Resource start = ResourceFactory.createProperty("http://ns.aksw.org/jassa/resource/start");
	public static final Resource end = ResourceFactory.createProperty("http://ns.aksw.org/jassa/resource/end");

	public static final Property joinsWith = ResourceFactory.createProperty("http://ns.aksw.org/jassa/ontology/joinsWith");

	// Used to generally connect a start or end node with a set of other nodes
	public static final Property connectsWith = ResourceFactory.createProperty("http://ns.aksw.org/jassa/ontology/connectsWith");


	public static final Property hasOutgoingPredicate = ResourceFactory.createProperty("http://www.example.org/hasOutgoingPredicate");
	public static final Property hasIngoingPredicate = ResourceFactory.createProperty("http://www.example.org/hasIngoingPredicate");
	public static final Property isIngoingPredicateOf = ResourceFactory.createProperty("http://www.example.org/isIngoingPredicateOf");
	public static final Property isOutgoingPredicateOf = ResourceFactory.createProperty("http://www.example.org/isOutgoingPredicateOf");

}