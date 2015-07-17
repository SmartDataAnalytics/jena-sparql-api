package org.aksw.jena_sparql_api.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.graph.GraphFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

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
