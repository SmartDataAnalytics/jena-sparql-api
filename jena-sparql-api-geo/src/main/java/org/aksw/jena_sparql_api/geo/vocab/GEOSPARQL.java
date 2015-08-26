package org.aksw.jena_sparql_api.geo.vocab;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class GEOSPARQL {
    public static final String ns = "http://www.opengis.net/ont/geosparql#";

    public static final Resource Geometry = ResourceFactory.createResource(ns + "Geometry");
    
    public static final Property asWKT = ResourceFactory.createProperty(ns + "asWKT");
    public static final Property hasGeometry = ResourceFactory.createProperty(ns + "hasGeometry");
}
