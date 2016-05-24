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


//    @Override
//    public Collection<Entry<K, V>> getAllEntriesThatAreSubsetsOf(
//            Set<K> prototye) {
        // TODO Auto-generated method stub
//        return null;
//    }
    // Map from a tag to all sets containing it



    @Override
    public Collection<Entry<Set<K>, V>> getAllEntriesThatAreSubsetsOf(Set<K> prototype) {
        //Set<Entry<Set<K>, Set<V>>> result;

        K leastUsedTag = prototype
            .stream()
            .map(k -> new SimpleEntry<>(k, tagToCount.getOrDefault(k, 0)))
            .min((a, b) -> a.getValue() - b.getValue())
            .map(Entry::getKey)
            .orElse(null);

        Collection<Set<K>> rawTagSets = tagToTagSets.get(leastUsedTag);

        Stream<Entry<Set<K>, V>> taggedStream = rawTagSets
                .stream()
                .filter(tagSet -> prototype.containsAll(tagSet))
                .flatMap(tagSet -> {
                    Collection<V> v = tagSetToValues.get(tagSet);

                    Stream<Entry<Set<K>, V>> r = v.stream()
                        .map(w -> new SimpleEntry<>(tagSet, w));

                    return r;
                });

//        Collection<V> untagged = tagSetToValues.get(Collections.emptySet());

//        Stream<Entry<Set<K>, V>> untaggedStream = untagged.stream()
//            .map(v -> new SimpleEntry<>(Collections.<K>emptySet(), v));
//
//        Stream<Entry<Set<K>, V>> resultStream = Stream.concat(taggedStream, untaggedStream);
        //Stream<Entry<Set<K>, V>> resultStream = taggedStream;

        Collection<Entry<Set<K>, V>> result = taggedStream.collect(Collectors.toList());
        return result;

//        Stream<V> stream = smallestTagSets.stream()
//                .flatMap(tags -> tagSetToValues.get(tags));
//
//        stream = Stream.concat(stream, emptyTagValues.stream());
//
        //Collection<V> result = .collect(Collectors.asList());


    }

}


//// Find out the least frequest key
//
//Map<K, Collection<V>> map = tagToValues.asMap();
//
//// Map each key to its size and take the smallest one
//Entry<Set<K>, Set<V>> prototype.stream()
//    .map(k -> new SimpleEntry<>(map.get(k).size()))
//    .min((a, b) -> b.getValue() - a.getValue());
