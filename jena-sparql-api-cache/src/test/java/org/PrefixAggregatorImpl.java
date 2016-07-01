package org;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.mapper.AccMap2;
import org.aksw.jena_sparql_api.mapper.AccTransform2;
import org.aksw.jena_sparql_api.mapper.Accumulator;
import org.aksw.jena_sparql_api.mapper.Aggregator;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.trie.PatriciaTrie;

// TODO Maybe we can use the aggregator / accumulator infrastructure
// These classes are actually the accumulators
interface PrefixAggregator
{
    Set<String> getPrefixes();
    void add(String prefix);
}



//class AccPartition<B, T>
//    implements Accumulator<B, T>
//{
//    protected Function<String, String> prefixToGroup;
//    protected BiFunction<String, String, PrefixAggregator> aggregatorFactory;
//    protected Map<String, PrefixAggregator> groupToAggregator;
//
//
//    // pattern for matching up to the third slash
//    public static final Pattern pattern = Pattern.compile("(^([^/]*/){3})");
//    public static String defaultGrouper(String prefix) {
//        Matcher m = pattern.matcher(prefix);
//
//        String result = m.find() ? m.group(1) : null;
//
//        System.out.println("group " + result + " for " + prefix);
//        return result;
//    }
//
//    public PrefixAggregatorGrouping(int targetSize) {
//        this(
//            PrefixAggregatorGrouping::defaultGrouper,
//            (prefix, group) -> new PrefixAggregatorImpl(targetSize)
//        );
//    }
//
//
//    public PrefixAggregatorGrouping(Function<String, String> prefixToGroup,
//            BiFunction<String, String, PrefixAggregator> aggregatorFactory) {
//        super();
//        this.prefixToGroup = prefixToGroup;
//        this.aggregatorFactory = aggregatorFactory;
//        this.groupToAggregator = new HashMap<>();
//    }
//
//    @Override
//    public void accumulate(String prefix) {
//        String group = prefixToGroup.apply(prefix);
//
//        PrefixAggregator agg = groupToAggregator
//            .computeIfAbsent(group, (g) -> aggregatorFactory.apply(prefix, g));
//
//        agg.add(prefix);
//    }
//
//    @Override
//    public Set<String> getValue() {
//        Set<String> result = groupToAggregator.values().stream()
//        .flatMap(v -> v.getPrefixes().stream())
//        .collect(Collectors.toSet());
//
//        return result;
//    }
//}

/**
 * Class for aggregating a set of prefixes with a specified target size from a set of strings
 *
 * @author raven
 *
 */
