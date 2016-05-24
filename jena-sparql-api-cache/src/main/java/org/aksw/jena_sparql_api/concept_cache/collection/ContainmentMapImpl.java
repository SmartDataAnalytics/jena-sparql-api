package org.aksw.jena_sparql_api.concept_cache.collection;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Map a set of features to a single value.
 *
 * In
 *
 * @author raven
 *
 * @param <K>
 * @param <V>
 */
public class ContainmentMapImpl<K, V>
    //extends AbstractMap<Set<K>, V>
    implements ContainmentMap<K, V>
{
    protected Map<K, Integer> tagToCount;

    protected Multimap<K, Set<K>> tagToTagSets;
    protected Multimap<Set<K>, V> tagSetToValues; // rename to tagSetToValues

    protected Multimap<V, Set<K>> valueToTagSets;


//    public ContainmentMapImpl(Map<K, Integer> tagToCount,
//            Multimap<K, Set<K>> tagToTagSets,
//            Multimap<Set<K>, V> tagSetToValues,
//            Multimap<V, Set<K>> valueToTagSets) {
//        super();
//        this.tagToCount = tagToCount;
//        this.tagToTagSets = tagToTagSets;
//        this.tagSetToValues = tagSetToValues;
//        this.valueToTagSets = valueToTagSets;
//    }
    public ContainmentMapImpl() {
        super();
        this.tagToCount = new HashMap<>();
        this.tagToTagSets = HashMultimap.create();
        this.tagSetToValues = HashMultimap.create();
        this.valueToTagSets = HashMultimap.create();
    }

    @Override
    public void put(Set<K> tags, V value) {
        tagSetToValues.put(tags, value);
        tags.forEach(tag -> {
            tagToTagSets.put(tag, tags);
            tagToCount.merge(tag, 1, Integer::sum);
        });

        //return value;
    }

    @Override
    public void remove(Object value) {
        @SuppressWarnings("unchecked")
        Collection<Set<K>> tagSets = valueToTagSets.get((V)value);

        tagSets.forEach(tagSet -> {
            tagSet.forEach(tag -> tagToCount.merge(tag, 1, (a, b) -> a - b));
        });

    }

    @Override
    public Collection<Entry<Set<K>, V>> getAllEntriesThatAreSupersetOf(Set<K> prototype) {
        //Set<Entry<Set<K>, Set<V>>> result;

        K leastUsedTag = prototype
            .stream()
            .map(k -> new SimpleEntry<>(k, tagToCount.getOrDefault(k, 0)))
            .min((a, b) -> a.getValue() - b.getValue())
            .map(Entry::getKey)
            .orElse(null);

        //Stream<Set<K>> baseStream;
        Stream<Entry<Set<K>, V>> baseStream;
        if(leastUsedTag != null) {
            Collection<Set<K>> rawTagSets = tagToTagSets.get(leastUsedTag);
            baseStream = rawTagSets
                    .stream()
                    .filter(tagSet -> tagSet.containsAll(prototype))
                    .flatMap(tagSet -> {
                        Collection<V> v = tagSetToValues.get(tagSet);

                        Stream<Entry<Set<K>, V>> r = v.stream()
                            .map(w -> new SimpleEntry<>(tagSet, w));

                        return r;
                    });

        } else {
            //baseStream = tagToTagSets.values().stream();
            baseStream = tagSetToValues.entries().stream();
                    //.map(v -> new SimpleEntry<>(Collections.<K>emptySet(), v));
            //baseStream = Stream.of(Collections.emptySet());
        }

//        Stream<Entry<Set<K>, V>> taggedStream = baseStream
//                .flatMap(tagSet -> {
//                    Collection<V> v = tagSetToValues.get(tagSet);
//
//                    Stream<Entry<Set<K>, V>> r = v.stream()
//                        .map(w -> new SimpleEntry<>(tagSet, w));
//
//                    return r;
//                });

        Collection<Entry<Set<K>, V>> result = baseStream.collect(Collectors.toList());
        return result;
    }


    @Override
    public Collection<Entry<Set<K>, V>> getAllEntriesThatAreSubsetOf(Set<K> prototype) {
        Collection<Entry<Set<K>, V>> result = prototype.stream()
            .flatMap(tag -> tagToTagSets.get(tag).stream())
            .flatMap(tagSet -> {
                Collection<V> values = tagSetToValues.get(tagSet);
                Stream<Entry<Set<K>, V>> r = values.stream()
                        .map(w -> new SimpleEntry<>(tagSet, w));
                return r;
            })
            .collect(Collectors.toSet());

        return result;
    }
}
