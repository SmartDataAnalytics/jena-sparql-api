package org.aksw.jena_sparql_api.sparql.ext.sys;

import org.aksw.jena_sparql_api.sparql.ext.benchmark.E_Benchmark;
import org.aksw.jena_sparql_api.sparql.ext.benchmark.E_CompareResultSet;
import org.aksw.jena_sparql_api.sparql.ext.benchmark.E_NextLong;
import org.aksw.jena_sparql_api.sparql.ext.benchmark.PropertyFunctionFactoryBenchmark;
import org.aksw.jena_sparql_api.sparql.ext.benchmark.PropertyFunctionFactoryExecSelect;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

public class JenaExtensionSys {
    public static String ns = "http://jsa.aksw.org/fn/sys/";
    
    public static void register() {
    	
    	PropertyFunctionRegistry pfRegistry = PropertyFunctionRegistry.get();
		
    	pfRegistry.put(ns + "benchmark", new PropertyFunctionFactoryBenchmark());
    	pfRegistry.put(ns + "execSelect", new PropertyFunctionFactoryExecSelect());
    	

    	
        FunctionRegistry registry = FunctionRegistry.get();
        
        registry.put(ns + "getenv", E_Getenv.class);
        registry.put(ns + "benchmark", E_Benchmark.class);
        registry.put(ns + "nextLong", E_NextLong.class);
        registry.put(ns + "rscmp", E_CompareResultSet.class);
    }
    
    public static void addPrefixes(PrefixMapping pm) {
		pm.setNsPrefix("sys", ns);
    }
}
