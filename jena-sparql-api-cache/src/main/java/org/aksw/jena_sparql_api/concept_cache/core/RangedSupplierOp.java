package org.aksw.jena_sparql_api.concept_cache.core;

import org.aksw.jena_sparql_api.util.collection.RangedSupplier;
import org.aksw.jena_sparql_api.utils.IteratorClosable;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterRoot;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.util.iterator.ClosableIterator;

import com.google.common.collect.Range;

public class RangedSupplierOp implements RangedSupplier<Long, Binding> {
	protected OpExecutor opExecutor;
	protected Op op;
	protected Context context;

	public RangedSupplierOp(OpExecutor opExecutor, Op op) {
	}

	public RangedSupplierOp(OpExecutor opExecutor, Op op, Context context) {
		super();
		this.opExecutor = opExecutor;
		this.op = op;
		this.context = context;
	}

	@Override
	public ClosableIterator<Binding> apply(Range<Long> range) {
		long offset = QueryUtils.rangeToOffset(range);
		long limit = QueryUtils.rangeToLimit(range);

		OpSlice effectiveOp = new OpSlice(op, offset, limit);

		DatasetGraph dg = DatasetGraphFactory.create();
		ExecutionContext execCxt = new ExecutionContext(context, dg.getDefaultGraph(), dg, QC.getFactory(context)) ;

		QueryIterator it = opExecutor.executeOp(effectiveOp, QueryIterRoot.create(execCxt));
		ClosableIterator<Binding> result = new IteratorClosable<>(it, () -> it.close());
		return result;
	}

}
