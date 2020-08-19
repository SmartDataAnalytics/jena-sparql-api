package org.aksw.jena_sparql_api.sparql_path2;

import java.util.function.Supplier;

import org.jgrapht.Graph;

public class VertexFactoryInteger
    implements Supplier<Integer>
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
    public Integer get() {

        while(graph.containsVertex(nextId)) {
            ++nextId;
        }

        //graph.addVertex(nextId);
        return nextId;
    }

}