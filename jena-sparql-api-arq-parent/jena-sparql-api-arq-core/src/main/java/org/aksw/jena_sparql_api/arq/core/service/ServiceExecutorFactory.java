package org.aksw.jena_sparql_api.arq.core.service;

import java.util.function.Supplier;

import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;

/**
 * Interface for custom handling of service execution requests.
 * If {@link #createExecutor(OpService, Binding, ExecutionContext)} cannot handle
 * the execution request then it needs to return null.
 * Otherwise, a supplier with the corresponding QueryIterator needs to be supplied.
 * The supplier is invoked exactly once per request.
 *
 */
public interface ServiceExecutorFactory {
    Supplier<QueryIterator> createExecutor(OpService op, Binding binding, ExecutionContext context);
}