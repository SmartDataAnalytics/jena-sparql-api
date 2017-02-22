package org.aksw.jena_sparql_api.mapper.jpa.criteria;

import javax.persistence.criteria.Path;

public interface CriteriaEnv {
	<T> Path<T> createPath(Class<T> cls);
}
