package org.aksw.jena_sparql_api.limit;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDecorator;
import org.aksw.jena_sparql_api.core.QueryExecutionStreaming;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;

/**
 * A query execution that sets a limit on all queries
 *
 * @author Claus Stadler
 *         <p/>
 *         Date: 11/19/11
 *         Time: 11:33 PM
 */
public class QueryExecutionFactoryLimit
    extends QueryExecutionFactoryDecorator
{
    private Long limit;
    private boolean doCloneQuery = false;

    public static <U extends QueryExecution> QueryExecutionFactoryLimit decorate(QueryExecutionFactory decoratee, boolean doCloneQuery, Long limit) {
        return new QueryExecutionFactoryLimit(decoratee, doCloneQuery, limit);
    }

    public QueryExecutionFactoryLimit(QueryExecutionFactory decoratee, boolean doCloneQuery, Long limit) {
        super(decoratee);
        this.limit = limit;
    }

    public QueryExecutionStreaming createQueryExecution(Query query) {
        if(limit != null) {
            if(query.getLimit() == Query.NOLIMIT) {
                if(doCloneQuery) {
                    query = query.cloneQuery();
                }

                query.setLimit(limit);
            } else {
                long adjustedLimit = Math.min(limit, query.getLimit());

                if(adjustedLimit != query.getLimit()) {
                    if(doCloneQuery) {
                        query = query.cloneQuery();
                    }

                    query.setLimit(adjustedLimit);
                }
            }
        }

        return super.createQueryExecution(query);
    }

    @Override
    public QueryExecutionStreaming createQueryExecution(String queryString) {
    	Query query = QueryFactory.create(queryString);
    	QueryExecutionStreaming result = createQueryExecution(query);
        return result;
    }

}
