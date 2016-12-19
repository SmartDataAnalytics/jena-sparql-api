package org.aksw.jena_sparql_api.vocabs;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Ad hoc Apache log vocabulary - actually for now just re-use terms of LSQ
 *
 * @author raven
 *
 */
public class ALOG {
    public static String ns = "http://alog.aksw.org/ontology/";

    public static Property property(String localName) { return ResourceFactory.createProperty(ns + localName); }

    //public static Property host = property("host");
    //public static Property  = property("host");
}
