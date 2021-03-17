package org.aksw.jena_sparql_api.core;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 12:53 PM
 */
public class QueryExecutionFactoryDecorator
    extends QueryExecutionFactoryDecoratorBase<QueryExecutionFactory>
{
    public QueryExecutionFactoryDecorator(QueryExecutionFactory decoratee) {
        super(decoratee);
    }
}

