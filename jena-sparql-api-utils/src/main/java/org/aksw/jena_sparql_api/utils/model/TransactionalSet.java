package org.aksw.jena_sparql_api.utils.model;

import java.util.Set;

public interface TransactionalSet<T>
    extends TransactionalCollection<T>, Set<T>
{
}
