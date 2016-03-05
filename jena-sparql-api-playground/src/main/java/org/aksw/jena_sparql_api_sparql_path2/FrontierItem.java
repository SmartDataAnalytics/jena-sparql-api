package org.aksw.jena_sparql_api_sparql_path2;

import java.util.Set;

import org.apache.jena.graph.Node;

import scala.Tuple2;

/**
 * Map every data vertex V to the so far reached NFA states S and the corresponding path whose edges are expressed in E
 *
 * @author raven
 *
 * @param <S>
 * @param <V>
 * @param <E>
 */
public class FrontierItem<I, S, V, E>
    extends Tuple2<V, FrontierData<I, S, V, E>> {
    private static final long serialVersionUID = 6450807270172504356L;

    public FrontierItem(V _1, FrontierData<I, S, V, E> _2) {
        super(_1, _2);
    }

    public FrontierItem(I frontierId, Set<S> states, Directed<NestedPath<V, E>> pathHead) { //, Class<E> clazz) {
        this(pathHead.getValue().getCurrent(), new FrontierData<>(frontierId, states, pathHead));
    }

    public FrontierItem(I frontierId, Set<S> states, V vertex, boolean reverse, Class<E> clazz) {
        this(vertex, new FrontierData<>(frontierId, states, new Directed<>(new NestedPath<>(vertex), reverse)));
    }

}