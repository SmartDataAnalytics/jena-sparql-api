package org.aksw.jena_sparql_api.utils.views.map;

import java.util.Map;

import org.apache.jena.rdf.model.Resource;

public interface RdfMap<K, V extends Resource>
    extends Map<K, V>
{
    Resource allocate(K key);
}
