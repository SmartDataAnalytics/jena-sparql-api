package org.aksw.jena_sparql_api.rx.query_flow;

import org.apache.jena.sparql.function.FunctionEnv;

import io.reactivex.rxjava3.core.FlowableEmitter;

/**
 * Abstract base class for query (execution) flows.
 *
 * @author raven
 *
 * @param <T>
 */
public abstract class QueryFlowBase<T>
    extends FlowBase<T>
{
    protected FunctionEnv execCxt;

    public QueryFlowBase(FlowableEmitter<T> emitter, FunctionEnv execCxt) {
        super(emitter);
        this.execCxt = execCxt;
    }
}