package org.aksw.jena_sparql_api.jgrapht.wrapper;

public interface LabeledEdgeFactory<V, E, T>
{
    E createEdge(V sourceVertex, V targetVertex, T label);
}