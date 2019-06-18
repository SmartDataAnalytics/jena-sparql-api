package org.aksw.jena_sparql_api.utils;

import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.shared.PrefixMapping;

public class PrefixUtils {
	public static  void usedPrefixes(PrefixMapping in, Stream<Node> node, PrefixMapping out) {
		node.forEach(n -> usedPrefixes(n, in, out));
	}
	
	public static void usedPrefixes(Node node, PrefixMapping in, PrefixMapping out) {
		if(node.isURI()) {
			String tmp = node.getURI();
			String shortForm;
			if((shortForm = in.shortForm(tmp)) != tmp) {
				String prefix = shortForm.substring(0, shortForm.indexOf(':'));
				String uri = in.getNsPrefixURI(prefix);
				
				out.setNsPrefix(prefix, uri);
			}
		}
	}
}
