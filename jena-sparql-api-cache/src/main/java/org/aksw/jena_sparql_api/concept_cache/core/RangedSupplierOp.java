package org.aksw.jena_sparql_api.concept_cache.core;

import org.aksw.commons.rx.range.RangedSupplier;
import org.aksw.jena_sparql_api.algebra.transform.TransformPushSlice;
import org.aksw.jena_sparql_api.util.RewriteUtils;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterRoot;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.engine.main.OpExecutorFactory;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.util.Context;

import com.google.common.collect.Range;

import io.reactivex.rxjava3.core.Flowable;

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
    public Flowable<Binding> apply(Range<Long> range) {
        long offset = QueryUtils.rangeToOffset(range);
        long limit = QueryUtils.rangeToLimit(range);

        Op effectiveOp = new OpSlice(op, offset, limit);

        // The base op may be a service reference (or some other expression)
        // Push down the newly added slice for best performance
        //effectiveOp = Transformer.transform(TransformPushSlice.fn, effectiveOp);

        // TODO Make this transformation configurable
        Op finalEffectiveOp = RewriteUtils.transformUntilNoChange(effectiveOp, op -> Transformer.transform(TransformPushSlice.fn, op));


        //QueryIterator it = execute(effectiveOp, context);
        //Stream<Binding> result = StreamUtils.stream(it);

        Flowable<Binding> result = Flowable.fromIterable(() -> execute(finalEffectiveOp, context));
        //ClosableIterator<Binding> result = new IteratorClosable<>(it, () -> it.close());
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
        QueryIterator qIter = QueryIterRoot.create(execCxt);
        OpExecutor opExecutor = opExecutorFactory.create(execCxt);
        QueryIterator result = opExecutor.executeOp(op, qIter);

        return result;
    }

    @Override
    public Op getOp() {
        return op;
    }
}
