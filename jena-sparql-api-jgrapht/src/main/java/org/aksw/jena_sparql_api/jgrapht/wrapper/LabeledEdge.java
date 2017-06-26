package org.aksw.jena_sparql_api.jgrapht.wrapper;

public interface LabeledEdge<V, T>
{
    V getSource();
    V getTarget();
    T getLabel();
    void setLabel(T label);
}