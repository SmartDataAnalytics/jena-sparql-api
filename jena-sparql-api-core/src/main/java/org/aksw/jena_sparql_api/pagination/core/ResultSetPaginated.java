package org.aksw.jena_sparql_api.pagination.core;


import java.util.Iterator;

import org.aksw.commons.collections.IClosable;
import org.aksw.commons.collections.PrefetchIterator;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.ResultSetClosable;
import org.apache.jena.atlas.lib.Closeable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorResultSet;

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
    private static Logger logger = LoggerFactory.getLogger(ResultSetPaginated.class);

	private QueryExecutionFactory serviceFactory;
	//private QueryExecutionIterated execution;

    private Iterator<Query> queryIterator;
    private boolean stopOnEmptyResult = true;

    private ResultSet currentResultSet = null;
    private QueryExecution currentExecution = null;
    
    /*
	public ResultSetPaginated(QueryExecutionIterated execution,QueryExecutionFactory service, Iterator<Query> queryIterator) {
		this(execution, service, QueryFactory.create(queryString),);
	}
	*/

	public ResultSetPaginated(QueryExecutionFactory service, Iterator<Query> queryIterator, boolean stopOnEmptyResult) {
		//this.execution = execution;
        this.serviceFactory = service;
		//this.state = new PaginationQueryIterator(query, pageSize);
        this.queryIterator = queryIterator;
	}

    public ResultSet getCurrentResultSet() {
        return currentResultSet;
    }

	@Override
	protected QueryIteratorResultSet prefetch() throws Exception {
        while(queryIterator.hasNext()) {
            
            Query query = queryIterator.next();
            if(query == null) {
                throw new RuntimeException("Null query encountered in iterator");
            }

            final QueryExecution qe = serviceFactory.createQueryExecution(query);

            logger.trace("Executing: " + query);

            // TODO Virtuoso sometimes yields invalid XML (probably due to encoding issues) and thus execSelect fails
            // Should this happen, we could try to recover by
            // doing a binary partitioning of the current query range, and try to locate the bindings causing the error
            currentResultSet = qe.execSelect();
            
            
            
            currentResultSet = new ResultSetClosable(currentResultSet, new IClosable() {
                @Override
                public void close() {
                    qe.close();
                }
            });
            
            if(!currentResultSet.hasNext()) {
                if(stopOnEmptyResult) {
                    break;
                }
                
                continue;
            }

            return new QueryIteratorResultSet(currentResultSet);
        }

        return null;
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

