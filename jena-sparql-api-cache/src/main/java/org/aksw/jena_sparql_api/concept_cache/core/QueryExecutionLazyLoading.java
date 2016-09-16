package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.List;

import org.aksw.jena_sparql_api.core.ResultSetCloseable;
import org.aksw.jena_sparql_api.util.collection.RangedSupplierLazyLoadingListCache;
import org.aksw.jena_sparql_api.utils.ResultSetUtils;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.util.iterator.ClosableIterator;

import com.google.common.collect.Range;

public class QueryExecutionLazyLoading {
	protected List<String> varNames;
	protected RangedSupplierLazyLoadingListCache<Binding> cache;
	protected Range<Long> range;
	
	protected transient ClosableIterator<Binding> execution;
	
	public QueryExecutionLazyLoading(List<String> varNames, RangedSupplierLazyLoadingListCache<Binding> cache,
			Range<Long> range) {
		super();
		this.varNames = varNames;
		this.cache = cache;
		this.range = range;
	}


	public ResultSet execSelect() {
		execution = cache.apply(range);		
		ResultSet rs = ResultSetUtils.create(varNames, execution);
		ResultSet result = new ResultSetCloseable(rs, () -> execution.close());
		return result;
	}
}
