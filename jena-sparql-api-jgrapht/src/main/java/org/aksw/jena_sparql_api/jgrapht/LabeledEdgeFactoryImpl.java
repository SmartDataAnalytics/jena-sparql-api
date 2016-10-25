package org.aksw.jena_sparql_api.jgrapht;

public class LabeledEdgeFactoryImpl<V, T>
    implements LabeledEdgeFactory<V, LabeledEdgeImpl<V, T>, T>
{

    @Override
    public LabeledEdgeImpl<V, T> createEdge(V sourceVertex, V targetVertex, T data) {
        LabeledEdgeImpl<V, T> result = new LabeledEdgeImpl<V, T>(sourceVertex, targetVertex, data);
        return result;
    }
}
//
//
//interface LabeledEdgeFactory<V, E, T>
//{
//    E createEdge(V sourceVertex, V targetVertex, T label);
//}
//
//class LabeledEdgeFactoryImpl<V, T>