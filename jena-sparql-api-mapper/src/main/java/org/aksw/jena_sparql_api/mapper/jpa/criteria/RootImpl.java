package org.aksw.jena_sparql_api.mapper.jpa.criteria;

import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

public class RootImpl<X>
	extends FromImpl<X, X>
    implements Root<X>
{
	@Override
	public EntityType<X> getModel() {
		return null;
		//return super.getModel();
	}
}
