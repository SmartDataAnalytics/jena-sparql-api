package org.aksw.jena_sparql_api.io.filter.sys;

public interface ThrowingConsumer<T> {
    void accept(T item) throws Exception;
}
