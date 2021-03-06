package org.aksw.jena_sparql_api.geo.vocab;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class GEOM {
    public static final String ns = "http://geovocab.org/geometry#";

    public static final Property geometry = ResourceFactory.createProperty(ns + "geometry");
}
