package org.aksw.jena_sparql_api.sparql_path2;

public interface EdgeLabelAccessor<E, T> {
    T getLabel(E edge);
    void setLabel(E edge, T label);
}