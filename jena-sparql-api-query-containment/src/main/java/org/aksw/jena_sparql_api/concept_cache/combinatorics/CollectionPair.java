package org.aksw.jena_sparql_api.concept_cache.combinatorics;

import java.util.Collection;

import org.aksw.jena_sparql_api.utils.Pair;

public class CollectionPair<T>
    extends Pair<Collection<T>>
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public CollectionPair(Collection<T> key, Collection<T> value) {
        super(key, value);
    }
}