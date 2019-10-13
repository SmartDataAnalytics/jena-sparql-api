package org.aksw.jena_sparql_api.conjure.job.api;

import java.util.List;

import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.RdfTypeNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

/**
 * A macro denotes an unary operation on datasets.
 * macro(dataset) = dataset'
 * 
 * It is realized by an algebra expression with a single variable
 * 
 * Hence, the output of a macro primarily depends on the input dataset, however
 * its definition may contain be side-effects with the execution context
 * 
 * @author raven
 *
 */
@ResourceView
@RdfTypeNs("rpif")
public interface Macro
	extends Resource
{
	@IriNs("rpif")
	Op getDefiniton();
	Macro setDefinition(Op op);	

	@IriNs("rpif")
	List<MacroParam> getParams();
	void setParams(List<MacroParam> params);
	
	public static Macro create(Model model, Op definition) {
		Macro result = model.createResource().as(Macro.class)
			.setDefinition(definition);
			//.setVarName(varName);
	
		return result;
	}
}
