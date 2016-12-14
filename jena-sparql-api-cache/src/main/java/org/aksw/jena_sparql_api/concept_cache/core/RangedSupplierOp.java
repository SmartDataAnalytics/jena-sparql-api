package org.aksw.jena_sparql_api.concept_cache.core;

import org.aksw.jena_sparql_api.algebra.transform.TransformPushSlice;
import org.aksw.jena_sparql_api.util.collection.RangedSupplier;
import org.aksw.jena_sparql_api.utils.IteratorClosable;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIter;
import org.apache.jena.sparql.engine.iterator.QueryIterRoot;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.engine.main.OpExecutorFactory;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.util.iterator.ClosableIterator;

import com.google.common.collect.Range;

public class RangedSupplierOp
	implements RangedSupplier<Long, Binding>, OpAttribute
{
	protected Op op;
	protected Context context;


	public RangedSupplierOp(Op op, Context context) {
		super();
		this.op = op;
		this.context = context;
	}

	@Override
	public ClosableIterator<Binding> apply(Range<Long> range) {
		long offset = QueryUtils.rangeToOffset(range);
		long limit = QueryUtils.rangeToLimit(range);

		Op effectiveOp = new OpSlice(op, offset, limit);

		effectiveOp = Transformer.transform(TransformPushSlice.fn, effectiveOp);

		QueryIterator it = execute(effectiveOp, context);
		ClosableIterator<Binding> result = new IteratorClosable<>(it, () -> it.close());
		return result;
	}

//	@Override
//    public <X> X unwrap(Class<X> clazz, boolean reflexive) {
//    	@SuppressWarnings("unchecked")
//		X result = reflexive && this.getClass().isAssignableFrom(clazz)
//    		? (X)this
//    		: null;
//
//    	return result;
//    }

	/**
	 * This is partly a repetition of private functions in QC
	 * @param op
	 * @param context
	 * @return
	 */
	public static QueryIterator execute(Op op, Context context) {
		DatasetGraph dg = DatasetGraphFactory.create();
		OpExecutorFactory opExecutorFactory = QC.getFactory(context);
		ExecutionContext execCxt = new ExecutionContext(context, dg.getDefaultGraph(), dg, opExecutorFactory);
		QueryIter qIter = QueryIterRoot.create(execCxt);
		OpExecutor opExecutor = opExecutorFactory.create(execCxt);
		QueryIterator result = opExecutor.executeOp(op, qIter);

		return result;
	}

	@Override
	public Op getOp() {
		return op;
	}
}
