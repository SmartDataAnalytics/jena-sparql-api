package org;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.aksw.commons.collections.cache.BlockingCacheIterator;
import org.aksw.commons.collections.cache.Cache;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend2;
import org.aksw.jena_sparql_api.cache.extra.CacheResource;
import org.aksw.jena_sparql_api.cache.extra.CacheResourceCacheEntry;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;


public class CacheFrontend2Mem<K>
    implements CacheFrontend2<K>
{
    protected ExecutorService executorService;
    protected com.google.common.cache.Cache<K, Object> cacheMap;



    /**
     * Caches a given result set for the given key, and returns an (immediately resolved)
     * future of a cache resource giving access to a result set.
     * 
     * 
     * 
     */
    @Override
    public Future<CacheResource> write(K key, ResultSet resultSet) {

        
        // If a result set is being cached for the same key, join with the prior one
        
        //CacheBuilder.from("")


        // Note: A cache execution may already be in progress
        List<Binding> cacheData = new ArrayList<>();
        Cache<List<Binding>> cache = new Cache<>(cacheData);

        // Prepare the cache entry
        synchronized(cacheMap) {
            cacheMap.put(key, cache);
        }

        cacheMap.put(key, value);


        List<String> rsVars = resultSet.getResultVars();

        Callable<CacheResource> task = () -> {
            while(resultSet.hasNext() && !cache.isAbanoned()) {
                Binding binding = resultSet.nextBinding();
                cacheData.add(binding);
            }

            if(!cache.isAbanoned()) {
                cache.setComplete(true);
            }
            
            CacheResource r = new CacheResourceCacheEntry(cacheEntry);
        };
        Future<?> future = executorService.submit(task);


        BlockingCacheIterator<Binding> it = new BlockingCacheIterator<>(cache);
        QueryIterator queryIt = new QueryIterPlainWrapper(it);
        ResultSet rrs = ResultSetFactory.create(queryIt, rsVars);

        Future<ResultSet> result = CompletableFuture.completedFuture(rrs);
        return result;
        //result = ResultSetClose.
        //cache.put(key, value);
    }

    @Override
    public Future<CacheResource> write(K key, Model model) {
        throw new UnsupportedOperationException();
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
