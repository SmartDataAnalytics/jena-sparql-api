package org.aksw.jena_sparql_api.rx.query_flow;

import java.util.Collection;
import java.util.List;

import org.aksw.commons.rx.util.RxUtils;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.ext.com.google.common.collect.Multimap;
import org.apache.jena.ext.com.google.common.collect.MultimapBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.aggregate.Accumulator;
import org.apache.jena.sparql.function.FunctionEnv;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableTransformer;

public class QueryFlowGroupBy
    extends QueryFlowBase<Binding>
{
    protected VarExprList groupVarExpr;
    protected List<ExprAggregator> aggregators;

    protected boolean noInput = true;

    protected Multimap<Binding, Pair<Var, Accumulator>> accumulators = MultimapBuilder.hashKeys().arrayListValues()
            .build();

    private static Pair<Var, Accumulator> placeholder = Pair.create((Var) null, (Accumulator) null);

    public QueryFlowGroupBy(
            FlowableEmitter<Binding> emitter,
            FunctionEnv execCxt,
            VarExprList groupVarExpr,
            List<ExprAggregator> aggregators
            ) {
        super(emitter, execCxt);
        this.groupVarExpr = groupVarExpr;
        this.aggregators = aggregators;
    }

    @Override
    public void onNext(Binding b) {

        this.noInput = false;

        boolean hasAggregators = (aggregators != null && !aggregators.isEmpty());
        boolean hasGroupBy = !groupVarExpr.isEmpty();
        // boolean noInput = ! iter.hasNext();

        // Case: No input.
        // 1/ GROUP BY - no rows.
        // 2/ No GROUP BY, e.g. COUNT=0, the results is one row always and not handled
        // here.

        // Case: there is input.
        // Phase 1 : Create keys and aggreators per key, and pump bindings through the
        // aggregators.
        // Multimap<Binding, Pair<Var, Accumulator>> accumulators =
        // MultimapBuilder.hashKeys().arrayListValues().build();

        Binding key = genKey(groupVarExpr, b, execCxt);

        if (!hasAggregators) {
            // Put in a dummy to remember the input.
            accumulators.put(key, placeholder);
            // continue;
        }

        // Create if does not exist.
        if (!accumulators.containsKey(key)) {
            for (ExprAggregator agg : aggregators) {
                Accumulator x = agg.getAggregator().createAccumulator();
                Var v = agg.getVar();
                accumulators.put(key, Pair.create(v, x));
            }
        }

        // Do the per-accumulator calculation.
        for (Pair<Var, Accumulator> pair : accumulators.get(key))
            pair.getRight().accumulate(b, execCxt);

        // Phase 2 : There was input and so there are some groups.
        // For each bucket, get binding, add aggregator values to the binding.
        // We used AccNull so there are always accumulators.

    }

    public void onComplete() {
        boolean hasAggregators = (aggregators != null && !aggregators.isEmpty());
        boolean hasGroupBy = !groupVarExpr.isEmpty();

        if (noInput) {
            if (hasGroupBy)
                // GROUP
                // return Iter.nullIterator() ;
                if (!hasAggregators) {
                    // No GROUP BY, no aggregators. One result row of no colums.
                    // return Iter.singleton(BindingFactory.binding());
                    emitter.onNext(BindingFactory.binding());
                }
            // No GROUP BY, has aggregators. Insert default values.
            BindingMap binding = BindingFactory.create();
            for (ExprAggregator agg : aggregators) {
                Node value = agg.getAggregator().getValueEmpty();
                if (value == null)
                    continue;
                Var v = agg.getVar();
                binding.add(v, value);
            }
            // return Iter.singleton(binding);
            emitter.onNext(binding);
        }

        for (Binding k : accumulators.keySet()) {
            Collection<Pair<Var, Accumulator>> accs = accumulators.get(k);
            BindingMap b = BindingFactory.create(k);

            for (Pair<Var, Accumulator> pair : accs) {
                NodeValue value = pair.getRight().getValue();
                if (value == null)
                    continue;
                Var v = pair.getLeft();
                b.add(v, value.asNode());
            }
            // results.add(b);
            emitter.onNext(b);
        }

        emitter.onComplete();

        // return results.iterator();
    }

    static private Binding genKey(VarExprList vars, Binding binding, FunctionEnv execCxt) {
        return copyProject(vars, binding, execCxt);
    }

    static private Binding copyProject(VarExprList vars, Binding binding, FunctionEnv execCxt) {
        // No group vars (implicit or explicit) => working on whole result set.
        // Still need a BindingMap to assign to later.
        BindingMap x = BindingFactory.create();
        for (Var var : vars.getVars()) {
            Node node = vars.get(var, binding, execCxt);
            // Null returned for unbound and error.
            if (node != null) {
                x.add(var, node);
            }
        }
        return x;
    }


    public static FlowableTransformer<Binding, Binding> createTransformer(
            FunctionEnv execCxt,
            VarExprList groupVarExpr,
            List<ExprAggregator> aggregators) {
        return RxUtils.createTransformer(emitter ->
            new QueryFlowGroupBy(emitter, execCxt, groupVarExpr, aggregators), BackpressureStrategy.BUFFER);
    }


}