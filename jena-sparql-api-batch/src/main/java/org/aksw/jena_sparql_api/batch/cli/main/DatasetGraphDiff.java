package org.aksw.jena_sparql_api.batch.cli.main;

import java.util.Iterator;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphBase;
import org.apache.jena.sparql.core.Quad;

class PredicateIfQuadExists
    implements Predicate<Quad>
{
    protected DatasetGraph datasetGraph;

    public PredicateIfQuadExists(DatasetGraph datasetGraph) {
        super();
        this.datasetGraph = datasetGraph;
    }

    @Override
    public boolean apply(Quad quad) {
        boolean result = datasetGraph.contains(quad);
        return result;
    }
}

public class DatasetGraphDiff
    extends DatasetGraphBase
{
    protected DatasetGraph core;

    protected DatasetGraph added;
    protected DatasetGraph removed;


    @Override
    public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
        Predicate<Quad> pred = new PredicateIfQuadExists(removed);

        Iterator<Quad> itAdded = added.find(g, s, p, o);

        Iterator<Quad> result = core.find(g, s, p, o);
        result = Iterators.filter(result, pred);
        result = Iterators.concat(result, itAdded);

        return result;
    }

    @Override
    public Iterator<Quad> findNG(Node g, Node s, Node p, Node o) {
        Predicate<Quad> pred = new PredicateIfQuadExists(removed);

        Iterator<Quad> itAdded = added.findNG(g, s, p, o);

        Iterator<Quad> result = core.findNG(g, s, p, o);
        result = Iterators.filter(result, pred);
        result = Iterators.concat(result, itAdded);

        return result;
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        Iterator<Node> result = core.listGraphNodes();
        return result;
    }

    @Override
    public void addGraph(Node arg0, Graph arg1) {
    }

    @Override
    public Graph getDefaultGraph() {
        return null;
//        Graph result = core.getDefaultGraph();
//        return result;
    }

    @Override
    public Graph getGraph(Node arg0) {
        return null;
//        Graph result = core.getDefaultGraph();
//        return result;
    }

    @Override
    public void removeGraph(Node arg0) {
        // TODO Auto-generated method stub

    }

}
