package org.aksw.jena_sparql_api_sparql_path2;

import java.util.Iterator;

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
                graph.find(s, e, o).mapWith(t -> new Triplet<Node, Node>(t.getSubject(), t.getPredicate(), t.getObject()));
        return result;
    }

}
