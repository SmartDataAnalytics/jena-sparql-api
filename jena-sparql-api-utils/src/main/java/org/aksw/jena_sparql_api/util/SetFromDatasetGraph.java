package org.aksw.jena_sparql_api.util;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

public class SetFromDatasetGraph
    extends AbstractSet<Quad>
{
    private DatasetGraph datasetGraph;

    public SetFromDatasetGraph(DatasetGraph graph) {
        this.datasetGraph = graph;
    }

    public DatasetGraph getDatasetGraph() {
		return datasetGraph;
	}
    
    @Override
    public boolean add(Quad quad) {
        boolean result = contains(quad);
        datasetGraph.add(quad);
        return result;
    }

    @Override
    public boolean contains(Object item) {
        boolean result;
        if (item instanceof Quad) {
            Quad quad = (Quad)item;
            if (Quad.isDefaultGraph(quad.getGraph())) {
                Graph defaultGraph = datasetGraph.getDefaultGraph();
                result = defaultGraph == null ? false : defaultGraph.contains(quad.asTriple());
            } else {
                result = datasetGraph.contains(quad);
            }
        } else {
            result = false;
        }

        return result;
    }

    public static int size(DatasetGraph datasetGraph) {
        int result = 0;
        Graph defaultGraph = datasetGraph.getDefaultGraph();

        result += defaultGraph == null ? 0 : datasetGraph.getDefaultGraph().size();
        Iterator<Node> it = datasetGraph.listGraphNodes();
        while (it.hasNext()) {
            Node node = it.next();
            Graph g = datasetGraph.getGraph(node);
            result += g == null ? 0 : g.size();
        }

        return result;
    }

    public static Iterable<Quad> quads(DatasetGraph datasetGraph) {
        Graph defaultGraph = datasetGraph.getDefaultGraph();
        Set<Triple> triples = defaultGraph == null ? Collections.emptySet() : SetFromGraph.wrap(defaultGraph);
        Iterable<Quad> quads = () -> datasetGraph.find(Node.ANY, Node.ANY, Node.ANY, Node.ANY);

        Iterable<Quad> result = Iterables.concat(
                Iterables.transform(triples, t -> new Quad(Quad.defaultGraphIRI, t)),
                quads);

        return result;
    }

    @Override
    public Iterator<Quad> iterator() {
        Iterable<Quad> it = quads(datasetGraph);
        return it.iterator();
    }

    @Override
    public int size() {
        int result = size(datasetGraph);
        return result;
    }

    public static SetFromDatasetGraph wrap(DatasetGraph graph) {
        SetFromDatasetGraph result = new SetFromDatasetGraph(graph);
        return result;
    }
}
