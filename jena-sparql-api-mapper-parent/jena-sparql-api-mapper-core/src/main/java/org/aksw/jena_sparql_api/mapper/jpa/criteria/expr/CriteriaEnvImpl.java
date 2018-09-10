package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;

import javax.persistence.criteria.Path;

import org.aksw.jena_sparql_api.mapper.jpa.criteria.CriteriaEnv;

public class CriteriaEnvImpl
	implements CriteriaEnv
{

	@Override
	public <T> Path<T> createPath(Class<T> cls) {
		return new PathImpl(null, null, cls);
	}

}
