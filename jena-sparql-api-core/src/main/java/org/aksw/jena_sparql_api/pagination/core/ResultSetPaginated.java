package org.aksw.jena_sparql_api.pagination.core;


import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.aksw.commons.collections.PrefetchIterator;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.ResultSetCloseable;
import org.aksw.jena_sparql_api.utils.CloseableQueryExecution;
import org.apache.jena.atlas.lib.Closeable;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIteratorResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
class ConstructPaginated
    extends PrefetchIterator<Statement>
{
    private Sparqler sparqler;
    private PaginationQueryIterator state;

    public ConstructPaginated(Sparqler sparqler, String queryString, long pageSize) {
        this(sparqler, QueryFactory.create(queryString), pageSize);
    }

    public ConstructPaginated(Sparqler sparqler, Query query, long pageSize) {
        this.sparqler = sparqler;
        this.state = new PaginationQueryIterator(query, pageSize);
    }

    @Override
    protected Iterator<Statement> prefetch() throws Exception {
        Query query = state.next();
        if(query == null) {
            return null;
        }

        Model model = ModelFactory.createDefaultModel();
        return sparqler.executeConstruct(model, query).listStatements();
    }
}
*/

public class ResultSetPaginated
    extends PrefetchIterator<Binding>
    implements Closeable
{
    private static final Logger logger = LoggerFactory.getLogger(ResultSetPaginated.class);

    protected QueryExecutionFactory serviceFactory;
    //private QueryExecutionIterated execution;

    protected Iterator<Query> queryIterator;
    protected boolean stopOnEmptyResult = true;

    // Stop if a query returned by the queryIterator has a limit but its result set is less than that
    protected boolean stopIfLimitNotReached = true;

    protected ResultSet currentResultSet = null;
    protected QueryExecution currentExecution = null;

    // A cache of the resultVars of the current resultSet
    // This is needed, because on empty result sets we close the underyling result set immediately before we
    // have a chance to access its metadata (i.e. the result vars)
    // Jene even raises an exception when calling getResultVars() on a closed result set.
    protected List<String> currentResultVars = null;

    protected long lastExpectedResultSetSize = Query.NOLIMIT;
    protected long lastSeenResultSetSize = 0;

    /*
    public ResultSetPaginated(QueryExecutionIterated execution,QueryExecutionFactory service, Iterator<Query> queryIterator) {
        this(execution, service, QueryFactory.create(queryString),);
    }
    */

    public ResultSetPaginated(QueryExecutionFactory service, Iterator<Query> queryIterator, boolean stopOnEmptyResult, boolean stopIfLimitNotReached) {
        super();
        this.serviceFactory = service;
        this.queryIterator = queryIterator;
        this.stopOnEmptyResult = stopOnEmptyResult;
        this.stopIfLimitNotReached = stopIfLimitNotReached;
    }

    public ResultSet getCurrentResultSet() {
        return currentResultSet;
    }

    public List<String> getCurrentResultVars() {
        return currentResultVars;
    }


    // Cache of the rowNumber of currentResultSet;
    protected long lastSeenRowNumber = -1;

    @Override
    protected QueryIteratorResultSet prefetch() throws Exception {
        QueryIteratorResultSet result = null;

        // long lastSeenRowNumber = currentResultSet == null ? -1 : currentResultSet.getRowNumber();

        // If the last iterator we returned had fewer results than a given limit on the query we can abort the iteration
        if(stopIfLimitNotReached &&
                (lastExpectedResultSetSize != Query.NOLIMIT &&
                currentResultSet != null && lastSeenRowNumber < lastExpectedResultSetSize)) {
            result = null;
        } else {

            while(queryIterator.hasNext()) {

                Query query = queryIterator.next();
                if(query == null) {
                    throw new RuntimeException("Null query encountered in iterator");
                }

                // Abort early feature: If the result set size is less than a givin limit, we can abort (without having to wait for an
                // empty result set)
                // As a side effect, this causes pagination configured with a too large page size to exit immediatly
                lastExpectedResultSetSize = query.getLimit();

                final QueryExecution qe = serviceFactory.createQueryExecution(query);

                logger.trace("Executing: " + query);

                // TODO Virtuoso sometimes yields invalid XML (probably due to encoding issues) and thus execSelect fails
                // Should this happen, we could try to recover by
                // doing a binary partitioning of the current query range, and try to locate the bindings causing the error
                currentResultSet = qe.execSelect();

                //currentResultVars = new ArrayList<String>(currentResultSet.getResultVars());
                currentResultVars = currentResultSet.getResultVars();


                currentResultSet = new ResultSetCloseable(currentResultSet, new CloseableQueryExecution(qe)) {
                    @Override
                    public void close() throws IOException {
                        // Save the value of getRowNumber;
                        // After close getRowNumber raises an exception
                        lastSeenRowNumber = getRowNumber();
                        super.close();
                    }
                };

                if(!currentResultSet.hasNext()) {
                    if(stopOnEmptyResult) {
                        break;
                    }

                    continue;
                }

                result = new QueryIteratorResultSet(currentResultSet);
                break;
            }
        }

        return result;
    }

    @Override
    public void close() {
        if(currentExecution != null) {
            currentExecution.close();
        }
        /*
        if(execution != null) {
            execution.close();
        }
        */
    }
}

