package org.aksw.jena_sparql_api.mapper.impl.engine;

import org.aksw.jena_sparql_api.mapper.context.RdfPersistenceContext;

public interface PersistenceContextSupplier {
	RdfPersistenceContext getPersistenceContext();
}
