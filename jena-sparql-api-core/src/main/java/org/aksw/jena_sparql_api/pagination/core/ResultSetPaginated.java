package org.aksw.jena_sparql_api.pagination.core;


import java.util.Iterator;

import org.aksw.commons.collections.PrefetchIterator;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionStreaming;
import org.openjena.atlas.lib.Closeable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Query;
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

	private QueryExecutionFactory service;
	private QueryExecutionIterated execution;

    private Iterator<Query> queryIterator;
    private boolean stopOnEmptyResult = true;

    private ResultSet currentResultSet = null;

    /*
	public ResultSetPaginated(QueryExecutionIterated execution,QueryExecutionFactory service, Iterator<Query> queryIterator) {
		this(execution, service, QueryFactory.create(queryString),);
	}
	*/

	public ResultSetPaginated(QueryExecutionIterated execution, QueryExecutionFactory service, Iterator<Query> queryIterator, boolean stopOnEmptyResult) {
		this.execution = execution;
        this.service = service;
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
                //return null;
            }

            QueryExecutionStreaming qe = service.createQueryExecution(query);

            if(execution != null) {
                execution._setDecoratee(qe);
            }

            //QueryIteratorCloseable
            logger.trace("Executing: " + query);
            currentResultSet = qe.execSelect();
            if(!currentResultSet.hasNext()) {
                if(stopOnEmptyResult) {
                    return null;
                } else {
                    continue;
                }
            }

            return new QueryIteratorResultSet(currentResultSet);
        }

        return null;
	}

    @Override
    public void close() {
        if(execution != null) {
            execution.close();
        }
    }
}

