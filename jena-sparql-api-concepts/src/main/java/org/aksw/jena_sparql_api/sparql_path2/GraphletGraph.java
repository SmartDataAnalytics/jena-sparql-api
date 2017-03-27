package org.aksw.jena_sparql_api.sparql_path2;

import java.util.Iterator;

import org.aksw.jena_sparql_api.utils.model.Triplet;
import org.aksw.jena_sparql_api.utils.model.TripletImpl;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;

public class GraphletGraph
    implements Graphlet<Node, Node>
{
    protected Graph graph;

    public GraphletGraph(Graph graph) {
        super();
        this.graph = graph;
    }

    @Override
    public Iterator<Triplet<Node, Node>> find(Node s, Node e, Node o) {
        Iterator<Triplet<Node, Node>> result =
                graph.find(s, e, o).mapWith(t -> new TripletImpl<Node, Node>(t.getSubject(), t.getPredicate(), t.getObject()));
        return result;
    }

}
