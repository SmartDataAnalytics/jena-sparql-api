package org.aksw.jena_sparql_api.user_defined_function;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class UdfVocab {
	public static final String ns = "http://ns.aksw.org/jena/udf/";
	
	public static final Resource UserDefinedFunction = ResourceFactory.createResource(Strs.UserDefinedFunction);
	//public static final Resource UserDefinedPropertyFunction = ResourceFactory.createResource(ns + "UserDefinedPropertyFunction");

	public static final Property profile = ResourceFactory.createProperty(Strs.profile);

	public static class Strs {
		public static final String UserDefinedFunction = ns + "UserDefinedFunction";
		public static final String profile = ns + "profile";
	}
}
