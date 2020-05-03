package org.aksw.jena_sparql_api.io.common;


/**
 * Interface for nested references.
 * References allow for sharing an entity across several clients and
 * deferring the release of that entity's resources immediately to the point
 * in time when the last reference is released. The main use case is for memory paging
 * such that if several threads request the same page only one physical buffer is handed out
 * from a cache - conversely, as long as a page is still in used by a client, cache eviction
 * may be suppressed.
 *
 *
 * @author raven
 *
 * @param <T>
 */
public interface Reference<T>
    extends AutoCloseable
{
    /**
     * Get the referent
     *
     * @return The referent
     */
    T get();

    /**
     * Acquire a new reference with a given comment object
     * Acquiration fails if isAlive() returns false
     *
     * @return
     */
    Reference<T> acquire(Object purpose);

    /**
     * A reference may itself be released, but references to it may keep it alive
     *
     * @return true iff either this reference is not closed or there exists any acquired reference.
     */
    boolean isAlive();

    /**
     * Check whether this reference is closed / released
     */
    boolean isClosed();


    // TODO The throws declaration of Autoclose can be a pain to work with - override it?
    // @Override
    // void close();


    /**
     * Optional operation.
     *
     * References may expose where they were aquired
     *
     * @return
     */
    StackTraceElement[] getAquisitionStackTrace();
}
