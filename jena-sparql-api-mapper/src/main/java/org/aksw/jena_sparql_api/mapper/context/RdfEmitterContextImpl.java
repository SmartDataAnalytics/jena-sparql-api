package org.aksw.jena_sparql_api.mapper.context;


public class RdfEmitterContextImpl
	implements RdfEmitterContext
{
	protected EntityContext<? super Object> entityContext;

	public RdfEmitterContextImpl() {
		this(EntityContextImpl.createIdentityContext(Object.class));
	}

	public RdfEmitterContextImpl(EntityContext<? super Object> entityContext) {
		this.entityContext = entityContext;
	}

	@Override
	public void add(Object bean, Object parentBean, String propertyName) {
		//Map<String, Object> map = entityContext.getOrCreate(bean);
		if(!isEmitted(bean)) {
			setEmitted(bean, false);
		}

		// TODO We could keep track of who referenced the bean


	}

	public boolean isEmitted(Object entity) {
		boolean result = entityContext.getAttribute(entity, "isEmitted", false);
		return result;
	}

	public void setEmitted(Object entity, boolean status) {
		entityContext.setAttribute(entity, "isEmitted", status);
	}

}
