package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.HashMap;
import java.util.Map;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.engine.main.OpExecutorFactory;

public class OpExecutionFactoryViewCache
    implements OpExecutorFactory
{
    protected Map<Node, QueryExecutionFactory> serviceToQef;

    public OpExecutionFactoryViewCache(
            Map<Node, QueryExecutionFactory> serviceToQef) {
        super();
        this.serviceToQef = serviceToQef;
    }

    public OpExecutionFactoryViewCache() {
        super();
        this.serviceToQef = new HashMap<>();
    }

    public Map<Node, QueryExecutionFactory> getServiceMap() {
        return serviceToQef;
    }


    @Override
    public OpExecutor create(ExecutionContext execCxt) {
        return new OpExecutorViewCache(execCxt, serviceToQef);
    }

}
