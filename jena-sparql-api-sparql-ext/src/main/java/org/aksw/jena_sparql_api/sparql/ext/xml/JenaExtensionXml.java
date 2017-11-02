package org.aksw.jena_sparql_api.sparql.ext.xml;

import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.function.FunctionRegistry;

public class JenaExtensionXml {
    public static String xmlFn = "http://jsa.aksw.org/fn/xml/";
    
    public static void register() {
        FunctionRegistry.get().put(xmlFn + "path", E_XPath.class);
        //FunctionRegistry.get().put(jsonFn + "path", E_JsonPath.class);

        TypeMapper.getInstance().registerDatatype(new RDFDatatypeXml());
        
        //PropertyFunctionRegistry.get().put(xmlFn + "unnest", new PropertyFunctionFactoryJsonUnnest());
    }
    
    public static void addPrefixes(PrefixMapping pm) {
		pm.setNsPrefix("xml", JenaExtensionXml.xmlFn);
    }
}
