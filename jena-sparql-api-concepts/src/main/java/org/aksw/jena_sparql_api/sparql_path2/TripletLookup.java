package org.aksw.jena_sparql_api.sparql_path2;

import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.utils.model.Triplet;

import com.google.common.collect.Multimap;

public interface TripletLookup<T, G, V, E>
//	extends BiFunction<
//	    LabeledEdge<Integer, PredicateClass>,
//	    Multimap<Node, NestedPath<Node, Triplet<Node, Node>>>,
//        Map<Node, Set<Triplet<Node, Node>>>
//    > getMatchingTriplets = null;
{
	Map<V, Set<Triplet<V, E>>> lookup(T transition, Multimap<G, NestedPath<V, E>> groupedPaths);
}
