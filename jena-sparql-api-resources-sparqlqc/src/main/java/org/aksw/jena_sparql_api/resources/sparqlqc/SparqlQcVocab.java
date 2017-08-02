package org.aksw.jena_sparql_api.resources.sparqlqc;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class SparqlQcVocab {
    public static final String ns = "http://sparql-qc-bench.inrialpes.fr/testsuite#";

    public static Resource resource(String name) { return ResourceFactory.createResource(ns + name); }
    public static Property property(String name) { return ResourceFactory.createProperty(ns + name); }

    public static final Resource TestSuite = resource("TestSuite" );
    public static final Resource ContainmentTest = resource("ContainmentTest" );
    public static final Resource WarmupContainmentTest = resource("WarmupContainmentTest" );
    public static final Property hasTest = property("hasTest" );
    public static final Property sourceDir = property("sourceDir" );
    public static final Property sourceQuery = property("sourceQuery" );
    public static final Property targetQuery = property("targetQuery" );
    public static final Property rdfSchema = property("rdfSchema" );
    public static final Property result = property("result" );
    //public static final Property error = property("error" );

//    public static final Property sourceQueryRecord = property("sourceQueryRecord" );
//    public static final Property targetQueryRecord = property("targetQueryRecord" );

    public static final Property Schema = property("Schema" );


    // TODO Replace with SPIN or LSQ Query
    // TODO the query property links a query record with resource representing the query.
    // Same name for class and property is discouraged by best practices - thus rename
    public static final Property query = property("query");

    public static final Resource QueryRecord = resource("QueryRecord");
    public static final Property id = property("id"); //ResourceFactory.createProperty("http://ex.org/ontology/id");

    public static final Property variant = property("variant"); //ResourceFactory.createProperty("http://ex.org/ontology/variant");


    public static final Property Query = ResourceFactory.createProperty("http://lsq.aksw.org/vocab#Query");
    public static final Property sparqlQueryString = ResourceFactory.createProperty("http://lsq.aksw.org/vocab#text");
}
