package org.aksw.jena_sparql_api.core.connection;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.core.Transactional;

public interface TransactionalTmp
    extends Transactional
{
    @Override
    default boolean isInTransaction() {
        return false;
    }

    @Override
    default void begin(ReadWrite readWrite) {
    }

    @Override
    default void commit() {
    }

    @Override
    default void abort() {
    }

    @Override
    default void end() {
    }
}
