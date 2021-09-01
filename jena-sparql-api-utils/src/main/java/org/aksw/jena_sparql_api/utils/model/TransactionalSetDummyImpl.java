package org.aksw.jena_sparql_api.utils.model;

import java.util.Set;

public class TransactionalSetDummyImpl<T, C extends Set<T>>
    extends TransactionalCollectionDummyImpl<T, C>
    implements TransactionalSet<T>
{
    public TransactionalSetDummyImpl(C delegate) {
        super(delegate);
    }
}
