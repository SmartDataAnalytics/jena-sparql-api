package org.aksw.jena_sparql_api.sparql.ext.sys;

import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

public class JenaExtensionSys {
    public static String ns = "http://jsa.aksw.org/fn/sys/";
    
    public static void register() {
        FunctionRegistry.get().put(ns + "getenv", E_Getenv.class);
    }
    
    public static void addPrefixes(PrefixMapping pm) {
		pm.setNsPrefix("sys", ns);
    }
}
