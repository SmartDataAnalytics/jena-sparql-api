package org.aksw.jena_sparql_api.sparql.ext.gml;

import org.apache.jena.sparql.function.FunctionRegistry;

public class JenaExtensionGml {
  public static String ns = "http://jsa.aksw.org/fn/gml/";

  public static void register() {
    FunctionRegistry.get().put(ns + "gml2Wkt", E_Gml2Wkt.class);
  }
}
