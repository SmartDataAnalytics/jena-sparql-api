package org.aksw.jena_sparql_api.mapper.context;


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
		boolean result = frontier.isDone(entity);
		return result;
	}

	public void setEmitted(Object entity, boolean status) {
		frontier.makeDone(entity);
	}
}
