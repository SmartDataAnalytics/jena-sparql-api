package org.aksw.jena_sparql_api.util;

import java.util.AbstractSet;
import java.util.Objects;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;

public class SetFromGraph
    extends AbstractSet<Triple>
{
    protected Graph graph;

    public SetFromGraph(Graph graph) {
        this.graph = Objects.requireNonNull(graph);
    }

    public Graph getGraph() {
        return graph;
    }

    @Override
    public boolean add(Triple e) {
        boolean result = !contains(e);
        if (result) {
            graph.add(e);
        }
        return result;
    }

    @Override
    public boolean remove(Object o) {
        boolean result = contains(o);

        if (result) {
            Triple t = (Triple)o;
            graph.delete(t);
        }
        return result;
    }

    @Override
    public ExtendedIterator<Triple> iterator() {
        ExtendedIterator<Triple> result = graph.find();
        return result;
    }

    @Override
    public boolean contains(Object o) {
        boolean result = false;
        if(o instanceof Triple) {
            Triple t = (Triple)o;
            result = graph.contains(t);
        }

        return result;
    }

    @Override
    public int size() {
        int result = graph.size();
        return result;
    }

    public static SetFromGraph wrap(Graph graph) {
        SetFromGraph result = new SetFromGraph(graph);
        return result;
    }
}