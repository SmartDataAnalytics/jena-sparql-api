package org.aksw.jena_sparql_api.core;


/**
 * @author Claus Stadler
 *
 *
 *         Date: 7/23/11
 *         Time: 9:25 PM
 */
public interface QueryExecutionFactoryString {
    QueryExecutionStreaming createQueryExecution(String queryString);
}
