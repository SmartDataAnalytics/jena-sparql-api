package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.engine.main.OpExecutorFactory;

public class OpExecutorFactoryViewMatcher
    implements OpExecutorFactory
{
    //protected Map<Node, ViewCacheIndexer> serviceToQef;
	protected Map<Node, StorageEntry> storageMap;

    private static OpExecutorFactoryViewMatcher instance = null;

    public static synchronized OpExecutorFactoryViewMatcher get() {
        if(instance == null) {
            instance = new OpExecutorFactoryViewMatcher();
        }

        return instance;
    }


    public OpExecutorFactoryViewMatcher(
            Map<Node, StorageEntry> storageMap) {
        super();
        this.storageMap = storageMap;
    }

    public OpExecutorFactoryViewMatcher() {
        super();
        this.storageMap = new HashMap<>();
    }

    public Map<Node, StorageEntry> getStorageMap() {
    	return storageMap;
    }

    @Deprecated
    public Map<Node, ViewCacheIndexer> getServiceMap() {
        return null;
    }


    @Override
    public OpExecutor create(ExecutionContext execCxt) {
        return new OpExecutorViewCache(execCxt, storageMap);
    }

}
