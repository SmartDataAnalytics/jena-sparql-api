package org.aksw.jena_sparql_api.sparql_path2;

import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;

public class VertexFactoryInteger
    implements VertexFactory<Integer>
{
    protected Graph<Integer, ?> graph;
    int nextId;


    public VertexFactoryInteger(Graph<Integer, ?> graph) {
        this(graph, 0);
    }

    public VertexFactoryInteger(Graph<Integer, ?> graph, int nextId) {
        super();
        this.graph = graph;
        this.nextId = nextId;
    }



    @Override
    public Integer createVertex() {

        while(graph.containsVertex(nextId)) {
            ++nextId;
        }

        //graph.addVertex(nextId);
        return nextId;
    }

}