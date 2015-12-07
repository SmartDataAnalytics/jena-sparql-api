package org.aksw.jena_sparql_api.mapper.impl.engine;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.graph.GraphFactory;
import com.hp.hpl.jena.sparql.util.graph.GraphUtils;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Keeps track of associations between objects and triples
 *
 */
public class EntityGraphMap {
	protected Graph graph;

	protected Map<Object, Graph> entityToGraph = new IdentityHashMap<Object, Graph>();
	protected Map<Triple, Set<Object>> tripleToEntities = new HashMap<Triple, Set<Object>>();

	public void putAll(Graph graph, Object entity) {
		for(Triple triple : graph.find(Node.ANY, Node.ANY, Node.ANY).toSet()) {
			put(triple, entity);
		}
	}

	public void removeAll(Graph graph, Object entity) {
		for(Triple triple : graph.find(Node.ANY, Node.ANY, Node.ANY).toSet()) {
			remove(triple, entity);
		}
	}

	public void clearGraph(Object entity) {
		Graph graph = entityToGraph.get(entity);
		if(graph == null) {
			removeAll(graph, entity);
		}
	}

	public void put(Triple triple, Object entity) {
		Set<Object> entities = tripleToEntities.get(triple);
		if(entities == null) {
			entities = Sets.newIdentityHashSet();
		}
		entities.add(triple);

		Graph graph = entityToGraph.get(entity);
		if(graph == null) {
			graph = GraphFactory.createDefaultGraph();
			entityToGraph.put(entity, graph);
			graph.add(triple);
		}
	}

	public void remove(Triple triple, Object entity) {
		Set<Object> entities = tripleToEntities.get(triple);
		if(entities != null) {
			entities.remove(entities);
		}

		Graph g = entityToGraph.get(entity);
		if(g != null) {
			g.remove(triple.getSubject(), triple.getPredicate(), triple.getObject());

			if(graph.isEmpty()) {
				entityToGraph.remove(entity);
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
		Set<Object> entities = tripleToEntities.get(triple);
		if(entities != null) {
			entities.remove(triple);

			for(Object entity : entities) {
				Graph graph = entityToGraph.get(entity);
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
		Graph graph = entityToGraph.get(entity);
		entityToGraph.remove(entity);

		if(graph != null) {
			for(Triple triple : graph.find(Node.ANY, Node.ANY, Node.ANY).toSet()) {
				Set<Object> entities = tripleToEntities.get(triple);
				entities.remove(entity);
			}
		}
	}

	public Graph getGraphForEntity(Object entity) {
		Graph result = entityToGraph.get(entity);
		return result;
	}


}