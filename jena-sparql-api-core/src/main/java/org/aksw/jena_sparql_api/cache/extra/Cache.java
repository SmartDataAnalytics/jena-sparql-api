package org.aksw.jena_sparql_api.cache.extra;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;


/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/23/11
 *         Time: 10:22 PM
 */
public interface Cache
{
    void write(String queryString, ResultSet resultSet);
    void write(Query query, ResultSet resultSet);

    void write(String queryString, Model model);
    void write(Query query, Model model);
    
    void write(String queryString, boolean value);
    void write(Query query, boolean value);

    CacheResource lookup(String queryString);
    CacheResource lookup(Query query);
}
