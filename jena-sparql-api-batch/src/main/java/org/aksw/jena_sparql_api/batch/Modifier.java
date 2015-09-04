package org.aksw.jena_sparql_api.batch;

/**
 * Modifies object T. Logging could be implemented as side effects of an
 * application
 *
 * @author raven
 *
 * @param <T>
 * @param <R>
 */
public interface Modifier<T> {
    void apply(T item);
}
