package org.aksw.jena_sparql_api.user_defined_function;

import java.util.List;
import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.PolymorphicOnly;
import org.apache.jena.rdf.model.Resource;

public interface UserDefinedFunctionResource
    extends Resource
{
    /**
     * Get a simple definition of the function in form of a list of strings.
     * The first item is the SPARQL expression string whereas the remaining elements are the parameter
     * variable names.
     *
     * @return
     */
    @Iri("http://ns.aksw.org/jena/udf/simpleDefinition")
    List<String> getSimpleDefinition();


    @Iri("http://ns.aksw.org/jena/udf/definition")
    Set<UdfDefinition> getDefinitions();

//	public default UserDefinedFunctionDefinition toJena() {
//		UserDefinedFunctionDefinition result = toJena(this);
//		return result;
//	}
}
