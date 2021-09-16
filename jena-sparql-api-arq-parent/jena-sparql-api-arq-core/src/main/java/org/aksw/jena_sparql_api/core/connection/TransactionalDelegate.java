package org.aksw.jena_sparql_api.core.connection;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.Transactional;

public abstract class TransactionalDelegate
    implements Transactional
{
    protected abstract Transactional getDelegate();

    @Override
    public void begin(ReadWrite readWrite) {
        getDelegate().begin(readWrite);
    }

    @Override
    public void commit() {
        getDelegate().commit();
    }

    @Override
    public void abort() {
        getDelegate().abort();
    }

    @Override
    public void end() {
        getDelegate().end();
    }

    @Override
    public boolean isInTransaction() {
        return getDelegate().isInTransaction();
    }

    @Override
    public void begin(TxnType type) {
        getDelegate().begin(type);
    }

    @Override
    public boolean promote(Promote mode) {
        return getDelegate().promote(mode);
    }

    @Override
    public ReadWrite transactionMode() {
        return getDelegate().transactionMode();
    }

    @Override
    public TxnType transactionType() {
        return getDelegate().transactionType();
    }
}
