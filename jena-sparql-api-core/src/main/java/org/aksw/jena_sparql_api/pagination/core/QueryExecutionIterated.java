package org.aksw.jena_sparql_api.pagination.core;

import java.util.Iterator;
import java.util.List;

import org.aksw.commons.collections.IClosable;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.utils.query_execution.QueryExecutionAdapter;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIteratorBase;
import org.apache.jena.sparql.engine.iterator.QueryIteratorCloseable;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.util.ModelUtils;
import org.apache.jena.sparql.util.QueryExecUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * A query execution that generates paginated result sets.
 * Note: Construct queries are NOT paginated.
 * (Because I don't see how a model can be paginated)
 *
 *
 *
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 7:59 PM
 */
public class QueryExecutionIterated
        extends QueryExecutionAdapter
{
    private static final Logger logger = LoggerFactory.getLogger(QueryExecutionIterated.class);

    //private ResultSetPaginated currentSelectExecution;

    protected QueryExecutionFactory factory;
    protected Iterator<Query> queryIterator;
    //private Query query;
    //private long pageSize;

    // If false, the whole query iterator will be consumed
    // (query iterators may be endless)
    protected boolean stopOnEmptyResult;
    protected boolean stopIfLimitNotReached;

    protected IClosable currentCloseAction = null;

    protected Query originalQuery;
    //private QueryExecution current;


//    synchronized void _setDecoratee(QueryExecution decoratee) {
//        super.setDecoratee(decoratee);
//    }

    public QueryExecutionIterated(
            Query originalQuery,
            QueryExecutionFactory factory,
            Iterator<Query> queryIterator) {
        this(originalQuery, factory, queryIterator, true, true);
    }

    public QueryExecutionIterated(
            Query originalQuery,
            QueryExecutionFactory factory,
            Iterator<Query> queryIterator,
            boolean stopOnEmptyResult,
            boolean stopIfLimitNotReached) {
        super();
        this.originalQuery = originalQuery;
        this.queryIterator = queryIterator;
        this.factory = factory;
        this.stopOnEmptyResult = stopOnEmptyResult;
        this.stopIfLimitNotReached = stopIfLimitNotReached;
    }

    @Override
    public Query getQuery() {
        return originalQuery;
    }

    @Override
    public boolean execAsk() {
        Query query = queryIterator.next();
        query.setLimit(Query.NOLIMIT);
        QueryExecution qe = factory.createQueryExecution(query);

        boolean result = qe.execAsk();
        return result;
    }

    @Override
    public ResultSet execSelect() {
        ResultSetPaginated it = new ResultSetPaginated(factory, queryIterator, stopOnEmptyResult, stopIfLimitNotReached);

        // Note: This line forces the iterator to initialize the result set...
        it.hasNext();


        // Also note, that hasNext() may return false if there are no result rows,
        // but still the result vars will be initialized

        /*
        if(!hasNext) {
            throw new RuntimeException("Error attempting to iterate a paginated result set");
        }
        */

        // ... which makes the set of resultVars available

        // TODO ISSUE Jena disallows getting ResultVars from a closed result set; and exhausting a result set auto-closes it...
        ResultSet tmp = it.getCurrentResultSet();
        if(tmp == null) {
            throw new RuntimeException("Underlying result set not avaliable - probably a query failed.");
        }

        List<String> resultVars =  it.getCurrentResultVars();//tmp.getResultVars();

        QueryIterator myQueryIterator = new MyQueryIteratorWrapper(it);
        QueryIteratorCloseable itClosable = new QueryIteratorCloseable(myQueryIterator, it);

        ResultSet rs = ResultSetFactory.create(itClosable, resultVars);

        return rs;
    }


    @Override
    public Model execConstruct() {
        return execConstruct(ModelFactory.createDefaultModel());
    }

    @Override
    public Iterator<Triple> execConstructTriples() {
        // TODO We need to implement jena's ClosableIterator or org.apache.jena.atlas.lib.Cloeable
        // to be compatible with jena's closing machinery
        return new PrefetchIteratorForJena<Triple>() {
            QueryExecution current = null;

            // A query execution created per page to check whether the query pattern yields no bindings
            // in which case execution can usually abort
            QueryExecution counter = null;

            @Override
            protected Iterator<Triple> prefetch() throws Exception {
                Iterator<Triple> r;

                if (current != null) {
                    current.close();
                }

                Query query = queryIterator.hasNext() ? queryIterator.next() : null;

                    // TODO We could count the result for the query's WHERE pattern
                    // and if it does not reach the limit we know we are on the last page
                    // However, this requires counting all items - so just checking for non-empty
                    // pages is much more efficient

//                  if (stopOnEmptyResult || stopIfLimitNotReached) {
//                      Query clone = query.cloneQuery();
  //
//                      Entry<Var, Query> e = QueryGenerationUtils.createQueryCount(clone);
//                      QueryExecution countQe = factory.createQueryExecution(e.getValue());
//                      //ServiceUtils.fetchNumber(countQe, e.getKey()).longValue()
//                      long count = ServiceUtils.fetchCountQuery(countQe);
//                  }

                if (query != null && stopOnEmptyResult) {
                    // Check whether the current query's WHERE pattern is non-empty
                    // Don't use ASK - it does not support LIMIT / OFFSET
                    // Just use the original query with a LIMIT 1 and check for a result
                    Query clone = query.cloneQuery();
                    clone.setQuerySelectType();

                    // The clone is derived from construct query therefore setting query result set
                    // cannot conflict with a specific projection / aggregators
                    clone.setQueryResultStar(true);
                    clone.setLimit(1);

//                        logger.trace("Checking where construct query has underlying bindings: " + clone);

                    boolean isEmpty;
                    // FIXME If abort() is invoked on the outer query execution
                    // we should delegate the call to the one we create here
                    try(QueryExecution qe = counter = factory.createQueryExecution(clone)) {
                        ResultSet rs = qe.execSelect();
                        isEmpty = !rs.hasNext();
                    }
                    counter = null;

                    if(isEmpty) {
                        query = null;
                    }
                }

                if (query != null) {
                    current = factory.createQueryExecution(query);
                     r = current.execConstructTriples();
                     currentCloseAction = () -> close();
                } else {
                    r = null;
                }
                return r;
            }

            @Override
            public void close() {
                try {
                    if (counter != null) {
                        counter.close();
                    }
                } finally {
                    if (current != null) {
                        current.close();
                    }
                }

            }
        };
    }

    @Override
    public Model execConstruct(Model result) {

        Iterator<Triple> it = execConstructTriples();
        StmtIterator iter = ModelUtils.triplesToStatements(it, result);
        result.add(iter);

        return result;

        //PaginationQueryIterator state = new PaginationQueryIterator(query, pageSize);

//        Query query;
//        try {
//            while(queryIterator.hasNext()) {
//                query = queryIterator.next();
//                final QueryExecution current = factory.createQueryExecution(query);
//
//                currentCloseAction = () -> current.close();
//
//                logger.trace("Executing query: " + query);
//                Model tmp = current.execConstruct();
//                if(tmp.isEmpty() && stopOnEmptyResult) {
//                    break;
//                }
//
//                result.add(tmp);
//                //System.out.println("Added | Size result = " + result.size() + ", tmp = " + tmp.size());
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }

    @Override
    public void close() {
        if(currentCloseAction != null) {
            currentCloseAction.close();
        }
        /*
        if(current != null) {
            current.close();
        }
        */
    }
}


class MyQueryIteratorWrapper
    extends QueryIteratorBase
{
    private Iterator<Binding> it;

    public MyQueryIteratorWrapper(Iterator<Binding> it) {
        this.it = it;
    }

    @Override
    protected boolean hasNextBinding() {
        return it.hasNext();
    }

    @Override
    protected Binding moveToNextBinding() {
        return it.next();
    }

    @Override
    protected void closeIterator() {

    }

    @Override
    protected void requestCancel() {

    }

    @Override
    public void output(IndentedWriter out, SerializationContext sCxt) {
    }

}