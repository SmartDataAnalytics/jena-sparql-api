package org.aksw.jena_sparql_api.views.index;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collections.FeatureMap;
import org.aksw.commons.collections.FeatureMapImpl;
import org.apache.jena.sparql.algebra.Op;

/**
 *
 * @author raven
 *
 * @param <C> (Cache) Item object type
 * @param <Q> Query object type
 * @param <D> Type of the (D)ata associated with cache objects
 * @param <F> (F)eature type
 */
public class IndexSystemImpl<C, Q, D, F>
    implements IndexSystem<Entry<C, D>, Q>
{

    /**
     * Function that for a given item yields a stream (set) of
     * feature sets describing the item.
     * Feature sets can be considered disjunctive, i.e. a lookup will yield the item,
     * if any of its describing feature sets match.
     *
     */
    protected Function<C, Stream<Set<F>>> itemFeatureExtractor;

    /**
     * Function for extracting features from a query object
     *
     */
    protected Function<Q, Stream<Set<F>>> queryFeatureExtractor;

    /**
     * The item store
     *
     */
    protected FeatureMap<F, Entry<C, D>> featuresToItems;




    public IndexSystemImpl(
            Function<C, Stream<Set<F>>> itemFeatureExtractor,
            Function<Q, Stream<Set<F>>> queryFeatureExtractor)
    //        FeatureMap<F, Entry<C, D>> featuresToItems)
    {
        super();
        this.itemFeatureExtractor = itemFeatureExtractor;
        this.queryFeatureExtractor = queryFeatureExtractor;
        this.featuresToItems = new FeatureMapImpl<F, Entry<C, D>>();
//        this.featuresToItems = featuresToItems;
    }
    
    @Override
    public void add(Entry<C, D> item) {
        //featuresToItems.put(featureSet, new SimpleEntry<>(item, data));
        put(item.getKey(), item.getValue());
    }

    public void put(C item, D data) {
        itemFeatureExtractor
            .apply(item)
            .forEach(featureSet -> {
                featuresToItems.put(featureSet, new SimpleEntry<>(item, data));
            });
    }

    public Set<Entry<C, D>> lookup(Q query) {
        Set<Entry<C, D>> candidateEntries = queryFeatureExtractor.apply(query)
            .flatMap(featureSet -> featuresToItems.getIfSubsetOf(featureSet).stream())
            .map(e -> e.getValue())
            .collect(Collectors.toSet());

        return candidateEntries;
    }


    public static IndexSystemImpl<Op, Op, OpIndex, String> create() {
        Function<Op, Stream<Set<String>>> featureExtractor = (oop) ->
            Collections.singleton(OpVisitorFeatureExtractor.getFeatures(oop, (op) -> op.getClass().getSimpleName())).stream();

        //FeatureMap<String, Op> featureMap = new FeatureMapImpl<>();

        IndexSystemImpl<Op, Op, OpIndex, String> result = new IndexSystemImpl<>(
            featureExtractor,
            featureExtractor);

        
        return result;

    }

}
