package org.aksw.jena_sparql_api_sparql_path2;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.jena.graph.Node;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

class Frontier<V> {
    protected Map<V, Multimap<Node, NestedRdfPath>> paths = new HashMap<V, Multimap<Node, NestedRdfPath>>();

    public Set<V> getCurrentStates() {
        return paths.keySet();
    }

    public Multimap<Node, NestedRdfPath> getPaths(V state) {
        Multimap<Node, NestedRdfPath> result = paths.get(state);
        return result;
    }

    protected Multimap<Node, NestedRdfPath> getOrCreateStateInfo(V state) {
        Multimap<Node, NestedRdfPath> result = paths.get(state);
        if(result == null) {
            result = HashMultimap.create();
            paths.put(state, result);
        }

        return result;
    }

    public void add(V state, NestedRdfPath path) {
        Node node = path.getCurrent();
        Multimap<Node, NestedRdfPath> nodeToPath = getOrCreateStateInfo(state);
        nodeToPath.put(node, path);
    }

    public boolean isEmpty() {
        boolean result = paths.entrySet().stream().allMatch(x -> x.getValue().isEmpty());
        return result;
    }
}

