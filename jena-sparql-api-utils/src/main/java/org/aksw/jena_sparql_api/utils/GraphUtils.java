package org.aksw.jena_sparql_api.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

public class GraphUtils {

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
