package org.aksw.jena_sparql_api.arq.core.query;


import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/23/11
 *         Time: 9:30 PM
 */
public interface QueryExecutionFactoryQuery {
    QueryExecution createQueryExecution(Query query);
}
