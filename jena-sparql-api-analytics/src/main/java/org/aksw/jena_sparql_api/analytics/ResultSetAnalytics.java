package org.aksw.jena_sparql_api.analytics;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collections.SetUtils;
import org.aksw.commons.collector.core.AggBuilder;
import org.aksw.commons.collector.domain.ParallelAggregator;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

public class ResultSetAnalytics {

	/**
	 * Given an aggregator for {@link Node} input, create a new aggregator that invokes it on-demand for each encountered variable
	 * in bindings.
	 *  
	 * Important Note: This method only instantiates accumulators when encountering variables.
	 * If e.g. a result set does not bind a variable then it won't be part of the output map.
	 * Use {@link #aggPerVar(Set, ParallelAggregator)} to specify the set of variables for which to perform the given base aggregation.
	 * 
	 * @param <O>
	 * @param nodeAgg
	 * @return
	 */
	public static <O> ParallelAggregator<Binding, Map<Var, O>, ?> aggPerVar(ParallelAggregator<Node, O, ?> nodeAgg) {
		return AggBuilder.inputSplit((Binding b) -> SetUtils.newLinkedHashSet(b.vars()), Binding::get,
				nodeAgg);
//		return AggBuilder.inputSplit((Binding b) -> Sets.newHashSet(b.vars()), (Binding b, Var v) -> b.get(v),
//				nodeAgg);
	}

	public static <O> ParallelAggregator<Binding, Map<Var, O>, ?> aggPerVar(Set<Var> staticVars, ParallelAggregator<Node, O, ?> nodeAgg) {
		// Create a copy of the set to avoid serialization issues
		// For example, passing a immutable set from scala makes the lambda non-serializable
		Set<Var> staticVarCopy = new LinkedHashSet<>(staticVars);

		// Note that the key set includes all static variables (in addition to those mentioned in the binding)
		// in order to allow aggregation over null values!
		return AggBuilder.inputSplit(staticVarCopy, true, (Binding b) -> {
			Set<Var> r = new LinkedHashSet<>();
			r.addAll(staticVarCopy);
			b.vars().forEachRemaining(r::add);
			return r;
		}, Binding::get, nodeAgg);
//		return AggBuilder.inputSplit(staticVarCopy, true, (Binding b) -> Sets.union(staticVarCopy, Sets.newHashSet(b.vars())), (Binding b, Var v) -> b.get(v),
//				nodeAgg);

//		return AggBuilder.inputSplit(staticVarCopy, true, (Binding b) -> Sets.newHashSet(b.vars()), Binding::get,
//				nodeAgg);
	}

	
    public static ParallelAggregator<Binding, Map<Var, Set<String>>, ?> usedPrefixes(int targetSize) {
    	return aggPerVar(NodeAnalytics.usedPrefixes(targetSize));
    }
    
    public static ParallelAggregator<Binding, Map<Var, Set<String>>, ?> usedDatatypes() {
    	return aggPerVar(NodeAnalytics.usedDatatypes());
    }


    // Null counting only makes sense for a-priori provided variables!
    public static ParallelAggregator<Binding, Map<Var, Entry<Set<String>, Long>>, ?> usedDatatypesAndNullCounts(Set<Var> staticVars) {
    	return aggPerVar(staticVars,
    			NodeAnalytics.usedDatatypesAndNullCounts());
    }

    
//    public static ParallelAggregator<Binding, Map<Var, Multiset<String>>, ?> usedDatatypes() {
//    	return aggPerVar(NodeAnalytics.usedDatatypes());
//    }
//
//
//    // Null counting only makes sense for a-priori provided variables!
//    public static ParallelAggregator<Binding, Map<Var, Entry<Multiset<String>, Long>>, ?> usedDatatypesAndNullCounts(Set<Var> staticVars) {
//    	return aggPerVar(staticVars,
//    			NodeAnalytics.usedDatatypesAndNullCounts());
//    }

}
