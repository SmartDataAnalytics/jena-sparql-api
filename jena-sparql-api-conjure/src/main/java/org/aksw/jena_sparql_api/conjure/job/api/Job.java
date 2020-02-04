package org.aksw.jena_sparql_api.conjure.job.api;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.traversal.api.OpTraversal;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
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
	
//	@IriNs("rpif")
//	List<JobBinding> getVars();
//	Job setJobBindings(List<JobBinding> bindings);

	/**
	 * Explicitly declared variables for the job.
	 * Can be automatically derived from the description (TODO add link to util method),
	 * but an explicit description can be useful.
	 * 
	 * 
	 * @return
	 */
	@Iri("rpif:declaredVar")
	Set<String> getDeclaredVars();
	Job setDeclaredVars(Collection<String> varNames);

	@Iri("rpif:opVar")
	Set<String> getOpVars();
	Job setOpVars(Collection<String> varNames);

	
	
//	default Set<Var> getMentionedVars() {
//		throw new RuntimeException("Not implemented yet");
//	}
	

	/**
	 * Specification of a mapping from resource to literal in order to
	 * create binding of the job's variables to values based on a another resource
	 * 
	 * TODO Probably this should go to a higher level class that combines a job with a default binding
	 * 
	 * @return
	 */
	@IriNs("rpif")
	List<JobBinding> getJobBindings();
	Job setJobBindings(List<JobBinding> bindings);

	@IriNs("rpif")
	String getJobName();
	Job setJobName(String name);
	
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
	
	public static Job create(Model model, String jobName) {
		Job result = create(model)
				.setJobName(jobName);

		return result;
	}

}

