package org;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.aksw.jena_sparql_api.concept_cache.collection.FeatureMap;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.expr.Expr;

public class SparqlCacheSystem {

    protected IndexSystem<Entry<Op, QueryIndex>, Op> indexSystem;
    protected Function<Op, QueryIndex> queryIndexer;    
    //protected Map<Op, D> opToCacheData;
    
    public SparqlCacheSystem() {
        indexSystem = IndexSystemImpl.create();
        queryIndexer = new QueryIndexerImpl();
    }
    
    public void registerCache(String name, Op cacheOp) { //, D cacheData) {
        QueryIndex queryIndex = queryIndexer.apply(cacheOp);

        // This is the op level indexing of cache
        indexSystem.add(new SimpleEntry<>(cacheOp, queryIndex));
        //opToCacheData.put(cacheOp, cacheData);
    }


    public void rewriteQuery(Op queryOp) {
        QueryIndex queryIndex = queryIndexer.apply(queryOp);
        
        Collection<Entry<Op, QueryIndex>> candidates = indexSystem.lookup(queryOp);

        for(Entry<Op, QueryIndex> e : candidates) {
            QueryIndex cacheIndex = e.getValue();
            FeatureMap<Expr, QuadPatternIndex> cacheQpi = cacheIndex.getQuadPatternIndex();
            
            for(Entry<Set<Expr>, Collection<QuadPatternIndex>> f : queryIndex.getQuadPatternIndex().entrySet()) {
                Set<Expr> queryFeatureSet = f.getKey();
                Collection<QuadPatternIndex> queryQps = f.getValue();

                Collection<Entry<Set<Expr>, QuadPatternIndex>> cacheQpiCandidates = cacheQpi.getIfSupersetOf(queryFeatureSet);

                for(QuadPatternIndex queryQp : queryQps) {                
                    for(Entry<Set<Expr>, QuadPatternIndex> g : cacheQpiCandidates) {
                        QuadPatternIndex cacheQp = g.getValue();
                        
                        System.out.println("QueryQP: " + queryQp);
                        System.out.println("CacheQP: " + cacheQp);
                        System.out.println("-----");
                    
                    }
                }
                
                
                
            }
            
            
        }
        


    }
}
