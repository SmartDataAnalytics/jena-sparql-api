package org.aksw.jena_sparql_api.concept_cache.dirty;

import java.util.List;

public interface Tree<T> {
    T getRoot();
    List<T> getChildren(T node);
    T getParent(T node);
}
