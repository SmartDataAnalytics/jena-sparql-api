package org.aksw.jena_sparql_api.views.index;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.aksw.jena_sparql_api.concept_cache.collection.FeatureMap;
import org.apache.jena.sparql.algebra.Op;

public class OpViewMatcherImpl
	implements OpViewMatcher
{
    protected Function<Op, Set<?>> itemFeatureExtractor;
    protected FeatureMap<Op, > featureMap;
	

	@Override
	public void add(Op op) {
		Set<?> features = itemFeatureExtractor.apply(op);
		
		featureMap
		
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<Entry<Op, OpVarMap>> lookup(Op op) {
		// TODO Auto-generated method stub
		return null;
	}
	

}
