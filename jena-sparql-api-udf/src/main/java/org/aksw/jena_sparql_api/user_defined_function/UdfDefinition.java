package org.aksw.jena_sparql_api.user_defined_function;

import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.apache.jena.rdf.model.Resource;

public interface UdfDefinition
	extends Resource
{
	/**
	 * Function definitions can be associated with profiles.
	 * This allows loading a function if <b>any</b> of the profiles is active.
	 * (I.e. the set of profiles is disjunctive)
	 * 
	 * @return
	 */
	@Iri("http://ns.aksw.org/jena/udf/profile")
	Set<Resource> getProfiles();

//	<T> Set<T> getProfiles(Class<T> x);

	/**
	 * Definition that refers to another function for macro-expansion under the given profiles
	 * 
	 * @return
	 */
	@Iri("http://ns.aksw.org/jena/udf/aliasFor")
	UserDefinedFunctionResource getAliasFor();

	UdfDefinition setAliasFor(Resource r);
	

	/**
	 * True means, that the function is realized using a property function with the same name
	 * 
	 * 
	 * @return
	 */
	@Iri("http://ns.aksw.org/jena/udf/mapsToPropertyFunction")
	boolean mapsToPropertyFunction();

//	@Iri("http://ns.aksw.org/jena/udf/definition")
//	boolean mapsToPropertyFunction;

}


