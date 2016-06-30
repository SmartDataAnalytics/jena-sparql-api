package org;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.concept_cache.core.SparqlCacheUtils;
import org.aksw.jena_sparql_api.concept_cache.domain.ProjectedQuadFilterPattern;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPattern;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.concept_cache.op.OpUtils;
import org.aksw.jena_sparql_api.concept_cache.trash.OpVisitorViewCacheApplier;
import org.aksw.jena_sparql_api.utils.Generator;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.core.Var;

public class SparqlCacheSystem<D> {

    protected IndexSystem<Op, Op, D, String> indexSystem;

    protected Function<Op, QueryIndex> queryIndexer;
    
    public void registerCache(String name, Op cacheOp, D cacheData) {

        QueryIndex queryIndex = queryIndexer.apply(cacheOp);

        // Make the qfp canonical, extract their features, and index them


        // This is the op level indexing of cache
        indexSystem.put(cacheOp, cacheData);
    }


    public void rewriteQuery(Op queryOp) {
        Set<Entry<Op, T>> candidates = indexSystem.lookup(queryOp);



    }
}
