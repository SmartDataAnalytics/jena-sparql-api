package org;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.collections4.trie.PatriciaTrie;

public class PrefixAggregator {
    //public NavigableSet<String> prefixes = new TreeSet<>();
    protected PatriciaTrie<Void> prefixes = new PatriciaTrie<>();
    protected int targetSize;

    public PrefixAggregator(int targetSize) {
        this.targetSize = targetSize;
    }

    public Set<String> getPrefixes() {
        Set<String> result = prefixes.keySet();
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

    public void removeSuperseded(String prefix) {
        // Remove items that are more specific than the current prefix
        SortedMap<String, ?> map = prefixes.prefixMap(prefix);
        List<String> superseded = new ArrayList<>(map.keySet());
        for(String item : superseded) {
            prefixes.remove(item);
        }

//        System.out.println("Removals for [" + prefix + "]: " + superseded);
    }

    public void add(String prefix) {
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
            // for every prefix, find its predecessor - and merge those that yield the longest prefix

            String bestCand = null;
            for(String higher : prefixes.keySet()) {
                String lower = prefixes.previousKey(higher);

//                System.out.println("prev of  " + higher + " is " + lower);

                if(lower != null) {
                    String commonPrefix = commonPrefix(higher, lower, false);
                    if(bestCand == null || bestCand.length() < commonPrefix.length()) {
                        bestCand = commonPrefix;
                    }
                }
            }

            removeSuperseded(bestCand);
            prefixes.put(bestCand, null);
        }
    }

    public static void main(String[] args) {
        PrefixAggregator x = new PrefixAggregator(2);
        x.add("http://dbpedia.org/resource/Leipzig");
//        x.add("http://dbpedia.org/resource/");
        x.add("http://dbpedia.org/resource/London");
        x.add("http://dbpedia.org/ontology/City");
        x.add("http://dbpedia.org/resource/Litauen");

        System.out.println(x.getPrefixes());
    }
}
