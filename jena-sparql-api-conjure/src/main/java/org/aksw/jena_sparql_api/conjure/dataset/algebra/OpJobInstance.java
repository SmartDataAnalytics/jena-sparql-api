package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import org.aksw.jena_sparql_api.conjure.job.api.JobInstance;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.RdfTypeNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * Pseudo operation that specifies substitutions
 * (expressed as changes in the execution context)
 * for the sub operations
 * 
 * @author raven
 *
 */
@ResourceView
@RdfTypeNs("rpif")
public interface OpJobInstance
	extends Op0
{
	@IriNs("rpif")
	JobInstance getJobInstance();
	OpJobInstance setJobInstance(JobInstance jobInstance);
	
	
	@Override
	default <T> T accept(OpVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	public static OpJobInstance create(Model model, JobInstance jobInstance) {
//		Model model = subOps.size() > 0 ? subOps.get(0).getModel() : ModelFactory.createDefaultModel();
		// Model model = ModelFactory.createDefaultModel();
		model = model != null ? model : ModelFactory.createDefaultModel();

		OpJobInstance result = model.createResource().as(OpJobInstance.class)
			.setJobInstance(jobInstance);
		
		return result;
	}
}
