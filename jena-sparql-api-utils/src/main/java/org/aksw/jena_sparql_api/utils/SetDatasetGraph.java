package org.aksw.jena_sparql_api.utils;

import java.util.AbstractSet;
import java.util.Iterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Quad;

public class SetDatasetGraph
    extends AbstractSet<Quad>
{
    private DatasetGraph graph;

    public SetDatasetGraph(DatasetGraph graph) {
        this.graph = graph;
    }

    @Override
    public boolean add(Quad quad) {
    	boolean result = contains(quad);
    	graph.add(quad);
    	return result;
    }

    @Override
    public boolean contains(Object item) {
    	boolean result = item instanceof Quad
    			? graph.contains((Quad)item)
    			: false;

    	return result;
    }

    @Override
    public Iterator<Quad> iterator() {
        Iterator<Quad> result = graph.find(Node.ANY, Node.ANY, Node.ANY, Node.ANY);
        return result;
    }

    @Override
    public int size() {
        int result = (int)graph.size();
        return result;
    }

    public static SetDatasetGraph wrap(DatasetGraph graph) {
        SetDatasetGraph result = new SetDatasetGraph(graph);
        return result;
    }
}
