package org.aksw.jena_sparql_api.concept_cache.collection;

import java.util.AbstractCollection;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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
public class FeatureMapImpl<K, V>
    extends AbstractCollection<Entry<Set<K>, V>>
    //extends AbstractMultimap
    implements FeatureMap<K, V>
{
    // This map contains the actual data, the other fields contain helper structures

    protected Map<K, Integer> tagToCount;

    protected Multimap<K, Set<K>> tagToTagSets;


    // Maybe the following two maps could be replaced with BiHashMultimap<K, V>

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
    public FeatureMapImpl() {
        super();
        this.tagToCount = new HashMap<>();
        this.tagToTagSets = HashMultimap.create();
        this.tagSetToValues = HashMultimap.create();
        this.valueToTagSets = HashMultimap.create();
    }

    @Override
    public Set<Set<K>> keySet() {
        Set<Set<K>> result = tagSetToValues.keySet();
        return result;
    }

    @Override
    public Collection<V> values() {
        Collection<V> result = tagSetToValues.values();
        return result;
    }

    @Override
    public Set<Entry<Set<K>,Collection<V>>> entrySet() {
        Set<Entry<Set<K>, Collection<V>>> result = tagSetToValues.asMap().entrySet();
        return result;
    };

    @Override
    public void put(Set<K> tagSet, V value) {
        //tagSetToValues.asMap().
        tagSetToValues.put(tagSet, value);
        tagSet.forEach(tag -> {
            tagToTagSets.put(tag, tagSet);
            tagToCount.merge(tag, 1, Integer::sum);
        });

        valueToTagSets.put(value, tagSet);

        //return value;
    }

    @Override
    public boolean remove(Object value) {
        @SuppressWarnings("unchecked")
        Collection<Set<K>> tagSets = valueToTagSets.get((V)value);

        tagSets.forEach(tagSet -> {
            tagSet.forEach(tag -> tagToCount.merge(tag, 1, (a, b) -> a - b));
        });

        return true;
    }

    /**
     * Return every entry of this featureMap whose associated feature set
     * is a super set of the given one.
     * 
     */
    @Override
    public Collection<Entry<Set<K>, V>> getIfSupersetOf(Set<K> prototype) {
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
    public Collection<Entry<Set<K>, V>> getIfSubsetOf(Set<K> prototype) {
        // get the count if we used index lookup
        int indexCount = prototype.stream().mapToInt(tag -> tagToCount.getOrDefault(tag, 0)).sum();
        int totalCount = valueToTagSets.size();

        Stream<Set<K>> tagSetStream;
//        float scanThreshold = 0.3f;
//        float val = scanThreshold * totalCount;
//        if(indexCount > val) {
        boolean useScan = indexCount >= totalCount; 
        if(useScan) {
            // perform a scan
            tagSetStream = tagSetToValues.keySet().stream();
        } else {
            tagSetStream = prototype.stream()
                    .flatMap(tag -> tagToTagSets.get(tag).stream())
                    .distinct();
        }

        Collection<Entry<Set<K>, V>> result = tagSetStream
            .filter(tagSet -> prototype.containsAll(tagSet))
            .flatMap(tagSet -> {
                Collection<V> values = tagSetToValues.get(tagSet);
                Stream<Entry<Set<K>, V>> r = values.stream()
                        .map(w -> new SimpleEntry<>(tagSet, w));
                return r;
            })
            .collect(Collectors.toSet());

        return result;
    }

    @Override
    public String toString() {
        return "ContainmentMapImpl [tagToCount=" + tagToCount
                + ", tagToTagSets=" + tagToTagSets + ", tagSetToValues="
                + tagSetToValues + ", valueToTagSets=" + valueToTagSets + "]";
    }

    @Override
    public Iterator<Entry<Set<K>, V>> iterator() {
        Iterator<Entry<Set<K>, V>> result = tagSetToValues.entries().iterator();
        return result;
    }

    @Override
    public int size() {
        int result = tagSetToValues.size();
        return result;
    }

}
