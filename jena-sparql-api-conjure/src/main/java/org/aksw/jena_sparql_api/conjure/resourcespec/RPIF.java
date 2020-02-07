package org.aksw.jena_sparql_api.conjure.resourcespec;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class RPIF {
	public static final String ns = "http://w3id.org/rpif/vocab#";

	public static final Property resourceUrl = ResourceFactory.createProperty(ns + "resourceUrl");

	public static final Property op = ResourceFactory.createProperty(Strs.op);

	public static class Strs {
		public static final String resourceUrl = ns + "resourceUrl";	

		// Relation of an entity to an instance of a (conjure) operation
		public static final String op = ns + "op";	
	}
}
