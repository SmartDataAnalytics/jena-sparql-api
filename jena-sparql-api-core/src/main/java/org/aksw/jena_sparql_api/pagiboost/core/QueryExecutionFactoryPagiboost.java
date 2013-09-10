package org.aksw.jena_sparql_api.pagiboost.core;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryBackQuery;
import org.aksw.jena_sparql_api.core.ResultSetClose;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;


class ResultSetLimited
	extends ResultSetClose
{
	private long limit;

	private int offset;
	
	public ResultSetLimited(ResultSet decoratee, long limit) {
		super(decoratee, decoratee.hasNext());
		offset = decoratee.getRowNumber();
		
		this.limit = limit;
	}

	@Override
	protected boolean checkClose() {

		long rowNumber = decoratee.getRowNumber(); 
		long pos = rowNumber - offset;
		
		if(pos >= limit) {
			close();
		}
		
		boolean result = super.checkClose();
		return result;
	}
}



class QueryExecutionPagiboost
//	extends QueryExecutionBaseSelect
{
	private QueryExecutionFactory qef;
	
	
	/*
	private long offset;
	private long limit;
	*/
	
	private long start;
	private long length;

	
	public QueryExecutionPagiboost(QueryExecutionFactory qe) {
		
	}

	//@Override
	protected QueryExecution executeCoreSelectX(Query query) {
		// TODO Auto-generated method stub
		return null;
	}
	
}



/**
 * Expand the page size of any incoming query.
 * 
 * 
 * 
 * 
 * @author raven
 *
 */
public class QueryExecutionFactoryPagiboost
	extends QueryExecutionFactoryBackQuery
{
	/**
	 * If an underlying QueryExecutionPaginate is used, the
	 * pageExpandSize should match up. 
	 * 
	 * 
	 */
	private QueryExecutionFactory qef;
	private long pageExpandSize;

	@Override
	public String getId() {
		return qef.getId();
	}

	@Override
	public String getState() {
		return qef.getState();
	}

	
	public QueryExecutionFactoryPagiboost(QueryExecutionFactory qef, long pageExpandSize) {
		this.qef = qef;
		this.pageExpandSize = pageExpandSize;
	}
	

	@Override
	public QueryExecution createQueryExecution(Query query) {
		Query q = query.cloneQuery();

		long offset = q.getOffset() == Query.NOLIMIT ? 0 : q.getOffset();
		long limit = q.getLimit();
		
		long o = (offset / pageExpandSize) * pageExpandSize;

		long l;
		if(limit != Query.NOLIMIT) {
			long target = offset + limit;
	
			long t = ((target / pageExpandSize) + 1) * pageExpandSize;
			l = t - o;
			
		} else {
			l = Query.NOLIMIT;
		}

		long start = o - offset;		
		
		// Align offset and target to pageExpandSize boundaries
		
		q.setOffset(o);
		q.setLimit(l);
		
		QueryExecution qe = qef.createQueryExecution(q);
		
		//QueryExecutionRange result = new QueryExecutionRange(qe, start, l);
		QueryExecution result = null;
		

		return result;
	}
	
	

	
}
