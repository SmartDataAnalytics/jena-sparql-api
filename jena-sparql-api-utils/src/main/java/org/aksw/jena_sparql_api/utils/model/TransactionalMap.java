package org.aksw.jena_sparql_api.utils.model;

import java.util.Map;

import org.apache.jena.sparql.core.Transactional;

public interface TransactionalMap<K, V>
    extends Map<K, V>, Transactional
{

}
