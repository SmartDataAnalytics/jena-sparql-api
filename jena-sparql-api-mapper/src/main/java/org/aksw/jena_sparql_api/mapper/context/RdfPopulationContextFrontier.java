package org.aksw.jena_sparql_api.mapper.context;

import org.aksw.jena_sparql_api.mapper.model.RdfType;
import org.aksw.jena_sparql_api.util.frontier.Frontier;
import org.aksw.jena_sparql_api.util.frontier.FrontierStatus;

import com.hp.hpl.jena.graph.Node;

public class RdfPopulationContextFrontier
	implements RdfPopulationContext
{
	protected Frontier<PopulationRequest> frontier;

	public RdfPopulationContextFrontier(Frontier<PopulationRequest> frontier) {
		super();
		this.frontier = frontier;
	}

	@Override
	public Object objectFor(RdfType rdfType, Node node) {
		Object result;

		// Check if there is already a java object for the given class with the given id
		result = rdfType.createJavaObject(node);

		PopulationRequest pr = new PopulationRequest(rdfType, node);
		frontier.add(pr);

		return result;
	}

	public void checkManaged(Object bean) {
		if(!isManaged(bean)) {
			throw new RuntimeException("Bean was expected to be managed: " + bean);
		}
	}

	public boolean isManaged(Object bean) {
		FrontierStatus status = frontier.getStatus(bean);
		boolean result = !FrontierStatus.UNKNOWN.equals(status);
		return result;
	}


	/**
	 * Convenience accessors
	 *
	 * @param bean
	 * @return
	 */

	public boolean isPopulated(Object populationRequest) {
		boolean result = FrontierStatus.DONE.equals(frontier.getStatus(populationRequest));

		return result;
	}


}
