package org.aksw.jena_sparql_api.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 */
public class Constants {


    public static Map<String, String> extensionToJenaFormat = new HashMap<String, String>();

    public static String RDFXML = "RDF/XML";
    public static String N3 = "N3";
    public static String RDF_XML_ABBREV = "RDF/XML-ABBREV";
    public static String N_TRIPLE = "N-TRIPLE";
    public static String TURTLE = "TURTLE";
    public static String TTL = "TTL";

    static {
        extensionToJenaFormat.put(".rdfxml", RDFXML);
        extensionToJenaFormat.put(".rdf", RDFXML);
        extensionToJenaFormat.put(".n3", N3);
        extensionToJenaFormat.put(".nt", N_TRIPLE);
        extensionToJenaFormat.put(".ttl", TURTLE);
        extensionToJenaFormat.put(".ttl", TTL);
    }


}
