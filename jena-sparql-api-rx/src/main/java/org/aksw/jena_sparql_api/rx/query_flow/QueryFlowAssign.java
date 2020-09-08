package org.aksw.jena_sparql_api.rx.query_flow;

import java.util.function.Function;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.apache.jena.sparql.function.FunctionEnv;

import io.reactivex.rxjava3.core.FlowableTransformer;

/**
 * Execution of assignment
 *
 * Based on {@link org.apache.jena.sparql.engine.iterator.QueryIterAssign}
 * @author raven
 *
 */
public class QueryFlowAssign
//    extends QueryFlowBase<Binding>
{
//    protected VarExprList exprs;
//
//    public QueryFlowAssign(FlowableEmitter<Binding> emitter, FunctionEnv execCxt, VarExprList exprs) {
//        super(emitter, execCxt);
//        this.exprs = exprs;
//    }
//
//    @Override
//    public void onNext(@NonNull Binding binding) {
//
//        emitter.onNext(b);
//    }
//

    public static Binding assign(Binding binding, VarExprList exprs, FunctionEnv execCxt) {
        BindingMap b = BindingFactory.create(binding);
        for (Var v : exprs.getVars()) {
            Node n = exprs.get(v, b, execCxt);
            if (n != null) {
                Node m = binding.get(v);
                if(m != null) {
                    if(!m.equals(n)) {
                        // TODO Just return from function?
                        throw new RuntimeException("Encountered incompatible mapping in join");
                    }
                } else {
                    b.add(v, n);
                }
            }
        }
        return b;
    }

    public static Function<Binding, Binding> createMapper(VarExprList exprs, FunctionEnv execCxt) {
        return binding -> assign(binding, exprs, execCxt);
    }

}