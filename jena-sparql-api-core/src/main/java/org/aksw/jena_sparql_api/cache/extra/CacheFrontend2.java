package org.aksw.jena_sparql_api.cache.extra;

import java.util.concurrent.Future;

import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

/**
 * More generic version of the service/query string based version
 *
 * @author Claus Stadler
 */
public interface CacheFrontend2<K>
{
    Future<CacheResource> write(K key, ResultSet resultSet);
    Future<CacheResource> write(K key, Model model);
    Future<CacheResource> write(K key, boolean value);

    CacheResource lookup(K key);
}