package org.aksw.jena_sparql_api.core;


import com.hp.hpl.jena.query.Query;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/23/11
 *         Time: 9:30 PM
 */
public interface QueryExecutionFactoryQuery {
    QueryExecutionStreaming createQueryExecution(Query query);
}
