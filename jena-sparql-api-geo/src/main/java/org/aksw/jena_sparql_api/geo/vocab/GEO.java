package org.aksw.jena_sparql_api.geo.vocab;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class GEO {
    public static final String ns = "http://www.w3.org/2003/01/geo/wgs84_pos#";
    
    public static String getURI() {
        return ns;
    }
    

    public static final Property xlong = ResourceFactory.createProperty(ns + "long");
    public static final Property lat = ResourceFactory.createProperty(ns + "lat");
    
    
    /**
     * This property is not part of the vocabulary, but it has been used in DBpedia for
     * quite a while
     */
    public static final Resource geometry = ResourceFactory.createResource(ns + "geometry");
}
