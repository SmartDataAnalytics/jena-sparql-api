package org.aksw.jena_sparql_api_sparql_path2;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

class Frontier<S, V, E> {
    protected Map<S, Multimap<V, NestedPath<V, E>>> paths = new HashMap<>();

    public Set<S> getCurrentStates() {
        return paths.keySet();
    }

    public Multimap<V, NestedPath<V, E>> getPaths(S state) {
        Multimap<V, NestedPath<V, E>> result = paths.get(state);
        return result;
    }

    protected Multimap<V, NestedPath<V, E>> getOrCreateStateInfo(S state) {
        Multimap<V, NestedPath<V, E>> result = paths.get(state);
        if(result == null) {
            result = HashMultimap.create();
            paths.put(state, result);
        }

        return result;
    }

    public void add(S state, NestedPath<V, E> path) {
        V node = path.getCurrent();
        Multimap<V, NestedPath<V, E>> nodeToPath = getOrCreateStateInfo(state);
        nodeToPath.put(node, path);
    }


    public boolean isEmpty() {
        boolean result = paths.entrySet().stream().allMatch(x -> x.getValue().isEmpty());
        return result;
    }

    public static <S, V, E> void addAll(Frontier<S, V, E> frontier, Set<S> states, V node) {
        for(S state : states) {
            NestedPath<V, E> rdfPath = new NestedPath<V, E>(node);
            frontier.add(state, rdfPath);
        }
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
        Frontier other = (Frontier) obj;
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

