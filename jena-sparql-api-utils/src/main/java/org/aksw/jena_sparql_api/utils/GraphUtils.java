package org.aksw.jena_sparql_api.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

import com.google.common.collect.Streams;

public class GraphUtils {

	
	public static Stream<Node> streamNodes(Graph graph) {
		return stream(graph)
			.flatMap(TripleUtils::streamNodes);
	}
	
	public static Stream<Triple> stream(Graph graph) {
		return Streams.stream(graph.find());
	}

    /**
     * Small convenience wrapper for default-loading of an RDF resource,
     * which would actually fit for RDFDataMgr
     *
     * FIXME Consolidate with RDFDataMgr{Ex, Rx}
     *
     * @param resourceName
     * @return
     */
    public static Stream<Triple> createTripleStream(String resourceName) {
        TypedInputStream in = RDFDataMgr.open(resourceName);
        Lang lang = RDFDataMgr.determineLang(resourceName, in.getContentType(), Lang.TURTLE);
        Iterator<Triple> it = RDFDataMgr.createIteratorTriples(in, lang, "http://example.org/");

        return Streams.stream(it);
    }

    public static Graph toMemGraph(Iterable<Triple> triples) {
        Graph result = GraphFactory.createGraphMem();
        triples.forEach(result::add);
        return result;
    }

    public static Map<Node, Graph> indexBySubject(Graph graph) {
        ExtendedIterator<Triple> it = graph.find(Node.ANY, Node.ANY, Node.ANY);
        Map<Node, Graph> result;
        try {
            result = indexBySubject(it);
        } finally {
            it.close();
        }
        return result;
    }

    public static Map<Node, Graph> indexBySubject(Iterable<Triple> triples) {
        Map<Node, Graph> result = indexBySubject(triples.iterator());
        return result;
    }

    public static Map<Node, Graph> indexBySubject(Iterator<Triple> it) {
        Map<Node, Graph> result = new HashMap<Node, Graph>();
        while(it.hasNext()) {
            Triple triple = it.next();
            Node s = triple.getSubject();

            Graph graph = result.get(s);
            if(graph == null) {
                graph = GraphFactory.createGraphMem();
                result.put(s,  graph);
            }
            graph.add(triple);
        }

        return result;
    }
}
