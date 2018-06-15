package org.aksw.jena_sparql_api.lookup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.MoreExecutors;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;


public class LookupServicePartition<K, V>
    implements LookupService<K, V>
{
    //private static final Logger logger = LoggerFactory.getLogger(LookupServicePartition.class);

    private LookupService<K, V> base;
    private int partitionSize;
    private int nThreads;

    //private ExecutorService executorService;
    //private ExecutorService executorService;

    public LookupServicePartition(LookupService<K, V> base, int partitionSize) {
        this(base, partitionSize, 1);
    }

    public LookupServicePartition(LookupService<K, V> base, int partitionSize, int nThreads) { //ExecutorService executorService) {
        this.base = base;
        this.partitionSize = partitionSize;
        this.nThreads = nThreads;
        //this.executorService = executorService;
    }

    @Override
    public Flowable<Entry<K, V>> apply(Iterable<K> keys)
    {
    	return Flowable.fromIterable(keys)
			.buffer(partitionSize)
			.parallel(nThreads)
			.runOn(Schedulers.io())
			.flatMap(base::apply)
			.sequential();
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
