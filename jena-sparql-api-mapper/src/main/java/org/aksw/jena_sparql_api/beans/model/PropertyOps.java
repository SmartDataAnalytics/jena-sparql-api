package org.aksw.jena_sparql_api.beans.model;

public interface PropertyOps {
    String getName();
    void setValue(Object entity, Object value);
    Object getValue(Object entity);
}
