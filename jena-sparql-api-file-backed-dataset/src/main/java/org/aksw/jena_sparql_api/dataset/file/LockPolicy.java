package org.aksw.jena_sparql_api.dataset.file;

/**
 * Lock policy controls on which granularity to lock the underlying store.
 *
 * @author raven
 *
 */
public enum LockPolicy {
    /**
     * Lock and unlock on every transaction
     *
     */
    TRANSACTION,

    /**
     * Aquire a lock for the lifetime of the graph object.
     * Hence, eventually calling graph.close() is essential.
     *
     * Every write transaction will still cause writing to the store
     * (which means rewriting the whole file),
     * but the inter-process lock only has to be acquired once instead of
     * on every transaction
     *
     */
    LIFETIME,

    /**
     * Similar to lifetime, except that the graph-lifetime lock is only acquired
     * on the first transaction
     *
     */
    LIFETIME_DEFERRED
}