package org.aksw.jena_sparql_api.sparql.ext.http;

import java.util.function.Supplier;

import org.apache.http.client.HttpClient;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.function.FunctionRegistry;

public class JenaExtensionHttp {
    public static String ns = "http://jsa.aksw.org/fn/http/";

    public static void register(Supplier<HttpClient> httpClientSupplier) {
        FunctionRegistry.get().put(ns + "get", new FunctionFactoryE_Http(httpClientSupplier));
        FunctionRegistry.get().put(ns + "encode_for_qsa", E_EncodeForQsa.class);
    }
    
    public static void addPrefixes(PrefixMapping pm) {
		// Note Use of http prefix might be dangerous with jena or rdf in general!
    	pm.setNsPrefix("http", ns);
    }
}
