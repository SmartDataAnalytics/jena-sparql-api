package org.aksw.jena_sparql_api.core;

import java.util.Iterator;
import java.util.Set;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;

//public class QueryExecutionWrapper
//	extends QueryExecutionDecorator
//	implements QueryExecution
//{
//	//private QueryExecution decoratee;
//	
//	public QueryExecutionWrapper(QueryExecution decoratee) {
//		super(decoratee);
//		//this.decoratee = decoratee;
//	}
//	
//    public static Iterator<Triple> createTripleIterator(Model model) {
//        Set<Triple> set = model.getGraph().find(null, null, null).toSet();
//        return set.iterator();
//    }
//	
//	@Override
//	public Iterator<Triple> execConstructTriples() {
//		Model model = decoratee.execConstruct();
//		Iterator<Triple> result = createTripleIterator(model);
//		return result;
//	}
//
//	@Override
//	public Iterator<Triple> execDescribeTriples() {
//		Model model = decoratee.execDescribe();
//		Iterator<Triple> result = createTripleIterator(model);
//		return result;
//	}
//	
//	public static QueryExecution wrap(QueryExecution qe) {
//		QueryExecution result;
//		if(qe instanceof QueryExecution) {
//			result = (QueryExecution)qe;
//		} else {
//			result = new QueryExecutionWrapper(qe);
//		}
//		
//		return result;
//	}
//}
