package org.aksw.jena_sparql_api.pagination.core;

import java.util.Iterator;

import com.hp.hpl.jena.query.Query;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 9/21/12
 *         Time: 4:11 PM
 */
public interface QueryTransformer
{
    public Iterator<Query> transform(Query query);
}
