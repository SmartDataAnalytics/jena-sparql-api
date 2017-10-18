package org.aksw.jena_sparql_api.sparql.ext.xml;

import org.aksw.jena_sparql_api.sparql.ext.json.PropertyFunctionFactoryJsonUnnest;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

public class JenaExtensionXml {
    public static String xmlFn = "http://jsa.aksw.org/fn/json/";
    
    public static void register() {
        FunctionRegistry.get().put(xmlFn + "path", E_XPath.class);
        //FunctionRegistry.get().put(jsonFn + "path", E_JsonPath.class);

        TypeMapper.getInstance().registerDatatype(new RDFDatatypeXml());
        
        PropertyFunctionRegistry.get().put(xmlFn + "unnest", new PropertyFunctionFactoryJsonUnnest());
    }
}
