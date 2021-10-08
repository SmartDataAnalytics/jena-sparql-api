package org.aksw.jena_sparql_api.util.iri;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;

public class PrefixUtils {
    public static  void usedPrefixes(PrefixMapping in, Stream<Node> node, PrefixMapping out) {
        node.forEach(n -> usedPrefixes(n, in, out));
    }

    public static void usedPrefixes(Node node, PrefixMapping in, PrefixMapping out) {
        if(node.isURI()) {
            String iri = node.getURI();
            addPrefix(iri, in, out);
        } else if(node.isLiteral()) {
            String iri = node.getLiteralDatatypeURI();

            // FIXME Ignore the implicit datatypes xsd:string and rdf:langString
            if(iri != null
//                    && !Objects.equals(iri, XSD.xstring.getURI())
//                    && !Objects.equals(iri, RDF.langString.getURI())
                    ) {
                addPrefix(iri, in, out);
            }
        }
    }

    public static void addPrefix(String iri, PrefixMapping in, PrefixMapping out) {
        Entry<String, String> e = findLongestPrefix(in, iri);
        if(e != null) {
            String prefix = e.getKey();
            String uri = e.getValue();
            out.setNsPrefix(prefix, uri);
        }
    }


    /**
     * Finds the longest prefix.
     * For {@link PrefixMappingTrie} lookups are optimized - otherwise all entries will be scanned.
     * @param pm
     * @param uri
     * @return
     */
    public static Entry<String, String> findLongestPrefix(PrefixMapping pm, String uri) {
        Entry<String, String> result;
        if (pm instanceof PrefixMappingTrie) {
            PrefixMappingTrie pmt = (PrefixMappingTrie)pm;
            result = pmt.findMapping(uri, false).orElse(null);
        } else {
            result = findLongestPrefixCore(pm, uri);
        }
        return result;
    }

    /**
     * Linear scan of all prefix mappings to find the longest prefix.
     * null if none found.
     *
     * @param pm
     * @param uri
     * @return
     */
    public static Entry<String, String> findLongestPrefixCore(PrefixMapping pm, String uri) {
        int bestResultLength = -1;
        Entry<String, String> bestResult = null;
        Map<String, String> nsPrefixMap = pm.getNsPrefixMap();
        for (Entry<String, String> e : nsPrefixMap.entrySet()) {
            String ss = e.getValue();
            int l = ss.length();
            if (l > bestResultLength && uri.startsWith(ss) && (l != uri.length())) {
                bestResultLength = l;
                bestResult = e;
            }
        }
        return bestResult;
    }

    public static PrefixMapping usedPrefixes(PrefixMapping pm, Set<Node> nodes) {
        PrefixMapping result = new PrefixMappingImpl();
        Stream<Node> nodeStream = nodes.stream();
        usedPrefixes(pm, nodeStream, result);
        return result;
    }
}
