package org.aksw.jena_sparql_api.user_defined_function;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.function.user.UserDefinedFunctionDefinition;
import org.apache.jena.sparql.util.ExprUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface UdfDefinition
	extends Resource
{
	@Iri("http://ns.aksw.org/jena/udf/prefixMapping")
	PrefixSet getPrefixSet();
	
	default PrefixMapping addTo(PrefixMapping pm) {
		PrefixSet ps = getPrefixSet();
		if(ps != null) {
			ps.addTo(pm);
		}
		
		return pm;
	}
	
	@Iri("http://ns.aksw.org/jena/udf/expr")
	String getExpr();
	
	@Iri("http://ns.aksw.org/jena/udf/params")
	List<String> getParams();
	
	/**
	 * Function definitions can be associated with profiles.
	 * This allows loading a function if <b>any</b> of the profiles is active.
	 * (I.e. the set of profiles is disjunctive)
	 * 
	 * @return
	 */
	@Iri(UdfVocab.Strs.profile)
	Set<Resource> getProfiles();

	@Iri("http://ns.aksw.org/jena/udf/inverse")
	Set<InverseDefinition> getInverses();

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

	public static UserDefinedFunctionDefinition toJena(String iri, UdfDefinition r) {
//		System.out.println(r);
//		if(!r.isURIResource()) {
//			throw new RuntimeException("Function definitions must be URI resources");
//		}
		
//		String uri = r.getURI();

		PrefixMapping pm = new PrefixMappingImpl();
		r.addTo(pm);
		
		Logger logger = LoggerFactory.getLogger(UdfDefinition.class);
		
		logger.debug("Processing user defined function definition: " + iri + ": " + pm);
		
		List<String> paramsStr = r.getParams();
		List<Var> params = paramsStr.stream()
				.map(Var::alloc)
				.collect(Collectors.toList());
		String exprStr = r.getExpr();
		
//		List<String> parts = r.getSimpleDefinition();
//		if(parts.size() < 1) {
//			throw new RuntimeException("Function definition requires at least one expression");
//		}
//
//		Iterator<String> it = parts.iterator();
//		String defStr = it.next();
		Expr e = ExprUtils.parse(exprStr, pm);
		
		UserDefinedFunctionDefinition result = new UserDefinedFunctionDefinition(iri, e, params);
		return result;		
	}

}


