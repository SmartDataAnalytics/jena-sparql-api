package org.aksw.jena_sparql_api.core;

import com.hp.hpl.jena.query.Query;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/23/11
 *         Time: 9:44 PM
 */
public abstract class QueryExecutionFactoryBackString
    implements QueryExecutionFactory
{
    @Override
    public QueryExecutionStreaming createQueryExecution(Query query) {
        return createQueryExecution(query.toString());
    }
}
