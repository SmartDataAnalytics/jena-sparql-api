package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.Iterator;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.util.collection.RangedSupplier;
import org.aksw.jena_sparql_api.utils.IteratorClosable;
import org.aksw.jena_sparql_api.utils.IteratorResultSetBinding;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.util.iterator.ClosableIterator;

import com.google.common.collect.Range;

public class RangedSupplierQuery
	implements RangedSupplier<Long, Binding>
{
	protected QueryExecutionFactory qef;
	protected Query query;
	
	public RangedSupplierQuery(QueryExecutionFactory qef, Query query) {
		super();
		this.qef = qef;
		this.query = query;
	}

	@Override
	public ClosableIterator<Binding> apply(Range<Long> range) {
		Query clone = query.cloneQuery();
		QueryUtils.applyRange(clone, range);
		
		QueryExecution qe = qef.createQueryExecution(clone);
		ResultSet rs = qe.execSelect();
		
		Iterator<Binding> it = new IteratorResultSetBinding(rs);
		
		ClosableIterator<Binding> result = new IteratorClosable<>(it, () -> qe.close());
		return result;
	}

}
