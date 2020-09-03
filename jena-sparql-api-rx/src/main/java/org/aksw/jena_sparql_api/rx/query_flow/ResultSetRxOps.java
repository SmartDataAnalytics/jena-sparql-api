package org.aksw.jena_sparql_api.rx.query_flow;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.aksw.jena_sparql_api.rx.ResultSetRx;
import org.aksw.jena_sparql_api.rx.ResultSetRxImpl;
import org.apache.jena.query.Query;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.binding.Binding;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableTransformer;

public class ResultSetRxOps {
    public static Function<ResultSetRx, ResultSetRx> createTransformForGroupBy(Query query, ExecutionContext execCxt) {
        FlowableTransformer<Binding, Binding> xform = QueryFlowOps.createTransformForGroupBy(query, execCxt);

        return upstream -> {
            List<Var> vars = query.getProjectVars();
            Flowable<Binding> downstream = upstream.getBindings().compose(xform);
            ResultSetRx r = ResultSetRxImpl.create(vars, downstream);
            return r;
        };
    }

    public static Function<ResultSetRx, ResultSetRx> createTransformFilter(ExecutionContext execCxt, String exprStr, PrefixMapping pm) {
        Predicate<Binding> filter = QueryFlowOps.createFilter(execCxt, exprStr, pm);

        return upstream -> {
            List<Var> vars = upstream.getVars();
            Flowable<Binding> downstream = upstream.getBindings().filter(filter::test);
            ResultSetRx r = ResultSetRxImpl.create(vars, downstream);
            return r;
        };
    }
}
