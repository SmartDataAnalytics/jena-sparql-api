package org.aksw.jena_sparql_api.utils.model;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

public class TransactionalSetImpl<T>
    extends TransactionalCollectionImpl<T, Set<T>>
    implements TransactionalSet<T>
{
    public TransactionalSetImpl() {
        super(new LinkedHashSet<>(), set -> new LinkedHashSet<>(set));
    }

    public TransactionalSetImpl(Set<T> baseCollection, Function<? super Set<T>, ? extends Set<T>> cloner) {
        super(baseCollection, cloner);
    }
}
