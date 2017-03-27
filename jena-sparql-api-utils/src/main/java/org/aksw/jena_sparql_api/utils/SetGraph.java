package org.aksw.jena_sparql_api.utils;

import java.util.AbstractSet;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;

public class SetGraph
    extends AbstractSet<Triple>
{
    private Graph graph;

    public SetGraph(Graph graph) {
        this.graph = graph;
    }

    @Override
    public ExtendedIterator<Triple> iterator() {
        ExtendedIterator<Triple> result = graph.find(Node.ANY, Node.ANY, Node.ANY);
        return result;
    }

    @Override
    public int size() {
        int result = graph.size();
        return result;
    }

    public static SetGraph wrap(Graph graph) {
        SetGraph result = new SetGraph(graph);
        return result;
    }
}