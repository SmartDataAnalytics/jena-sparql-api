package org.aksw.jena_sparql_api.analytics;

import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collector.core.AggBuilder;
import org.aksw.commons.collector.domain.ParallelAggregator;
import org.aksw.jena_sparql_api.utils.NodeUtils;
import org.apache.jena.graph.Node;

import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;

/**
 * Aggregation utilities for Jena Nodes
 * 
 * @author Claus Stadler
 *
 */
public class NodeAnalytics {

	public static ParallelAggregator<Node, Entry<Multiset<String>, Long>, ?> usedDatatypesAndNullCounts() {
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
	
	public static ParallelAggregator<Node, Multiset<String>, ?> usedDatatypes() {

		ParallelAggregator<Node, Multiset<String>, ?> result = AggBuilder.inputTransform(node -> NodeUtils.getDatatypeIri(node),
			AggBuilder.inputFilter(x -> x != null,
				AggBuilder.collectionSupplier(() -> (Multiset<String>)LinkedHashMultiset.<String>create())));
		
		return result;
	}
	
	
	public static ParallelAggregator<Node, Set<String>, ?> usedPrefixes(int targetSize) {
		ParallelAggregator<Node, Set<String>, ?> result =
			AggBuilder.inputFilter(node -> node.isURI(),
				AggBuilder.inputTransform(node -> node.getURI(),
					AggBuilder.naturalAccumulator(() -> new PrefixAccumulator(targetSize))));
				
		return result;
	}
}
