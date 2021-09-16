package org.aksw.jena_sparql_api.utils.model;

import java.util.Collection;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;

import com.google.common.collect.ForwardingCollection;


public class TransactionalCollectionDummyImpl<T, C extends Collection<T>>
    extends ForwardingCollection<T>
    implements TransactionalCollection<T>
{
    protected C delegate;

    public TransactionalCollectionDummyImpl(C delegate) {
        super();
        this.delegate = delegate;
    }


    @Override
    protected C delegate() {
        return delegate;
    }


    @Override
    public void begin(TxnType type) {
    }

    @Override
    public void begin(ReadWrite readWrite) {
    }

    @Override
    public boolean promote(Promote mode) {
        return true;
    }

    @Override
    public void commit() {
    }

    @Override
    public void abort() {
    }

    @Override
    public void end() {
    }

    @Override
    public ReadWrite transactionMode() {
        return ReadWrite.WRITE;
    }

    @Override
    public TxnType transactionType() {
        return TxnType.WRITE;
    }

    @Override
    public boolean isInTransaction() {
        return false;
    }

}
