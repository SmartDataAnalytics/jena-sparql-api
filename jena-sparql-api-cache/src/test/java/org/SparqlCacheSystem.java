package org;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.apache.jena.sparql.algebra.Op;

public class SparqlCacheSystem<D> {

    protected IndexSystem<Op, Op, QueryIndex> indexSystem;
    protected Function<Op, QueryIndex> queryIndexer;    
    //protected Map<Op, D> opToCacheData;
    
    public void registerCache(String name, Op cacheOp) { //, D cacheData) {
        QueryIndex queryIndex = queryIndexer.apply(cacheOp);

        // This is the op level indexing of cache
        indexSystem.put(cacheOp, queryIndex);
        //opToCacheData.put(cacheOp, cacheData);
    }


    public void rewriteQuery(Op queryOp) {
        QueryIndex queryIndex = queryIndexer.apply(queryOp);
        
        Collection<Entry<Op, QueryIndex>> candidates = indexSystem.lookup(queryOp);

        


    }
}
