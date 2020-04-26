package org.aksw.jena_sparql_api.io.common;

public interface Reference<T>
    extends AutoCloseable
{
    T get();

    /**
     * Aquire a new reference with a given comment object
     * Aquiration fails if isAlive() returns false
     *
     * @return
     */
    Reference<T> aquire(Object purpose);

    /**
     * A reference may itself be released, but references to it may keep it alive
     *
     *
     * @return
     */
    boolean isAlive();

    /**
     * Check whether this reference is closed / released
     */
    boolean isClosed();

    /**
     * Optional operation.
     *
     * References may expose where they were aquired
     *
     * @return
     */
    StackTraceElement[] getAquisitionStackTrace();
}
