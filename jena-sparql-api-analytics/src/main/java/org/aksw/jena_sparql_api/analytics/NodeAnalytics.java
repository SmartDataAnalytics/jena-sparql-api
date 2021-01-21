package org.aksw.jena_sparql_api.analytics;

import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.aksw.jena_sparql_api.mapper.parallel.AggBuilder;
import org.aksw.jena_sparql_api.mapper.parallel.ParallelAggregator;
import org.aksw.jena_sparql_api.utils.NodeUtils;
import org.apache.jena.graph.Node;

import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;

public class NodeAnalytics {

	public static ParallelAggregator<Node, Entry<Multiset<String>, Long>, ?> usedDatatypesAndNullCounts() {
		return AggBuilder.inputBroadcast(
			usedDatatypes(),
			nullCount());
	}
	
	public static ParallelAggregator<Node, Long, ?> nullCount() {
		ParallelAggregator<Node, Long, ?> result =
			AggBuilder.inputFilter(Objects::nonNull,
				AggBuilder.counting());
			
		return result;
	}
	
	public static ParallelAggregator<Node, Multiset<String>, ?> usedDatatypes() {

		ParallelAggregator<Node, Multiset<String>, ?> result = AggBuilder.inputTransform(NodeUtils::getDatatypeIri,
			AggBuilder.inputFilter(Objects::nonNull,
				AggBuilder.collectionSupplier(() -> (Multiset<String>)LinkedHashMultiset.<String>create())));
		
		return result;
	}
	
	
	public static ParallelAggregator<Node, Set<String>, ?> usedPrefixes(int targetSize) {
		ParallelAggregator<Node, Set<String>, ?> result =
			AggBuilder.inputFilter(Node::isURI,
				AggBuilder.inputTransform(Node::getURI,
					AggBuilder.naturalAccumulator(() -> new PrefixAccumulator(targetSize))));
				
		return result;
	}
}
