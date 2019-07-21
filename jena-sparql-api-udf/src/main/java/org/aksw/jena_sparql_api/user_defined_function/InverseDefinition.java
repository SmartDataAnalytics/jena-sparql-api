package org.aksw.jena_sparql_api.user_defined_function;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.apache.jena.rdf.model.Resource;

public interface InverseDefinition
	extends Resource
{
	@Iri("http://ns.aksw.org/jena/udf/forParam")
	String getForParam();
	InverseDefinition setForParam(String param);
	
	@Iri("http://ns.aksw.org/jena/udf/fn")
	UserDefinedFunctionResource getFunction();
	InverseDefinition setFunction(Resource fn);

}
