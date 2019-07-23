package org.aksw.jena_sparql_api.sparql.ext.url;

import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

public class JenaExtensionUrl {
    public static String ns = "http://jsa.aksw.org/fn/url/";
    
    public static void register() {
        FunctionRegistry.get().put(ns + "text", E_UrlText.class);
        FunctionRegistry.get().put(ns + "normalize", E_UrlNormalize.class);

        PropertyFunctionRegistry.get().put(ns + "text", new PropertyFunctionFactoryUrlText());
    }
    
    public static void addPrefixes(PrefixMapping pm) {
		pm.setNsPrefix("url", ns);
    }
}
