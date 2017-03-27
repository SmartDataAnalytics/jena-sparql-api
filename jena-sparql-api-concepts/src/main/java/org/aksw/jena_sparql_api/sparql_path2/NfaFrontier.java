package org.aksw.jena_sparql_api.sparql_path2;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * A nfa frontier maps states to the corresponding paths.
 * These paths are grouped by a key that is needed to advance the frontier.
 * Common group keys are either the target node of the path, or the direction of the predicate
 *
 *
 *
 * @author raven
 *
 * @param <S>
 * @param <V>
 * @param <E>
 */
public class NfaFrontier<S, G, V, E> {
    protected Map<S, Multimap<G, NestedPath<V, E>>> paths = new HashMap<>();


    //protected Function<NestedPath<V, E>, G> groupFn;

//    public NfaFrontier() {
//        groupFn = (Function<NestedPath<V, E>, G> & Serializable) nestedPath -> nestedPath.getCurrent();
//    }

//    public NfaFrontier(Function<NestedPath<V, E>, G> groupFn) {
//        this.groupFn = groupFn;
//    }
//
//
//    public static <S, V, E> NfaFrontier<S, V, V, E> createVertexGroupedFrontier() {
//        NfaFrontier<S, V, V, E> result = new NfaFrontier<S, V, V, E>((Function<NestedPath<V, E>, V> & Serializable) nestedPath -> nestedPath.getCurrent());
//        return result;
//    }

    public Set<S> getCurrentStates() {
        return paths.keySet();
    }

    public Multimap<G, NestedPath<V, E>> getPaths(S state) {
        Multimap<G, NestedPath<V, E>> result = paths.get(state);
        return result;
    }

    protected Multimap<G, NestedPath<V, E>> getOrCreateStateInfo(S state) {
        Multimap<G, NestedPath<V, E>> result = paths.get(state);
        if(result == null) {
            result = HashMultimap.create();
            paths.put(state, result);
        }

        return result;
    }

    public void add(S state, G groupKey, NestedPath<V, E> path) {
        //V node = path.getCurrent();
        Multimap<G, NestedPath<V, E>> nodeToPath = getOrCreateStateInfo(state);
        //G groupKey = groupFn.apply(path);
        nodeToPath.put(groupKey, path);
    }


    public boolean isEmpty() {
        boolean result = paths.entrySet().stream().allMatch(x -> x.getValue().isEmpty());
        return result;
    }

    public static <S, G, V, E> void addAll(NfaFrontier<S, G, V, E> frontier, Set<S> states, Function<NestedPath<V, E>, G> pathGrouper, V node) {
        addAll(frontier, states, pathGrouper, Collections.singleton(node));
    }

    public static <S, G, V, E> void addAll(NfaFrontier<S, G, V, E> frontier, Set<S> states, Function<NestedPath<V, E>, G> pathGrouper, Collection<V> nodes) {
        states.forEach(state -> {
            nodes.forEach(node -> {
                NestedPath<V, E> rdfPath = new NestedPath<V, E>(node);
                G groupKey = pathGrouper.apply(rdfPath);
                frontier.add(state, groupKey, rdfPath);
            });
        });
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((paths == null) ? 0 : paths.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NfaFrontier<?, ?, ?, ?> other = (NfaFrontier<?, ?, ?, ?>) obj;
        if (paths == null) {
            if (other.paths != null)
                return false;
        } else if (!paths.equals(other.paths))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Frontier [paths=" + paths + "]";
    }


}

