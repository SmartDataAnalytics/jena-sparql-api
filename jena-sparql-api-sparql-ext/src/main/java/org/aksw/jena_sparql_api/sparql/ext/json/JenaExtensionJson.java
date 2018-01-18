package org.aksw.jena_sparql_api.sparql.ext.json;

import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

public class JenaExtensionJson {
	public static String ns = "http://jsa.aksw.org/fn/json/";

	public static void register() {
		FunctionRegistry.get().put(ns + "parse", E_JsonParse.class);
		FunctionRegistry.get().put(ns + "path", E_JsonPath.class);

		TypeMapper.getInstance().registerDatatype(new RDFDatatypeJson());

		PropertyFunctionRegistry.get().put(ns + "unnest", new PropertyFunctionFactoryJsonUnnest());
	}

	public static void addPrefixes(PrefixMapping pm) {
		pm.setNsPrefix("json", ns);
	}
}
