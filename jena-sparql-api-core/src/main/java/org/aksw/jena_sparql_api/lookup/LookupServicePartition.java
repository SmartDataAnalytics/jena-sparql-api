package org.aksw.jena_sparql_api.lookup;

import java.util.Map.Entry;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;


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
