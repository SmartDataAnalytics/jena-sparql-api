package org.aksw.jena_sparql_api.pagination.core;

import java.util.Iterator;
import java.util.List;

import org.aksw.commons.collections.IClosable;
import org.aksw.jena_sparql_api.core.QueryExecutionAdapter;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.atlas.io.IndentedWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIteratorBase;
import org.apache.jena.sparql.engine.iterator.QueryIteratorCloseable;
import org.apache.jena.sparql.serializer.SerializationContext;



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

    protected IClosable currentCloseAction = null;

    protected Query originalQuery;
    //private QueryExecution current;


//    synchronized void _setDecoratee(QueryExecution decoratee) {
//        super.setDecoratee(decoratee);
//    }

    public QueryExecutionIterated(Query originalQuery, QueryExecutionFactory factory, Iterator<Query> queryIterator) {
        this(originalQuery, factory, queryIterator, true);
    }

    public QueryExecutionIterated(Query originalQuery, QueryExecutionFactory factory, Iterator<Query> queryIterator, boolean stopOnEmptyResult) {
        super();
        this.originalQuery = originalQuery;
        this.queryIterator = queryIterator;
        this.factory = factory;
        this.stopOnEmptyResult = stopOnEmptyResult;
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
        ResultSetPaginated it = new ResultSetPaginated(factory, queryIterator, stopOnEmptyResult);

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
    public Model execConstruct(Model result) {
        //PaginationQueryIterator state = new PaginationQueryIterator(query, pageSize);

        Query query;
        try {
            while(queryIterator.hasNext()) {
                query = queryIterator.next();
                final QueryExecution current = factory.createQueryExecution(query);

                currentCloseAction = new IClosable() {
                    @Override
                    public void close() {
                        current.close();
                    }
                };

                logger.trace("Executing query: " + query);
                Model tmp = current.execConstruct();
                if(tmp.isEmpty() && stopOnEmptyResult) {
                    break;
                }

                result.add(tmp);
                //System.out.println("Added | Size result = " + result.size() + ", tmp = " + tmp.size());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
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