package org.aksw.jena_sparql_api.rx.query_flow;

/**
 * Execution of projection
 *
 * Based on {@link org.apache.jena.sparql.engine.iterator.QueryIterAssign}
 * @author raven
 *
 */
//public class QueryFlowProject
//    extends QueryFlowBase<Binding>
//{
//    protected Collection<Var> vars;
//
//    public QueryFlowProject(FlowableEmitter<Binding> emitter, FunctionEnv execCxt, Collection<Var> vars) {
//        super(emitter, execCxt);
//        this.vars = vars;
//    }
//
//    @Override
//    public void onNext(@NonNull Binding binding) {
//        Binding b = new BindingProject(vars, binding);
//        emitter.onNext(b);
//    }
//
//    public static FlowableTransformer<Binding, Binding> createTransformer(
//            FunctionEnv execCxt,
//            Collection<Var> vars) {
//        return upstream -> upstream.map(binding -> new BindingProject(vars, binding));
//    }
//
//}