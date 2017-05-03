package org.aksw.jena_sparql_api.jgrapht.wrapper;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.jgrapht.EdgeFactory;


public class EdgeFactoryJena
    implements EdgeFactory<Node, Triple>
{
    protected Node predicate;

    public EdgeFactoryJena(Node predicate) {
        super();
        this.predicate = predicate;
    }

    @Override
    public Triple createEdge(Node sourceVertex, Node targetVertex) {
        Triple result = new Triple(sourceVertex, predicate, targetVertex);
        return result;
    }
}