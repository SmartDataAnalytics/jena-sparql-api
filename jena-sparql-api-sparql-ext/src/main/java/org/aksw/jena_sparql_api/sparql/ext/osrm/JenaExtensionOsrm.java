package org.aksw.jena_sparql_api.sparql.ext.osrm;

import org.apache.jena.sparql.pfunction.PropertyFunction;
import org.apache.jena.sparql.pfunction.PropertyFunctionFactory;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

public class JenaExtensionOsrm {

    public static String ns = "http://jsa.aksw.org/fn/osrm/";

    public static void register() {
        PropertyFunctionRegistry.get().put(ns + "query", new PropertyFunctionFactory() {
            @Override
            public PropertyFunction create(String uri) {
                return new OsrmRoutePF() ;
            }
        });
    }
}
