package org.aksw.jena_sparql_api.sparql.ext.io;

import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

public class JenaExtensionFs {
    public static String ns = "http://jsa.aksw.org/fn/fs/";
    
    public static void register() {
		PropertyFunctionRegistry.get().put(ns + "find", new PropertyFunctionFactoryFsFind());
    }
    
    public static void addPrefixes(PrefixMapping pm) {
		pm.setNsPrefix("fs", ns);
    }
}