package org.aksw.jena_sparql_api.rx.query_flow;

import java.util.Collection;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingProject;
import org.apache.jena.sparql.function.FunctionEnv;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableTransformer;

/**
 * Execution of projection
 *
 * Based on {@link org.apache.jena.sparql.engine.iterator.QueryIterAssign}
 * @author raven
 *
 */
public class QueryFlowProject
    extends QueryFlowBase<Binding>
{
    protected Collection<Var> vars;

    public QueryFlowProject(FlowableEmitter<Binding> emitter, FunctionEnv execCxt, Collection<Var> vars) {
        super(emitter, execCxt);
        this.vars = vars;
    }

    @Override
    public void onNext(@NonNull Binding binding) {
        Binding b = new BindingProject(vars, binding);
        emitter.onNext(b);
    }

    public static FlowableTransformer<Binding, Binding> createTransformer(
            FunctionEnv execCxt,
            Collection<Var> vars) {
        return RxUtils.createTransformer(emitter -> new QueryFlowProject(emitter, execCxt, vars));
    }

}