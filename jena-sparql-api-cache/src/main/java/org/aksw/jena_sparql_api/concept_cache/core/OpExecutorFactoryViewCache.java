package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.engine.main.OpExecutorFactory;
import org.apache.jena.sparql.engine.main.QC;

public class OpExecutorFactoryViewCache
    implements OpExecutorFactory
{
    protected Map<Node, ViewCacheIndexer> serviceToQef;

    private static OpExecutorFactoryViewCache instance = null;

    public static synchronized OpExecutorFactoryViewCache get() {
        if(instance == null) {
            instance = new OpExecutorFactoryViewCache();
        }

        return instance;
    }


    public OpExecutorFactoryViewCache(
            Map<Node, ViewCacheIndexer> serviceToQef) {
        super();
        this.serviceToQef = serviceToQef;
    }

    public OpExecutorFactoryViewCache() {
        super();
        this.serviceToQef = new HashMap<>();
    }

    public Map<Node, ViewCacheIndexer> getServiceMap() {
        return serviceToQef;
    }


    @Override
    public OpExecutor create(ExecutionContext execCxt) {
        return new OpExecutorViewCache(execCxt, serviceToQef);
    }

}
