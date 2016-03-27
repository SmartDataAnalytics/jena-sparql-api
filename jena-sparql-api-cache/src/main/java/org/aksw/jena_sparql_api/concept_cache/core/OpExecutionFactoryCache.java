package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.HashMap;
import java.util.Map;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.engine.main.OpExecutorFactory;

public class OpExecutionFactoryCache
    implements OpExecutorFactory
{
    protected Map<Node, QueryExecutionFactory> serviceToQef;

    public OpExecutionFactoryCache(
            Map<Node, QueryExecutionFactory> serviceToQef) {
        super();
        this.serviceToQef = serviceToQef;
    }

    public OpExecutionFactoryCache() {
        super();
        this.serviceToQef = new HashMap<>();
    }


    @Override
    public OpExecutor create(ExecutionContext execCxt) {
        return new OpExecutorCache(execCxt, serviceToQef);
    }

}
