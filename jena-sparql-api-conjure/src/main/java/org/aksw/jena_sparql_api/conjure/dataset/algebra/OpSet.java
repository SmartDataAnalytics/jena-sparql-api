package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import java.util.List;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.RdfTypeNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Model;

/**
 * Set a variable in the execution context.
 * A select query with a single projection variable acts as the selector for values.
 * For convenience, a property path can be used to navigate to a
 * related set of resources.
 * Empty or null property path is treated as identity
 * 
 * var = ("SELECT ?s { ?s someProp ?x }", rdfs:label)
 * 
 * @author raven
 *
 */
@ResourceView
@RdfTypeNs("rpif")
public interface OpSet
	extends Op1
{
	@IriNs("rpif")
	String getCtxVarName();
	OpSet setCtxVarName(String string);

	/**
	 * Selector is a select query returning a single variable, such as
	 * SELECT ?x { ?s a ?x }

	 * @return
	 */
	@IriNs("rpif")
	String getSelector();
	OpSet setSelector(String string);

	@IriNs("rpif")
	String getPropertyPath();
	OpSet setPropertyPath(String str);

	@IriNs("rpif")
	String getSelectorVarName();
	OpSet setSelectorVarName(String str);

	@IriNs("rpif")
	String getPath();
	OpSet setPath(String str);

	@Override
	OpSet setSubOp(Op subOp);
	
	@Override
	default <T> T accept(OpVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	@Override
	default OpSet clone(Model cloneModel, List<Op> subOps) {
		return this.inModel(cloneModel).as(OpSet.class)
				.setSubOp(subOps.iterator().next())
				.setSubOp(getSubOp())
				.setCtxVarName(getCtxVarName())
				.setSelectorVarName(getSelectorVarName())
				.setSelector(getSelector())
				.setPropertyPath(getPath());
	}

//	public static OpSet create(Model model, Op subOp, String queryString) {
//		OpSet result = model.createResource().as(OpSet.class)
//				.setSubOp(subOp);
//		
//		return result;
//	}
	
	public static OpSet create(Model model, Op subOp, String ctxVarName, String selectorVarName, String selector, String path) {
		OpSet result = model.createResource().as(OpSet.class)
			.setSubOp(subOp)
			.setCtxVarName(ctxVarName)
			.setSelectorVarName(selectorVarName)
			.setSelector(selector)
			.setPropertyPath(path);
		
		return result;
	}
}