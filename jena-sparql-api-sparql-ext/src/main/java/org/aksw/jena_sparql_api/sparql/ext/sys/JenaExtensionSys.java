package org.aksw.jena_sparql_api.sparql.ext.sys;

import org.aksw.jena_sparql_api.sparql.ext.benchmark.E_Benchmark;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.function.FunctionRegistry;

public class JenaExtensionSys {
    public static String ns = "http://jsa.aksw.org/fn/sys/";
    
    public static void register() {
        FunctionRegistry registry = FunctionRegistry.get();
        
        registry.put(ns + "getenv", E_Getenv.class);
        registry.put(ns + "benchmark", E_Benchmark.class);

    }
    
    public static void addPrefixes(PrefixMapping pm) {
		pm.setNsPrefix("sys", ns);
    }
}
