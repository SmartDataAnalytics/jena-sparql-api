package org.aksw.jena_sparql_api.analytics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.mapper.AccStaticMultiplex;
import org.aksw.jena_sparql_api.mapper.Accumulator;
import org.aksw.jena_sparql_api.mapper.Aggregator;
import org.aksw.jena_sparql_api.mapper.AggregatorBuilder;
import org.aksw.jena_sparql_api.utils.ResultSetUtils;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.jena.graph.Node;
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
    implements Accumulator<String, Set<String>>
{
    //public NavigableSet<String> prefixes = new TreeSet<>();
    protected PatriciaTrie<Void> prefixes = new PatriciaTrie<>();
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




    public static Map<Var, Set<String>> analyzePrefixes(ResultSet rs, int targetSize) {
        Aggregator<String, Set<String>> subAgg = createAggregatorStringPrefixes(targetSize);

        Map<Var, Set<String>> result = analyzeResultSetUrisPerVar(rs, subAgg);

        return result;
    }

    public static <V> Map<Var, V> analyzeResultSetUrisPerVar(ResultSet rs, Aggregator<String, V> subAgg) {
        List<Var> vars = ResultSetUtils.getVars(rs);
        Aggregator<Binding, Map<Var, V>> agg = createAggregatorResultSetUrisPerVar(vars, subAgg);
        Map<Var, V> result = aggregate(rs, agg);

        return result;
    }

    // TODO Move to resultset utils
    public static <V> V aggregate(ResultSet rs, Aggregator<? super Binding, V> agg) {
        Accumulator<? super Binding, V> acc = agg.createAccumulator();
        V result = aggregate(rs, acc);
        return result;
    }

    public static <V> V aggregate(ResultSet rs, Accumulator<? super Binding, V> acc) {
        while(rs.hasNext()) {
            Binding binding = rs.nextBinding();
            acc.accumulate(binding);
        }

        V result = acc.getValue();
        return result;
    }


//    public static Aggregator<Binding, Map<Var, Set<String>>> createAggregatorResultSetPrefixesPerVar(List<Var> vars, int targetSize) {
//        Aggregator<Binding, Map<Var, Set<String>>> result =
//            createAggregatorNodesPerVar(
//                vars,
//                createAggregatorNodeToUris(
//                    createAggregatorStringPrefixes(targetSize)));
//
//        return result;
//    }

    public static <V> Aggregator<Binding, Map<Var, V>> createAggregatorResultSetUrisPerVar(List<Var> vars, Aggregator<String, V> subAgg) {
        Aggregator<Binding, Map<Var, V>> result =
            createAggregatorNodesPerVar(
                vars,
                createAggregatorNodeToUris(subAgg));

        return result;
    }

    public static <V> Aggregator<Binding, Map<Var, V>> createAggregatorNodesPerVar(List<Var> vars, Aggregator<Node, V> subAgg) {

        //List<Var> vars = ResultSetUtils.getVars(rs);
        Map<Var, Accumulator<Node, V>> accMap = vars.stream()
                .collect(Collectors.toMap(v -> v, v -> subAgg.createAccumulator()));

        BiFunction<Binding, Var, Node> fn = (binding, var) -> binding.get(var);
        Aggregator<Binding, Map<Var, V>> result = () -> AccStaticMultiplex.create(fn, accMap);
        return result;
    }

    public static <V> Aggregator<Node, V> createAggregatorNodeToUris(Aggregator<String, V> subAgg) {
        //Aggregator<String, Set<String>> subAgg = createAggregatorStringPrefixes();

        Aggregator<Node, V> result = AggregatorBuilder
                .from(subAgg)
                .wrapWithBindingTransform((Function<Node, String>)(node) -> node.getURI())
                .wrapWithCondition((node) -> node.isURI())
                .get();

        return result;
    }

    public static Aggregator<String, Set<String>> createAggregatorStringPrefixes(int targetSize) {
        Aggregator<String, Set<String>> result = AggregatorBuilder
                .from(() -> new PrefixAccumulator(targetSize))
                .wrapWithMap(PrefixAccumulator::defaultGrouper)
                .wrapWithTransform(PrefixAccumulator::flatMapMapValues)
                .get();

        return result;
    }

    public static void main(String[] args) {

        // QueryExecutionFactory qef = FluentQueryExecutionFactory.http("http://dbpedia.org/sparql", "http://dbpedia.org").create();
    	
    	try (QueryExecution qe = QueryExecutionFactory.createServiceRequest("http://dbpedia.org/sparql", QueryFactory.create("Select * { ?s a <http://dbpedia.org/ontology/Airport> } Limit 100"))) {
    		ResultSet rs = qe.execSelect();
            Map<Var, Set<String>> ps = analyzePrefixes(rs, 50);
            System.out.println("Prefixes: " + ps);
    	}



        // Next step: given a sparql result set,

        // Accumulator that maps each binding

        // Create an aggregator that accumulates every variable's prefix set.
        //Aggregator<Binding, Map<Var, Set<String>>>


        Accumulator<String, Set<String>> x = createAggregatorStringPrefixes(3).createAccumulator();

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