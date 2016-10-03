package org.aksw.jena_sparql_api.concept_cache.core;

import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.engine.main.OpExecutorFactory;

public class OpExecutorFactoryViewMatcher
    implements OpExecutorFactory
{
    private static OpExecutorFactoryViewMatcher instance = null;

    public static synchronized OpExecutorFactoryViewMatcher get() {
        if(instance == null) {
            instance = new OpExecutorFactoryViewMatcher();
        }

        return instance;
    }

    @Override
    public OpExecutor create(ExecutionContext execCxt) {
        return new OpExecutorViewCache(execCxt);
    }

}
