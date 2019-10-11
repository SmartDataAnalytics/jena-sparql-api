package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

/**
 * A Job corresponds to a Job in spring / java batch terminology
 * 
 * It is comprised of
 * - an operator expression
 * - a binding - a specification for how to process an input resource into job parameters
 * 
 * @author raven
 *
 */
@ResourceView
@RdfType("rpif:Job")
public interface Job
	extends Resource
{
	@IriNs("rpif")
	Op getOp();
	Job setOp(Op op);
	
	@IriNs("rpif")
	JobBinding getBinding();
	Job setJobBinding(JobBinding binding);


	public static Job create(Model model) {
		Job result = model.createResource().as(Job.class);
			//.setSubOp(subOp)
			//.setQueryStrings(queryStrings);
		
		return result;
	}
}

