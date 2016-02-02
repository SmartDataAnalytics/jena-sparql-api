package org.aksw.jena_sparql_api_sparql_path2;

public interface LabeledEdge<V, T>
{
    V getSource();
    V getTarget();
    T getLabel();
    void setLabel(T label);
}