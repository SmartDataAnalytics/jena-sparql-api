package org.aksw.jena_sparql_api.update;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphListener;
import org.apache.jena.graph.Triple;


public abstract class GraphListenerBatchBase
    implements GraphListener
{
    protected abstract void addEvent(Graph g, Iterator<Triple> it) ;
    protected abstract void deleteEvent(Graph g, Iterator<Triple> it) ;

    public void addEvent(Graph g, Iterable<Triple> items) {
        addEvent(g, items.iterator());
    }

    public void deleteEvent(Graph g, Iterable<Triple> items) {
        deleteEvent(g, items.iterator());
    }

    @Override
    public void notifyAddTriple(Graph g, Triple t) {
        addEvent(g, Collections.singleton(t));
    }

    @Override
    public void notifyAddArray(Graph g, Triple[] triples) {
        addEvent(g, Arrays.asList(triples));
    }

    @Override
    public void notifyAddList(Graph g, List<Triple> triples) {
        addEvent(g, triples.iterator());
    }

    @Override
    public void notifyAddIterator(Graph g, Iterator<Triple> it) {
        addEvent(g, it);
    }

    @Override
    public void notifyAddGraph(Graph g, Graph added) {
    }

    @Override
    public void notifyDeleteTriple(Graph g, Triple t) {
        deleteEvent(g, Collections.singleton(t));
    }

    @Override
    public void notifyDeleteList(Graph g, List<Triple> triples) {
        deleteEvent(g, triples.iterator());
    }

    @Override
    public void notifyDeleteArray(Graph g, Triple[] triples) {
        deleteEvent(g, Arrays.asList(triples));
    }

    @Override
    public void notifyDeleteIterator(Graph g, Iterator<Triple> it) {
        deleteEvent(g, it);
    }

    @Override
    public void notifyDeleteGraph(Graph g, Graph removed) {
    }

    @Override
    public void notifyEvent(Graph source, Object value) {
    }

}
