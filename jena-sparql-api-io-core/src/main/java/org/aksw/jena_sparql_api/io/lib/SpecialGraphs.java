package org.aksw.jena_sparql_api.io.lib;

import java.nio.file.Path;

import org.aksw.jena_sparql_api.io.binseach.GraphFromPrefixMatcher;
import org.apache.jena.graph.Graph;

public class SpecialGraphs {
	public static Graph fromSortedNtriplesFile(Path path) {
		Graph result = new GraphFromPrefixMatcher(path);
		return result;
	}
}
