package org;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.aksw.commons.collections.cache.BlockingCacheIterator;
import org.aksw.commons.collections.cache.Cache;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend2;
import org.aksw.jena_sparql_api.cache.extra.CacheResource;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;

import com.google.common.cache.CacheBuilder;

public class CacheProgress {
    /**
     * A future for the caching process
     */

    Future<?> future;
    CacheResource cacheResource;

}

//class RunnableTaskResultSetCache {
//
//}

/**
 * A class that
 *
 * @author raven
 *
 */
public class CacheFuture {
    /**
     * A future for the caching process
     */

    Future<?> future;
    CacheResource cacheResource;
}

public class CacheFrontend2Mem<K>
    implements CacheFrontend2<K>
{
    protected ExecutorService executorService;
    protected com.google.common.cache.Cache<K, CacheResource> cacheMap;



    @Override
    public CacheFuture write(K key, ResultSet resultSet) {

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

        Runnable task = () -> {
            while(resultSet.hasNext() && !cache.isAbanoned()) {
                Binding binding = resultSet.nextBinding();
                cacheData.add(binding);
            }

            if(!cache.isAbanoned()) {
                cache.setComplete(true);
            }
        };
        Future<?> future = executorService.submit(task);


        BlockingCacheIterator<Binding> it = new BlockingCacheIterator<>(cache);
        QueryIterator queryIt = new QueryIterPlainWrapper(it);
        result = ResultSetFactory.create(queryIt, rsVars);

        //result = ResultSetClose.




        cache.put(key, value);
    }

    @Override
    public CacheFuture write(K key, Model model) {
        // TODO Auto-generated method stub

    }

    @Override
    public CacheFuture write(K key, boolean value) {
        // TODO Auto-generated method stub

    }

    @Override
    public CacheResource lookup(K key) {
        cacheMap.getIfPresent(key);


        cacheMap.

        // TODO Auto-generated method stub
        return null;
    }

}
