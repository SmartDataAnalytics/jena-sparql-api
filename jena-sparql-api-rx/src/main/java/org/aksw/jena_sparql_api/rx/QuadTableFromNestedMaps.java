package org.aksw.jena_sparql_api.rx;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.mem.QuadTable;

/**
 * This class looks similar to jena's QuadTable implementations, but I was not able to figure
 * out how to modify those in order to use LinkedHashMaps in order to retain quad insert order
 * 
 * @author Claus Stadler, Oct 30, 2018
 *
 */
public class QuadTableFromNestedMaps
	implements QuadTable
{
	protected Map<Node, Map<Node, Map<Node, Map<Node, Quad>>>> store;

	public QuadTableFromNestedMaps() {
		super();
		store = newMap();
	}
	
	protected <K, V> Map<K, V> newMap() {
		return new LinkedHashMap<>();
	}
	
	@Override
	public void add(Quad quad) {
		store.computeIfAbsent(quad.getGraph(), g -> newMap())
			.computeIfAbsent(quad.getSubject(), s -> newMap())
			.computeIfAbsent(quad.getPredicate(), p -> newMap())
			.computeIfAbsent(quad.getObject(), o -> quad);		
	}

	@Override
	public void delete(Quad quad) {
		Map<Node, Map<Node, Map<Node, Quad>>> sm = store.getOrDefault(quad.getGraph(), Collections.emptyMap());
		Map<Node, Map<Node, Quad>> pm = sm.getOrDefault(quad.getSubject(), Collections.emptyMap());
		Map<Node, Quad> om = pm.getOrDefault(quad.getPredicate(), Collections.emptyMap());

		if(om.containsKey(quad.getObject())) {
			om.remove(quad.getObject());
			if(om.isEmpty()) { pm.remove(quad.getPredicate()); }
			if(pm.isEmpty()) { sm.remove(quad.getSubject()); }
			if(sm.isEmpty()) { store.remove(quad.getGraph()); }
		}
	}

	@Override
	public void begin(ReadWrite readWrite) {
	}

	@Override
	public void commit() {
	}

	@Override
	public void end() {
	}
	
	// Create a stream of matching values from a stream of maps and a key that may be a wildcard
	public static <K, V> Stream<V> match(Stream<Map<K, V>> in, Predicate<? super K> isAny, K k) {
		boolean any = isAny.test(k);
		Stream<V> result = any
				? in.flatMap(m -> m.values().stream())
				: in.flatMap(m -> m.containsKey(k) ? Stream.of(m.get(k)) : Stream.empty());

		return result;
	}
	
	public static boolean isWildcard(Node n) {
		return n == null || Node.ANY.equals(n);
	}
	
	@Override
	public Stream<Quad> find(Node g, Node s, Node p, Node o) {
		Stream<Quad> result =
				match(
					match(
						match(
							match(Stream.of(store), QuadTableFromNestedMaps::isWildcard, g),
							QuadTableFromNestedMaps::isWildcard, s),
						QuadTableFromNestedMaps::isWildcard, p),
					QuadTableFromNestedMaps::isWildcard, o);
		
		return result;
	}
	
	@Override
	public Stream<Node> listGraphNodes() {
		return store.keySet().stream()
				.filter(node -> !Quad.isDefaultGraph(node));
	}
}

//
//Node gm = g == null ? Node.ANY : g;
//Triple t = Triple.createMatch(s, p, o);
//
//Stream<Quad> result = quads.stream()
//			.filter(q -> gm.matches(q.getGraph()) && t.matches(q.asTriple()));

//
//@Override
//public Graph getDefaultGraph() {
//	return GraphView.createDefaultGraph(this);
//}
//
//@Override
//public Graph getGraph(Node graphNode) {
//	return GraphView.createNamedGraph(this, graphNode);
//}
//
//@Override
//public void addGraph(Node graphName, Graph graph) {
//	graph.find().forEachRemaining(t -> add(new Quad(graphName, t)));
//}


//@Override
//public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
//	Iterator<Quad> result = findStream(g, s, p, o).iterator();
//	return result;
//}
//
//@Override
//public Iterator<Quad> findNG(Node g, Node s, Node p, Node o) {
//	Node gm = g == null ? Node.ANY : g;
//
//	Iterator<Quad> result = Quad.isDefaultGraph(gm)
//			? NiceIterator.emptyIterator()
//			: findStream(gm, s, p, o)
//				.filter(q -> !Quad.isDefaultGraph(q.getGraph()))
//				.iterator();
//	return result;
//}


