package org.aksw.jena_sparql_api.rx;

import java.util.List;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import io.reactivex.rxjava3.core.Flowable;

public class ResultSetRxImpl
    implements ResultSetRx
{
    protected List<Var> vars;
    protected Flowable<Binding> bindings;

    public ResultSetRxImpl(List<Var> vars, Flowable<Binding> bindings) {
        super();
        this.vars = vars;
        this.bindings = bindings;
    }

    @Override
    public List<Var> getVars() {
        return vars;
    }

    @Override
    public Flowable<Binding> getBindings() {
        return bindings;
    }


    public static ResultSetRxImpl create(List<Var> vars, Flowable<Binding> bindings) {
        return new ResultSetRxImpl(vars, bindings);
    }
}
