package org;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import org.aksw.commons.collections.cache.BlockingCacheIterator;
import org.aksw.commons.collections.cache.Cache;
import org.aksw.commons.collections.lists.LinkedList;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend2;
import org.aksw.jena_sparql_api.cache.extra.CacheResource;
import org.aksw.jena_sparql_api.cache.extra.CacheResourceCacheEntry;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;


class SinkResultSet
{
    
}



/**
 * Responsibilities:
 * - Allocate a container for the given key
 * - Start a task that transfers data into the container
 * - Return a resource to fetch data from the container
 * 
 * @author raven
 *
 * @param <K>
 */
public class CacheFrontend2Mem<K>
    implements CacheFrontend2<K>
{
    protected ExecutorService executorService;
    protected com.google.common.cache.Cache<K, Object> cacheMap;

    protected Supplier<Sink<>> resultSet sink;
    
    //protected LinkedList<Object> ll;
    // Note: the executorservice knows the running tasks - no need to handle them ourselves.
    //protected LinkedList<Future<?>> runningTasks;


    /**
     * Caches a given result set for the given key, and returns an (immediately resolved)
     * future of a cache resource giving access to a result set.
     * 
     * 
     * 
     */
    @Override
    public Future<Supplier<ResultSet>> write(K key, ResultSet resultSet) {

        LinkedList<Object> i;
        //i.last

        // TODO: Check if there already a cache process running for the current key.
        // This must not happen at this stage
        
        //QueryExecution qe;
       //qe.exS
        
        Future<?> f;
        //f.i
        
        
        
        // If a result set is being cached for the same key, join with the prior one
        
        //CacheBuilder.from("")


        // Note: A cache execution may already be in progress
        List<Binding> cacheData = new ArrayList<>();
        Cache<List<Binding>> cache = new Cache<>(cacheData);

        cacheMap.put(key, cache);


        List<String> rsVars = resultSet.getResultVars();

        Runnable task = () -> {
            
            while(resultSet.hasNext() && !(cache.isAbanoned() || Thread.interrupted())) {
                Binding binding = resultSet.nextBinding();
                cacheData.add(binding);
            }

            if(!(cache.isAbanoned() && Thread.interrupted())) {
                cache.setComplete(true);
            }
            
        };
        Future<?> future = executorService.submit(task);

        CacheResource r = new CacheResourceCacheEntry(cacheEntry);


        BlockingCacheIterator<Binding> it = new BlockingCacheIterator<>(cache);
        QueryIterator queryIt = new QueryIterPlainWrapper(it);
        ResultSet rrs = ResultSetFactory.create(queryIt, rsVars);

        Future<ResultSet> result = CompletableFuture.completedFuture(rrs);
        
//        QueryIter queryIter = new QueryIterPlainWrapper();
//        ResultSetFactory.create();
        return result;
        //result = ResultSetClose.
        //cache.put(key, value);
    }

    @Override
    public Future<CacheResource> write(K key, Model model) {
        Model copy = ModelFactory.createDefaultModel();
        copy.add(model);
        
        cacheMap.put(key, copy);
        
        Future<CacheResource> result;
    }

    @Override
    public Future<CacheResource> write(K key, boolean value) {
        throw new UnsupportedOperationException();

    }

    @Override
    public CacheResource lookup(K key) {        
        cacheMap.getIfPresent(key);


    }

}
