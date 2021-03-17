package org.aksw.jena_sparql_api.analytics;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.aksw.commons.collector.core.AggBuilder;
import org.aksw.commons.collector.domain.ParallelAggregator;
import org.aksw.jena_sparql_api.utils.NodeUtils;
import org.apache.jena.graph.Node;

/**
 * Aggregation utilities for Jena Nodes
 * 
 * @author Claus Stadler
 *
 */
public class NodeAnalytics {

	public static ParallelAggregator<Node, Entry<Set<String>, Long>, ?> usedDatatypesAndNullCounts() {
		return AggBuilder.inputBroadcast(
			usedDatatypes(),
			nullCount());
	}
	
	public static ParallelAggregator<Node, Long, ?> nullCount() {
		ParallelAggregator<Node, Long, ?> result =
			AggBuilder.inputFilter(x -> x == null,
				AggBuilder.counting());
			
		return result;
	}
	
	public static ParallelAggregator<Node, Set<String>, ?> usedDatatypes() {

		ParallelAggregator<Node, Set<String>, ?> result = AggBuilder.inputTransform(node -> NodeUtils.getDatatypeIri(node),
			AggBuilder.inputFilter(Objects::nonNull,
				AggBuilder.collectionSupplier(() -> (Set<String>)new HashSet<String>())));
		
		return result;
	}
	

//	public static ParallelAggregator<Node, Multiset<String>, ?> usedDatatypesWithCounts() {
//
//		ParallelAggregator<Node, Multiset<String>, ?> result = AggBuilder.inputTransform(node -> NodeUtils.getDatatypeIri(node),
//			AggBuilder.inputFilter(Objects::nonNull,
//				AggBuilder.collectionSupplier(() -> (Multiset<String>)LinkedHashMultiset.<String>create())));
//		
//		return result;
//	}

	public static ParallelAggregator<Node, Set<String>, ?> usedPrefixes(int targetSize) {
		ParallelAggregator<Node, Set<String>, ?> result =
			AggBuilder.inputFilter(Node::isURI,
				AggBuilder.inputTransform(Node::getURI,
					AggBuilder.naturalAccumulator(() -> new PrefixAccumulator(targetSize))));
				
		return result;
	}
}
