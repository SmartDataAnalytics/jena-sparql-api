package org.aksw.jena_sparql_api.analytics;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collector.core.AggBuilder;
import org.aksw.commons.collector.domain.ParallelAggregator;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

public class ResultSetAnalytics {

	public static <O> ParallelAggregator<Binding, Map<Var, O>, ?> aggPerVar(ParallelAggregator<Node, O, ?> nodeAgg) {
		return AggBuilder.inputSplit((Binding b) -> Sets.newHashSet(b.vars()), Binding::get,
				nodeAgg);
	}

	public static <O> ParallelAggregator<Binding, Map<Var, O>, ?> aggPerVar(Set<Var> staticVars, ParallelAggregator<Node, O, ?> nodeAgg) {
		// Create a copy of the set to avoid serialization issues
		// For example, passing a immutable set from scala makes the lambda non-serializable
		Set<Var> staticVarCopy = new LinkedHashSet<>(staticVars);
		
		return AggBuilder.inputSplit((Binding b) -> staticVarCopy, Binding::get,
				nodeAgg);
	}

	
    public static ParallelAggregator<Binding, Map<Var, Set<String>>, ?> usedPrefixes(int targetSize) {
    	return aggPerVar(NodeAnalytics.usedPrefixes(targetSize));
    }
    
    public static ParallelAggregator<Binding, Map<Var, Multiset<String>>, ?> usedDatatypes() {
    	return aggPerVar(NodeAnalytics.usedDatatypes());
    }


    // Null counting only makes sense for a-priori provided variables!
    public static ParallelAggregator<Binding, Map<Var, Entry<Multiset<String>, Long>>, ?> usedDatatypesAndNullCounts(Set<Var> staticVars) {
    	return aggPerVar(staticVars,
    			NodeAnalytics.usedDatatypesAndNullCounts());
    }

}
