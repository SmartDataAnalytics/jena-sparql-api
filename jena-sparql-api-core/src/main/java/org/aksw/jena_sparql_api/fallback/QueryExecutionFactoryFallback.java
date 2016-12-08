package org.aksw.jena_sparql_api.fallback;


import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryBackQuery;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;

import com.google.common.collect.Lists;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;

public class QueryExecutionFactoryFallback
	extends QueryExecutionFactoryBackQuery
{
	private List<QueryExecutionFactory> decoratees;

	public QueryExecutionFactoryFallback(PriorityQueue<QueryExecutionFactory> decoratees) {
		this.decoratees = Lists.newArrayList(decoratees);
	}
	
	public QueryExecutionFactoryFallback(List<QueryExecutionFactory> decoratees) {
		this.decoratees = decoratees;
	}

	@Override
	public QueryExecution createQueryExecution(Query query) {
		List<QueryExecution> queryExecutions = new ArrayList<QueryExecution>(decoratees.size());
		for (QueryExecutionFactory decoratee : decoratees) {
			QueryExecution qe = decoratee.createQueryExecution(query);
			queryExecutions.add(qe);
		}
		return new QueryExecutionFallback(queryExecutions);
	}

	@Override
	public QueryExecution createQueryExecution(String queryString) {
		List<QueryExecution> queryExecutions = new ArrayList<QueryExecution>(decoratees.size());
		for (QueryExecutionFactory decoratee : decoratees) {
			QueryExecution qe = decoratee.createQueryExecution(queryString);
			queryExecutions.add(qe);
		}
		return new QueryExecutionFallback(queryExecutions);
	}

	/* (non-Javadoc)
	 * @see org.aksw.jena_sparql_api.core.QueryExecutionFactory#getId()
	 */
	@Override
	public String getId() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aksw.jena_sparql_api.core.QueryExecutionFactory#getState()
	 */
	@Override
	public String getState() {
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		//official DBpedia
		QueryExecutionFactory qef1 = new QueryExecutionFactoryHttp("http://dbpedia.org/sparql", "http://dbpedia.org");
		//DBpedia Live
		QueryExecutionFactory qef2 = new QueryExecutionFactoryHttp("http://live.dbpedia.org/sparql", "http://dbpedia.org");
		//DBpedia in LOD cloud cache
		QueryExecutionFactory qef3 = new QueryExecutionFactoryHttp("http://lod.openlinksw.org/sparql", "http://dbpedia.org");
		
		List<QueryExecutionFactory> qefs = new ArrayList<QueryExecutionFactory>();
		qefs.add(qef1);
		qefs.add(qef2);
		qefs.add(qef3);
		
		QueryExecutionFactory qef = new QueryExecutionFactoryFallback(qefs);
		String query = "SELECT * WHERE {?s a <http://dbpedia.org/ontology/Book>.} LIMIT 10";
		QueryExecution qe = qef.createQueryExecution(query);
		ResultSet rs = qe.execSelect();
		System.out.println(ResultSetFormatter.asText(rs));
	}

}