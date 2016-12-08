package org.aksw.jena_sparql_api.beans.model;

import java.util.Iterator;

public interface CollectionOps {
    Iterator<?> getItems(Object entity);
    void setItems(Object entity, Iterator<?> items);
}
