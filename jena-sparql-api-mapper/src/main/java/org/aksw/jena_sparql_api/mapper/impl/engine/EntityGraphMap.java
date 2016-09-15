package org.aksw.jena_sparql_api.mapper.impl.engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.graph.GraphFactory;

/**
 * Keeps track of associations between objects and triples
 *
 */
public class EntityGraphMap<K> {
    protected Graph graph;

    protected Map<K, Graph> keyToGraph = new HashMap<K, Graph>();
    protected Map<Triple, Set<K>> tripleToKeys = new HashMap<Triple, Set<K>>();

    public void putAll(Graph graph, K key) {
        for(Triple triple : graph.find(Node.ANY, Node.ANY, Node.ANY).toSet()) {
            put(triple, key);
        }
    }

    public void removeAll(Graph graph, K key) {
        for(Triple triple : graph.find(Node.ANY, Node.ANY, Node.ANY).toSet()) {
            remove(triple, key);
        }
    }

    public void clearGraph(K key) {
        Graph graph = keyToGraph.get(key);
        if(graph != null) {
            removeAll(graph, key);
        }
    }

    public void put(Triple triple, K key) {
    	tripleToKeys.computeIfAbsent(triple, (t) -> new HashSet<>()).add(key);
    	
//        Set<Object> entities = tripleToKeys.get(triple);
//        if(entities == null) {
//            entities = Sets.newIdentityHashSet();
//            tripleToKeys.put(triple, entities);
//        }
//        entities.add(key);

        Graph graph = keyToGraph.get(key);
        if(graph == null) {
            graph = GraphFactory.createDefaultGraph();
            keyToGraph.put(key, graph);
        }
        graph.add(triple);
    }

    public void remove(Triple triple, K key) {
        Set<K> keys = tripleToKeys.get(key);
        if(keys != null) {
            keys.remove(keys);
        }

        Graph g = keyToGraph.get(key);
        if(g != null) {
            g.remove(triple.getSubject(), triple.getPredicate(), triple.getObject());

            if(graph.isEmpty()) {
                keyToGraph.remove(key);
            }
        }
    }

    /**
     * Globally remove a triple; i.e. removes the triple from
     * all referencing entities
     *
     * @param triple
     */
    public void removeTriple(Triple triple) {
        Set<K> keys = tripleToKeys.get(triple);
        if(keys != null) {
            keys.remove(triple);

            for(Object entity : keys) {
                Graph graph = keyToGraph.get(entity);
                if(graph != null) {
                    graph.remove(triple.getSubject(), triple.getPredicate(), triple.getObject());
                }
            }
        }
    }

    /**
     * Removes an entity and all triples associated with it
     * @param entity
     */
    public void removeEntity(Object entity) {
        Graph graph = keyToGraph.get(entity);
        keyToGraph.remove(entity);

        if(graph != null) {
            for(Triple triple : graph.find(Node.ANY, Node.ANY, Node.ANY).toSet()) {
                Set<K> keys = tripleToKeys.get(triple);
                keys.remove(entity);
            }
        }
    }

    public Graph getGraphForKey(Object key) {
        Graph result = keyToGraph.get(key);
        return result;
    }


}