package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDecorator;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;


public class QueryExecutionFactoryViewMatcherMaster
    extends QueryExecutionFactoryDecorator
{
    //protected SparqlViewMatcherSystem viewMatcherSystem;
	//protected QueryExecutionFactory decoratee;
	protected OpRewriteViewMatcherStateful viewMatcher;
	protected ExecutorService executorService;
	//protected Map<Node, RangedSupplier<Long, Binding>> opToRangedSupplier;
	protected Map<Node, StorageEntry> storageMap;

    protected long indexResultSetSizeThreshold;

    protected Map<Node, ? super ViewCacheIndexer> serviceMap;


//    public QueryExecutionFactoryViewMatcherMaster(QueryExecutionFactory decoratee, Map<Node, ? super ViewCacheIndexer> serviceMap) {
//        this(decoratee, serviceMap, new SparqlViewMatcherSystemImpl(), 10000);
//    }

    public QueryExecutionFactoryViewMatcherMaster(QueryExecutionFactory decoratee, OpRewriteViewMatcherStateful viewMatcher) { //long indexResultSetSizeThreshold) {
        super(decoratee);
        this.viewMatcher = viewMatcher;
    }

    public QueryExecutionFactoryViewMatcherMaster(QueryExecutionFactory decoratee, OpRewriteViewMatcherStateful viewMatcher, ExecutorService executorService, Map<Node, StorageEntry> storageMap) { //long indexResultSetSizeThreshold) {
        super(decoratee);
        //this.viewMatcherSystem = viewMatcherSystem;
    	//this.decoratee = decoratee;
        this.viewMatcher = viewMatcher;
        this.executorService = executorService;
        //this.indexResultSetSizeThreshold = indexResultSetSizeThreshold;
        this.storageMap = storageMap;
        //this.opToRangedSupplier = new HashMap<>();
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
    	QueryExecution result = new QueryExecutionViewMatcherMaster(query, decoratee, viewMatcher);//, viewMatcher, executorService, storageMap);
    	return result;
    }

    @Override
    public QueryExecution createQueryExecution(String queryString) {
    	throw new RuntimeException("parsing required");
    }

}
