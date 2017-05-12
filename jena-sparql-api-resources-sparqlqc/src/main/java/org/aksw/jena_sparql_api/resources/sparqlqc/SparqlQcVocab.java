package org.aksw.jena_sparql_api.resources.sparqlqc;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;

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

    public static final Property Schema = property("Schema" );


    // TODO Replace with SPIN or LSQ Query
    public static final Property id = property("http://ex.org/ontology/id");

    public static final Property variant = property("http://ex.org/ontology/variant");
}
