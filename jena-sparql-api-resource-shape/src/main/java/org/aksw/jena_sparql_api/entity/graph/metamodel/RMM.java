package org.aksw.jena_sparql_api.entity.graph.metamodel;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

/** Resource meta model vocabulary */
public class RMM {

    public static final String NS = "http://www.example.org/";

    public static class Terms {
        public static String targetResource = NS + "targetResource";
    }

    public static final Property targetResource = ResourceFactory.createProperty(Terms.targetResource);
}
