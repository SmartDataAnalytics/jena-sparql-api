package org.aksw.jena_sparql_api.cache.extra;

import java.util.Iterator;

import org.apache.jena.graph.Triple;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 11/28/11
 *         Time: 11:05 PM
 */

import org.apache.jena.query.Query;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/23/11
 *         Time: 10:22 PM
 */
public interface CacheFrontend
{
    void write(String service, String queryString, ResultSet resultSet);
    void write(String service, Query query, ResultSet resultSet);

    void write(String service, String queryString, Model model);
    void write(String service, Query query, Model model);

    void writeTriples(String service, String queryString, Iterator<Triple> it);
    void writeTriples(String service, Query query, Iterator<Triple> it);

    void write(String service, String queryString, boolean value);
    void write(String service, Query query, boolean value);

    CacheResource lookup(String service, String queryString);
    CacheResource lookup(String service, Query query);

    boolean isReadOnly();
}