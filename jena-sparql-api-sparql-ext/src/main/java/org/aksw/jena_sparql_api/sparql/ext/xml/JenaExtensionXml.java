package org.aksw.jena_sparql_api.sparql.ext.xml;

import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

public class JenaExtensionXml {
    public static String ns = "http://jsa.aksw.org/fn/xml/";
    
    public static void register() {
        FunctionRegistry.get().put(ns + "path", E_XPath.class);

        TypeMapper.getInstance().registerDatatype(new RDFDatatypeXml());
        
		PropertyFunctionRegistry.get().put(ns + "unnest", new PropertyFunctionFactoryXmlUnnest());
    }
    
    public static void addPrefixes(PrefixMapping pm) {
		pm.setNsPrefix("xml", ns);
    }
}
