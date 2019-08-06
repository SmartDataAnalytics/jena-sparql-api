package org.aksw.jena_sparql_api.user_defined_function;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class UdfVocab {
	public static final String ns = "http://ns.aksw.org/jena/udf/";
	
	public static final Resource UserDefinedFunction = ResourceFactory.createResource(ns + "UserDefinedFunction");
	//public static final Resource UserDefinedPropertyFunction = ResourceFactory.createResource(ns + "UserDefinedPropertyFunction");
}
