package org.aksw.jena_sparql_api.rx.query_flow;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.aksw.jena_sparql_api.rx.ResultSetRx;
import org.aksw.jena_sparql_api.rx.ResultSetRxImpl;
import org.apache.jena.query.Query;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.function.FunctionEnv;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableTransformer;

public class ResultSetRxOps {
    public static Function<ResultSetRx, ResultSetRx> createTransformForGroupBy(Query query, FunctionEnv execCxt) {
        FlowableTransformer<Binding, Binding> xform = QueryFlowOps.createTransformForGroupBy(query, execCxt);

        return upstream -> {
            List<Var> downstreamVars = query.isQueryResultStar()
                    ? upstream.getVars()
                    : query.getProjectVars();

            Flowable<Binding> downstream = upstream.getBindings().compose(xform);
            ResultSetRx r = ResultSetRxImpl.create(downstreamVars, downstream);
            return r;
        };
    }

    public static Function<ResultSetRx, ResultSetRx> createTransformFilter(Predicate<Binding> filter) {
        return upstream -> {
            List<Var> vars = upstream.getVars();
            Flowable<Binding> downstream = upstream.getBindings().filter(filter::test);
            ResultSetRx r = ResultSetRxImpl.create(vars, downstream);
            return r;
        };
    }

    public static Function<ResultSetRx, ResultSetRx> createTransformFilter(ExprList exprs, FunctionEnv execCxt) {
        Predicate<Binding> filter = QueryFlowOps.createFilter(exprs, execCxt);
        return createTransformFilter(filter);
    }

    public static Function<ResultSetRx, ResultSetRx> createTransformFilter(String exprStr, PrefixMapping pm, FunctionEnv execCxt) {
        Predicate<Binding> filter = QueryFlowOps.createFilter(exprStr, pm, execCxt);
        return createTransformFilter(filter);
    }

}
