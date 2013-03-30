package org.aksw.jena_sparql_api.core;

import java.util.Iterator;
import java.util.Set;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.Model;

public class QueryExecutionStreamingWrapper
	extends QueryExecutionDecorator
	implements QueryExecutionStreaming
{
	//private QueryExecution decoratee;
	
	public QueryExecutionStreamingWrapper(QueryExecution decoratee) {
		super(decoratee);
		//this.decoratee = decoratee;
	}
	
    public static Iterator<Triple> createTripleIterator(Model model) {
        Set<Triple> set = model.getGraph().find(null, null, null).toSet();
        return set.iterator();
    }
	
	@Override
	public Iterator<Triple> execConstructStreaming() {
		Model model = decoratee.execConstruct();
		Iterator<Triple> result = createTripleIterator(model);
		return result;
	}

	@Override
	public Iterator<Triple> execDescribeStreaming() {
		Model model = decoratee.execDescribe();
		Iterator<Triple> result = createTripleIterator(model);
		return result;
	}
	
	public static QueryExecutionStreaming wrap(QueryExecution qe) {
		QueryExecutionStreaming result;
		if(qe instanceof QueryExecutionStreaming) {
			result = (QueryExecutionStreaming)qe;
		} else {
			result = new QueryExecutionStreamingWrapper(qe);
		}
		
		return result;
	}
}
