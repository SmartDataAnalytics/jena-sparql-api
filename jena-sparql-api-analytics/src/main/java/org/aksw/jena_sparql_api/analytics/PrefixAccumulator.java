package org.aksw.jena_sparql_api.analytics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collector.core.AggBuilder;
import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.jena_sparql_api.utils.IteratorResultSetBinding;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;


/**
 * Class for accumulating a set of prefixes with a specified target size from a set of strings
 *
 * @author raven
 *
 */
public class PrefixAccumulator
    implements Accumulator<String, Set<String>>, Serializable
{
	private static final long serialVersionUID = -4653863475436646211L;
	
	//public NavigableSet<String> prefixes = new TreeSet<>();
    protected PatriciaTrie<Long> prefixes = new PatriciaTrie<>();
    protected int targetSize;

    public PrefixAccumulator(int targetSize) {
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

  // TODO: Add to SetUtils (or map utils???)
  public static <T> Set<T> flatMapMapValues(Map<?, ? extends Collection<T>> map) {
      Set<T> result = map.values().stream()
              .flatMap(v -> v.stream())
              .collect(Collectors.toSet());

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

    /**
     * For a given lookup string find a string in a sorted map that is the longest prefix of the lookup string. 
     * 
     * There is a suggestion for longest common prefix match could be performed efficiently:
	 * https://github.com/rkapsi/patricia-trie/issues/5
     *
     * 
     * @param lookup
     * @param inclusive
     * @param prefixes
     * @return
     */
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
	 * There is a suggestion for longest common prefix match on
	 * https://github.com/rkapsi/patricia-trie/issues/5:
	 * 
	 */
//    public static String longestCommonPrefixOld(PatriciaTrie<?> trie) {
//    }
    
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
    	int l = prefix.length();
        // Remove items that are more specific than the current prefix
    	// prefixMap may include the lookup prefix so we need to filter out the
    	// entry that has the same length as prefix
        SortedMap<String, ?> map = prefixes.prefixMap(prefix);
        List<String> superseded = new ArrayList<>(map.keySet());
        for(String item : superseded) {
        	if (item.length() != l) {
        		prefixes.remove(item);
        	}
        }

        // Iterator.remove() fails with exceptions apparently due to concurrent modification
        // TODO Investigat whether this a bug in my code or in patricia trie...
//        Iterator<String> it = map.keySet().iterator();
//        while (it.hasNext()) {
//        	String str = it.next();
//        	if (str.length() != l) {
//        		it.remove();
//        	}
//        }

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
            if (lcp != null) {
                removeSuperseded(lcp);
                prefixes.put(lcp, null);
            }
        }
    }

    public static void main(String[] args) {

        // QueryExecutionFactory qef = FluentQueryExecutionFactory.http("http://dbpedia.org/sparql", "http://dbpedia.org").create();
    	
    	try (QueryExecution qe = QueryExecutionFactory.createServiceRequest("http://dbpedia.org/sparql", QueryFactory.create("Select * { ?s a <http://dbpedia.org/ontology/Airport> } Limit 100"))) {
    		ResultSet rs = qe.execSelect();
    		
			Accumulator<Binding, Map<Var, Set<String>>> acc = ResultSetAnalytics.usedPrefixes(50).createAccumulator();
			IteratorResultSetBinding.wrap(rs).forEachRemaining(acc::accumulate);
            Map<Var, Set<String>> ps = acc.getValue();
            System.out.println("Prefixes: " + ps);
    	}



        // Next step: given a sparql result set,

        // Accumulator that maps each binding

        // Create an aggregator that accumulates every variable's prefix set.
        //Aggregator<Binding, Map<Var, Set<String>>>


//        Accumulator<String, Set<String>> x = createAggregatorStringPrefixes(3).createAccumulator();

    	List<String> items = Arrays.asList("dbr:Leipzig", "dbr:London", "dbr:City");

    	PrefixAccumulator acc = new PrefixAccumulator(3);
    	items.stream().forEach(acc::accumulate);
//        x.add("http://dbpedia.org/resource/Litauen");
        System.out.println(acc.getValue());
        acc.accumulate("lgd:foo");

        System.out.println(acc.getValue());
        
        Set<String> result = Stream.concat(items.stream(), Stream.of("lgd:foo"))
        		.collect(AggBuilder.naturalAccumulator(() -> new PrefixAccumulator(3)).asCollector());
        System.out.println(result);
    }
    
    
}