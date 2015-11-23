package org.aksw.jena_sparql_api.mapper.context;

import org.aksw.jena_sparql_api.util.frontier.Frontier;
import org.aksw.jena_sparql_api.util.frontier.FrontierImpl;
import org.aksw.jena_sparql_api.util.frontier.FrontierStatus;

public class RdfEmitterContextFrontier
	implements RdfEmitterContext
{
	protected Frontier<? super Object> frontier;

	public RdfEmitterContextFrontier() {
		this(FrontierImpl.createIdentityFrontier());
	}

	public RdfEmitterContextFrontier(Frontier<? super Object> frontier) {
		this.frontier = frontier;
	}

	@Override
	public void add(Object bean, Object parentBean, String propertyName) {
		frontier.add(bean);
	}

	public boolean isEmitted(Object entity) {
		boolean result = FrontierStatus.DONE.equals(frontier.getStatus(entity));
		return result;
	}

	public void setEmitted(Object entity, boolean status) {
		frontier.setStatus(entity, FrontierStatus.DONE);
	}
}
