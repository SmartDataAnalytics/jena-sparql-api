package org.aksw.jena_sparql_api.utils;

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
			String tmp = node.getURI();
            Entry<String, String> e = findLongestPrefix(in, tmp);
            if(e != null) {
            	String prefix = e.getKey();
            	String uri = e.getValue();
				out.setNsPrefix(prefix, uri);
			}
		}
	}

	/**
	 * Linear scan of all prefix mappings to find the longest prefix.
	 * null if none found.
	 * 
	 * @param pm
	 * @param uri
	 * @return
	 */
	public static Entry<String, String> findLongestPrefix(PrefixMapping pm, String uri) {
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
