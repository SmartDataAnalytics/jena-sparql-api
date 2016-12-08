package org.aksw.jena_sparql_api.sparql.ext.term;

import org.apache.jena.sparql.function.FunctionRegistry;

public class JenaExtensionTerm {
    public static String termFn = "http://jsa.aksw.org/fn/term/";
    
    public static void register() {
        FunctionRegistry.get().put(termFn + "valid", E_TermValid.class);
    }
}
