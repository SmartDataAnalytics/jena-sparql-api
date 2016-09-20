package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.HashMap;
import java.util.Map;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDecorator;
import org.aksw.jena_sparql_api.util.collection.RangedSupplier;
import org.aksw.jena_sparql_api.views.index.OpViewMatcher;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.engine.binding.Binding;


public class QueryExecutionFactoryViewMatcherMaster
    extends QueryExecutionFactoryDecorator
{
    //protected SparqlViewMatcherSystem viewMatcherSystem;
	//protected QueryExecutionFactory decoratee;
	protected OpViewMatcher viewMatcher;
	protected Map<Node, RangedSupplier<Long, Binding>> opToRangedSupplier;

    protected long indexResultSetSizeThreshold;

    protected Map<Node, ? super ViewCacheIndexer> serviceMap;


//    public QueryExecutionFactoryViewMatcherMaster(QueryExecutionFactory decoratee, Map<Node, ? super ViewCacheIndexer> serviceMap) {
//        this(decoratee, serviceMap, new SparqlViewMatcherSystemImpl(), 10000);
//    }

    public QueryExecutionFactoryViewMatcherMaster(QueryExecutionFactory decoratee, OpViewMatcher viewMatcher, long indexResultSetSizeThreshold) {
        super(decoratee);
        //this.viewMatcherSystem = viewMatcherSystem;
    	//this.decoratee = decoratee;
        this.viewMatcher = viewMatcher;
        this.indexResultSetSizeThreshold = indexResultSetSizeThreshold;
        this.opToRangedSupplier = new HashMap<>();
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
    	QueryExecution result = new QueryExecutionViewMatcherMaster(query, decoratee, viewMatcher, opToRangedSupplier);
    	return result;
    }

    @Override
    public QueryExecution createQueryExecution(String queryString) {
    	throw new RuntimeException("parsing required");
    }

}
