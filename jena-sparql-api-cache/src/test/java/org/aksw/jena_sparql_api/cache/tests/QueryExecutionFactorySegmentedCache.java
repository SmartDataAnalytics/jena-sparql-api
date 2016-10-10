package org.aksw.jena_sparql_api.cache.tests;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDecorator;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

/**
 * Performs cache lookup
 * @author raven
 *
 */
class QueryExecutionFactorySegmentedCache
    extends QueryExecutionFactoryDecorator
{
    //protected SparqlCacheSystem cacheSystem;
    //protected BiFunction 
    
    
    public QueryExecutionFactorySegmentedCache(QueryExecutionFactory delegate) {
        super(delegate);
    }
    
    @Override
    public QueryExecution createQueryExecution(Query query) {
        // Perform a lookup whether the exact query  (unless with different variable names)
        // is part of the cache
        
        
        
        // TODO Auto-generated method stub
        return null;
    }

//    QueryExecution createQueryExecution(Query query) {
//        
//        
//        Function<Range<Long>, ClosableIterator<T>> itemSupplier        
//    }
}