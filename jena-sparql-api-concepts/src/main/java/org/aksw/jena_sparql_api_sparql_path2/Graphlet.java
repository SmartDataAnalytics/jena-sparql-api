package org.aksw.jena_sparql_api_sparql_path2;

import java.util.Iterator;

/**
 * A set of Triplet<V, E>
 *
 * @author raven
 *
 * @param <V>
 * @param <E>
 */
public interface Graphlet<V, E> {
    Iterator<Triplet<V, E>> find(V s, E e, V o);
}
