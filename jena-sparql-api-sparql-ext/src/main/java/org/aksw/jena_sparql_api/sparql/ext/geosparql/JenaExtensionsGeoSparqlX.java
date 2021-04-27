package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.geosparql.implementation.vocabulary.GeoSPARQL_URI;
import org.apache.jena.sparql.function.FunctionRegistry;

public class JenaExtensionsGeoSparqlX {

	public static void register() {
		loadDefs(FunctionRegistry.get());
	}		

	
	public static void loadDefs(FunctionRegistry registry) {
        TypeMapper.getInstance().registerDatatype(RDFDatatypeWkbLiteral.INSTANCE);

        registry.put(GeoSPARQL_URI.GEOF_URI + "wkb2wkt", F_Wkb2Wkt.class);
        registry.put(GeoSPARQL_URI.GEOF_URI + "parsePolyline", F_ParsePolyline.class);
        
        
		// FunctionRegistry.get().put(ns + "nearestPoints", uri -> new E_ST_NearestPoints());		
	}
}
