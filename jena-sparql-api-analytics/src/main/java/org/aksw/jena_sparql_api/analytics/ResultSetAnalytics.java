package org.aksw.jena_sparql_api.analytics;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.aksw.jena_sparql_api.mapper.parallel.AggBuilder;
import org.aksw.jena_sparql_api.mapper.parallel.ParallelAggregator;
import org.aksw.jena_sparql_api.utils.NodeUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

public class ResultSetAnalytics {

    public static ParallelAggregator<Binding, Map<Var, Set<String>>, ?> usedPrefixes(int targetSize) {

    	ParallelAggregator<Binding, Map<Var, Set<String>>, ?> result = 
    	AggBuilder.fromNaturalAccumulator(() -> new PrefixAccumulator(targetSize))
    		.withInputTransform(Node::getURI)
    		.withInputFilter(Node::isURI)
    		.withInputSplit((Binding b) -> Sets.newHashSet(b.vars()), Binding::get)
    		.getAsParallelAggregator();
    	
    	return result;
    }
    
    public static ParallelAggregator<Binding, Map<Var, Multiset<String>>, ?> usedDatatypes() {

    	ParallelAggregator<Binding, Map<Var, Multiset<String>>, ?> result = 
    	AggBuilder.fromCollectionSupplier(() -> (Multiset<String>)LinkedHashMultiset.<String>create())
			.withInputFilter(Objects::nonNull)
    		.withInputTransform(NodeUtils::getDatatypeIri)
    		.withInputSplit((Binding b) -> Sets.newHashSet(b.vars()), Binding::get)
    		.getAsParallelAggregator();
    	
    	return result;
    }


}
