package org.aksw.jena_sparql_api_sparql_path2;

public interface EdgeLabelAccessor<E, T> {
    T getLabel(E edge);
    void setLabel(E edge, T label);
}