package org.aksw.jena_sparql_api.lookup;

public interface ListService<C, T> {
    ListPaginator<T> createPaginator(C concept);
}
