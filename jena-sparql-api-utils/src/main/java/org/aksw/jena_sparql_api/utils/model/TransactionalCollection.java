package org.aksw.jena_sparql_api.utils.model;

import java.util.Collection;

import org.apache.jena.sparql.core.Transactional;

public interface TransactionalCollection<T>
    extends Transactional, Collection<T>
{
}