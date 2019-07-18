package org.aksw.jena_sparql_api.user_defined_function;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.function.user.UserDefinedFunctionDefinition;
import org.apache.jena.sparql.util.ExprUtils;

import com.google.common.collect.Streams;

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

	public default UserDefinedFunctionDefinition toJena() {
		UserDefinedFunctionDefinition result = toJena(this);
		return result;
	}

	public static UserDefinedFunctionDefinition toJena(UserDefinedFunctionResource r) {
		System.out.println(r);
		if(!r.isURIResource()) {
			throw new RuntimeException("Function definitions must be URI resources");
		}
		
		String uri = r.getURI();

		List<String> parts = r.getSimpleDefinition();
		if(parts.size() < 1) {
			throw new RuntimeException("Function definition requires at least one expression");
		}

		Iterator<String> it = parts.iterator();
		String defStr = it.next();
		Expr e = ExprUtils.parse(defStr);

		List<Var> paramList = Streams.stream(it)
				.map(Var::alloc)
				.collect(Collectors.toList());
		
		
		UserDefinedFunctionDefinition result = new UserDefinedFunctionDefinition(uri, e, paramList);
		return result;		
	}
}
