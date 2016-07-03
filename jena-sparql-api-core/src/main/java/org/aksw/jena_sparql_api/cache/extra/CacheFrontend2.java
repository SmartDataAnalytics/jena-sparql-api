package org.aksw.jena_sparql_api.cache.extra;

import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

/**
 * More generic version of the service/query string based version
 *
 * @author Claus Stadler
 */
public interface CacheFrontend2<K>
{
    void write(K key, ResultSet resultSet);
    void write(K key, Model model);
    void write(K key, boolean value);

    CacheResource lookup(K key);
}