package org.aksw.jena_sparql_api.mapper.jpa.core;

import javax.persistence.EntityManager;

public interface RdfEntityManager
    extends EntityManager
{
    String getIri(Object entity);
}
