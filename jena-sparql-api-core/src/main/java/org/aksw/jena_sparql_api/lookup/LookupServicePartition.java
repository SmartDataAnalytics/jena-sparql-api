package org.aksw.jena_sparql_api.lookup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.common.collect.Iterables;


public class LookupServicePartition<K, V>
    implements LookupService<K, V>
{
    private LookupService<K, V> base;
    private int partitionSize;
    private int nThreads;

    //private ExecutorService executorService;

    public LookupServicePartition(LookupService<K, V> base, int partitionSize) {
        this(base, partitionSize, 1);
    }

    public LookupServicePartition(LookupService<K, V> base, int partitionSize, int nThreads) {
        this.base = base;
        this.partitionSize = partitionSize;
        this.nThreads = nThreads;
    }

    @Override
    public Map<K, V> apply(Iterable<K> keys)
    {
        try {
            Map<K, V> result = doLookup(keys);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected Map<K, V> doLookup(Iterable<K> keys) throws InterruptedException, ExecutionException
    {

        Iterable<List<K>> lists = Iterables.partition(keys, partitionSize);

        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);

        CompletionService<Map<K, V>> completionService = new ExecutorCompletionService<Map<K, V>>(executorService);

        int n = 0;
        for(List<K> list : lists) {
            LookupTask<K, V> task = new LookupTask<K, V>(base, list);
            completionService.submit(task);
            ++n;
        }

        executorService.shutdown();

        Map<K, V> result = new HashMap<K, V>();

        for(int i = 0; i < n; ++i) {
            Future<Map<K, V>> future = completionService.take();
            Map<K, V> tmp = future.get();

            result.putAll(tmp);
        }

        return result;
    }

    public static <K, V> LookupServicePartition<K, V> create(LookupService<K, V> base, int partitionSize) {
        LookupServicePartition<K, V> result = new LookupServicePartition<K, V>(base, partitionSize);
        return result;
    }

    public static <K, V> LookupServicePartition<K, V> create(LookupService<K, V> base, int partitionSize, int nThreads) {
        LookupServicePartition<K, V> result = new LookupServicePartition<K, V>(base, partitionSize, nThreads);
        return result;
    }

}
