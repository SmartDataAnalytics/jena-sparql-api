package org.aksw.jena_sparql_api.sparql.ext.fs;

import org.apache.jena.query.ARQ;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

public class JenaExtensionFs {
    public static String ns = "http://jsa.aksw.org/fn/fs/";
    
    public static void register() {
        FunctionRegistry.get().put(ns + "rdfLang", E_RdfLang.class);
        FunctionRegistry.get().put(ns + "probeRdf", E_ProbeRdf.class);

        
		PropertyFunctionRegistry.get().put(ns + "find", new PropertyFunctionFactoryFsFind());
    }
    
    public static void addPrefixes(PrefixMapping pm) {
		pm.setNsPrefix("fs", ns);
    }

    // Better not register the handler automatically; it is a quite intrusive deed
    public static void registerFileServiceHandler() {
        QC.setFactory(ARQ.getContext(), execCxt -> new OpExecutorServiceOrFile(execCxt));
    }
}