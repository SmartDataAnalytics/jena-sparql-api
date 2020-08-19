package org.aksw.jena_sparql_api.rx.query_flow;

import org.apache.jena.sparql.engine.ExecutionContext;

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
    protected ExecutionContext execCxt;

    public QueryFlowBase(FlowableEmitter<T> emitter, ExecutionContext execCxt) {
        super(emitter);
        this.execCxt = execCxt;
    }
}