package org.aksw.jena_sparql_api.util.graph.alg;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

/**
 * Basic least common ancestor implementation. Does not perform any indexing or caching.
 * For a given pair of nodes for which to find the lca their respective sets of ancestors are
 * alternately expanded with their parents until there is an overlap or there are no more
 * unseen parents to expand.
 * <br />
 * Example Usage:
 * <pre>
 * Graph graph = model.getGraph();
 * GraphSuccessorFunction gsf = GraphSuccessorFunction.create(RDFS.subClassOf.asNode(), true); 
 * LeastCommonAncestor alg = new LeastCommonAncestor(graph, gsf);
 * Set<Node> lcas = alg.getLeastCommonAncestor(XSD.nonNegativeInteger.asNode(), XSD.decimal.asNode());
 * </pre>
 * 
 * 
 * 
 * @author raven
 *
 */
public class LeastCommonAncestor {
	protected Graph graph;
	protected GraphSuccessorFunction successorFn;
		
	public LeastCommonAncestor(Graph graph) {
		this(graph, GraphSuccessorFunction.create(RDFS.Nodes.subClassOf, true));
	}

	public LeastCommonAncestor(Graph graph, GraphSuccessorFunction successorFn) {
		super();
		this.graph = graph;
		this.successorFn = successorFn;
	}


	/**
	 * Get the least common ancestor (lca) of two given nodes w.r.t. a model
	 * and a hierarchy property. 
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public Set<Node> leastCommonAncestors(Node a, Node b) {
		Set<Node> result = leastCommonAncestors(a, b, node -> successorFn.apply(graph, node));
		return result;
	}
	
	
	/** Generic core of the algorithm */
	public static <T> Set<T> leastCommonAncestors(
			T a,
			T b,
			Function<? super T, ? extends Stream<? extends T>> getParents) {

		Set<T> result = new HashSet<>();

		Set<? extends T> aParents = Collections.singleton(a);
		Set<? extends T> bParents = Collections.singleton(b);

		Set<T> aAncestors = new HashSet<>(aParents);
		Set<T> bAncestors = new HashSet<>(bParents);

		boolean foundLcas = false;
		
		while (!aParents.isEmpty() || !bParents.isEmpty()) {
			foundLcas = calcOverlap(aParents.iterator(), aAncestors, bAncestors, result) > 0;
			if (foundLcas) { break; }

			foundLcas = calcOverlap(bParents.iterator(), bAncestors, aAncestors, result) > 0;
			if (foundLcas) { break; }

			// Get the parents but remove those that have already been seen
			// as ancestors
			aParents = aParents.stream().flatMap(getParents)
					.filter(item -> !aAncestors.contains(item))
					.collect(Collectors.toSet());
			
			bParents = bParents.stream().flatMap(getParents)
					.filter(item -> !bAncestors.contains(item))
					.collect(Collectors.toSet());
		}
		
		return result;
	}

	/**
	 * Iterate items in 'it' and add each seen item to 'seen'.
	 * For any item that is contained 'col' and that item also to 'out'.
	 * Return the number of items added to 'out'.
	 *  
	 */
	public static <T> int calcOverlap(
			Iterator<? extends T> it,
			Collection<T> seen,
			Collection<? extends T> col,
			Collection<T> out) {
		int result = 0;
		
		while (it.hasNext()) {
			T pa = it.next();
			seen.add(pa);
			if (col.contains(pa)) {
				out.add(pa);
				++result;
			}
		}

		return result;
	}
	
	
	
	public static void main(String[] args) {
		Model model = RDFDataMgr.loadModel("xsd-ontology.ttl");
		
		Graph graph = model.getGraph();
		GraphSuccessorFunction gsf = GraphSuccessorFunction.create(RDFS.subClassOf.asNode(), true);
		
		LeastCommonAncestor alg = new LeastCommonAncestor(graph, gsf);
		
		// Find any types that are lcas of the given ones
		Set<Node> actual = alg.leastCommonAncestors(XSD.nonNegativeInteger.asNode(), XSD.decimal.asNode());
		Set<Node> expected = Collections.singleton(XSD.decimal.asNode());

		// Traverse the type hierarchy up until we find a type that has a corresponding java class
		TypeMapper tm = TypeMapper.getInstance();
		
		Stream<Set<Node>> breadthOfParentsStream = BreadthFirstSearchLib.stream(expected, node -> gsf.apply(graph, node), Collectors::toSet);
		
//		Node type = expected.iterator().next();
//		RDFDatatype dtype = tm.getTypeByName(type.getURI());

		// Find the first breadth for which a java type is found
		Map<Node, RDFDatatype> javaTypeMap = breadthOfParentsStream.map(set -> {
			Map<Node, RDFDatatype> map = set.stream()
				.map(node -> Maps.immutableEntry(node, tm.getTypeByName(node.getURI())))
				.filter(e -> e.getValue() != null && e.getValue().getJavaClass() != null)
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
			return map;
		})
		.filter(map -> !map.isEmpty())
		.findFirst().orElse(null);

		System.out.println(javaTypeMap);
		System.out.println(actual);
	}
	
}
