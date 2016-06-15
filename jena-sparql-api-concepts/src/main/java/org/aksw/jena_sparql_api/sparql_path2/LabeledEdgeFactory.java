package org.aksw.jena_sparql_api.sparql_path2;

public interface LabeledEdgeFactory<V, E, T>
{
    E createEdge(V sourceVertex, V targetVertex, T label);
}