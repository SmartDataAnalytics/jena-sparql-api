package org.aksw.jena_sparql_api.conjure.job.api;

import java.util.List;

import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.traversal.api.OpTraversal;
import org.aksw.jena_sparql_api.conjure.traversal.api.OpTraversalSelf;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.PolymorphicOnly;
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
	@PolymorphicOnly
	@IriNs("rpif")
	Op getOp();
	Job setOp(Op op);
	
	@IriNs("rpif")
	List<JobBinding> getJobBindings();
	Job setJobBindings(List<JobBinding> bindings);

	default Job addJobBinding(String varName, OpTraversal traversal) {
		getJobBindings()
			.add(JobBinding.create(getModel(), varName, traversal));

		return this;
	}
	
//	public static Job create() {
//		Job result = create(ModelFactory.createDefaultModel());
//			//.setSubOp(subOp)
//			//.setQueryStrings(queryStrings);
//		
//		return result;
//	}

	public static Job create(Model model) {
		Job result = model.createResource().as(Job.class);
			//.setSubOp(subOp)
			//.setQueryStrings(queryStrings);
		
		return result;
	}
}