public class PrefixAggregatorImpl
    implements Accumulator<String, Set<String>>
{
    //public NavigableSet<String> prefixes = new TreeSet<>();
    protected PatriciaTrie<Void> prefixes = new PatriciaTrie<>();
    protected int targetSize;

    public PrefixAggregatorImpl(int targetSize) {
        this.targetSize = targetSize;
    }

    @Override
    public Set<String> getValue() {
        Set<String> result = prefixes.keySet();
        return result;
    }

  // pattern for matching up to the third slash
  public static final Pattern pattern = Pattern.compile("(^([^/]*/){3})");
  public static String defaultGrouper(String prefix) {
      Matcher m = pattern.matcher(prefix);

      String result = m.find() ? m.group(1) : null;

      System.out.println("group " + result + " for " + prefix);
      return result;
  }


    /**
     * Returns the common prefix of the given strings
     *
     * @return
     */
    public static String commonPrefix(String sa, String sb, boolean skipLast)
    {
        char[] a = sa.toCharArray();
        char[] b = sb.toCharArray();
        int n = Math.min(a.length, b.length);

        char[] tmp = new char[n];


        int i;
        for(i = 0; i < n; i++) {
            if(a[i] != b[i]) {
                break;
            }

            tmp[i] = a[i];
        }

        if(skipLast) {
            i = i - 1;
        }

        String result = i < 0 ? null : new String(tmp, 0, i);
        return result;
    }

    public static String longestPrefixLookup(String lookup, boolean inclusive, SortedMap<String, ?> prefixes)
    {
        String result = null;
        String current = lookup;

        while(result == null) {

            if(prefixes.containsKey(current)) {
                if(inclusive) {
                    result = current;
                    break;
                }
            }

            // From now on, inclusive is acceptable
            inclusive = true;

            String candidate = null;
            SortedMap<String, ?> head = prefixes.headMap(current);
            if(!head.isEmpty()) {
                candidate = head.lastKey();
            }

            if(candidate == null) {
                break;
            }

            current = commonPrefix(current, candidate, false);
        }

        return result;
    }

    /**
     * It sucks that lcp is not part of the public trie api ...
     * @param trie
     * @return
     */
    public static String longestCommonPrefix(PatriciaTrie<?> trie) {
        // for every prefix, find its predecessor - and merge those that yield the longest prefix

        // TODO Bail out early if stri

        String result = null;
        int bestLength = 0;
        OrderedMapIterator<String, ?> it = trie.mapIterator();

        // HACK Move iterator to the end
        // Would be much better if the Trie API provided a way to iterate the map in reverse
        while(it.hasNext()) {
            it.next();
        }

        if(it.hasPrevious()) {
            String higher = it.previous();

            while(it.hasPrevious()) {
                String lower = it.previous();
                if(higher.length() <= bestLength) {
                    continue;
                }

                String commonPrefix = commonPrefix(higher, lower, false);
                if(result == null || result.length() < commonPrefix.length()) {
                    result = commonPrefix;
                    bestLength = result.length();
                }

                higher = lower;
            }
        }

        return result;
    }

    public void removeSuperseded(String prefix) {
        // Remove items that are more specific than the current prefix
        SortedMap<String, ?> map = prefixes.prefixMap(prefix);
        List<String> superseded = new ArrayList<>(map.keySet());
        for(String item : superseded) {
            prefixes.remove(item);
        }

//        System.out.println("Removals for [" + prefix + "]: " + superseded);
    }

    public void accumulate(String prefix) {
        String bestMatch = longestPrefixLookup(prefix, true, prefixes);

//        System.out.println("longest prefix of: " + prefix + ": " + bestMatch);
        removeSuperseded(prefix);

        // If there is a best match, there is nothing to do, otherwise we need to add the prefix
        if(bestMatch == null) {
            prefixes.put(prefix, null);
//            System.out.println("  Prefix map now: " + prefixes.keySet());
        }

        // Check if the prefix set exceeds its maximum size
        if(prefixes.size() > targetSize) {
            String lcp = longestCommonPrefix(prefixes);
            if(lcp != null) {
                removeSuperseded(lcp);
                prefixes.put(lcp, null);
            }
        }
    }

    public static void main(String[] args) {
        Aggregator<String, Set<String>> agg = () -> new PrefixAggregatorImpl(3);

        Aggregator<String, Set<String>> x = () -> AccMap2.create((p, i) -> PrefixAggregatorImpl.defaultGrouper(p), agg);

        //Aggregator<String, Set<String>> x = AggTransform2.create(
        Accumulator<String, Set<String>> x =
            new AccTransform2<>(
                new AccMap2<String, String, Map<String, Set<String>>, ?>((p, i) -> PrefixAggregatorImpl.defaultGrouper(p), agg),
                (map) -> map.values().stream()
                    .flatMap(v -> v.getPrefixes().stream())
                    .collect(Collectors.toSet()));

        //PrefixAggregator x = new PrefixAggregatorGrouping(3);
        x.accumulate("http://dbpedia.org/resource/Leipzig");
//        x.add("http://dbpedia.org/resource/");
        x.accumulate("http://dbpedia.org/resource/London");
        x.accumulate("http://dbpedia.org/ontology/City");
//        x.add("http://dbpedia.org/resource/Litauen");
        x.accumulate("http://linkedgeodata.org/foo");

        System.out.println(x.getValue());
    }
}
