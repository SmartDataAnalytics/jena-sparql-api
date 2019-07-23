package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.jena_sparql_api.algebra.utils.AlgebraUtils;
import org.aksw.jena_sparql_api.algebra.utils.OpUtils;
import org.aksw.jena_sparql_api.algebra.utils.ProjectedQuadFilterPattern;
import org.aksw.jena_sparql_api.algebra.utils.QuadFilterPattern;
import org.aksw.jena_sparql_api.algebra.utils.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.concept_cache.dirty.SparqlViewMatcherQfpc;
import org.aksw.jena_sparql_api.concept_cache.trash.OpVisitorViewCacheApplier;
import org.aksw.jena_sparql_api.core.QueryExecutionExecWrapper;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpNull;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.core.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public class SparqlCacheUtils {

	private static final Logger logger = LoggerFactory.getLogger(SparqlCacheUtils.class);

	
    public static long preparationId = 0;

    /**
     * Prepares the execution of a query in regard to a query cache.
     *
     * Replaces parts of the algebra with cache hits, and
     * replaces other parts with ops that perform the caching.
     *
     * There are two types of query execution under caching:
     * (a) Rewrite the query by making only use of SPARQL 1. 1, most notably VALUES keyword, such that the remote sparql service can execute it
     * (b) Rewrite the query such that a local executor has to do the execution. This one can then request remote result sets.
     *
     * Essentially this means, that if the query made use of local cache operators, then the remaining quad patterns would also have
     * to be rewritten as to make a remote query.
     *
     *
     * @param qef
     * @param rawQuery
     * @param conceptMap
     * @param indexResultSetSizeThreshold
     * @return
     */
    public static QueryExecution prepareQueryExecution(
            QueryExecutionFactory qef,
            Map<Node, ? super ViewCacheIndexer> serviceMap,
            //Node serviceNode,
            Query rawQuery,
            SparqlViewMatcherQfpc conceptMap,
            //SparqlViewMatcherSystem viewMatcherSystem,
            long indexResultSetSizeThreshold)
    {
        Node serviceNode = NodeFactory.createURI("cache://" + qef.getId() + "-" + (preparationId++));

        logger.debug("Rewriting query: " + rawQuery);

        Query query = rewriteQuery(serviceNode, rawQuery, conceptMap, indexResultSetSizeThreshold);
        logger.debug("Rewritten query: " + query);


        ViewCacheIndexer vci = new ViewCacheIndexerImpl(qef, conceptMap, indexResultSetSizeThreshold);


        //serviceMap.put(serviceNode, new ViewCacheIndexerImpl(qef, conceptMap, indexResultSetSizeThreshold));


        // Temporarily register query execution factories for the parts that need to be cached
        //QueryExecutionViewCachePartial qefPartial = new QueryExecutionViewCachePartial(query, pqfp, qef, conceptMap, indexVars, indexResultSetSizeThreshold)




        // TODO Get this right:

        //boolean isPatternFree = true;
        boolean performLocalExecution = true;
        //boolean isCachingAllowed = true;
//        RewriteResult rewriteResult = OpVisitorViewCacheApplier.apply(rawQuery, conceptMap);
        //Query query = rewriteResult.getRewrittenQuery();
        //boolean isPatternFree = rewriteResult.isPatternFree();
        //boolean isCachingAllowed = rewriteResult.isCachingAllowed();

        logger.debug("Preparing query: " + query.toString().substring(0, Math.min(2000, query.toString().length())));

        //System.out.println("Running query: " + query);
//
//        ProjectedQuadFilterPattern pqfp = SparqlCacheUtils.transform(query);
//        QuadFilterPattern qfp = pqfp == null ? null : pqfp.getQuadFilterPattern();
//        boolean isIndexable = qfp != null;
//
//        List<Var> vars = query.getProjectVars();

        // If the query is pattern free, we can execute it against an empty model instead of performing a remote request
        QueryExecution result;
        if(performLocalExecution) {
            QueryExecutionFactory ss = new QueryExecutionFactoryModel();
            result = ss.createQueryExecution(query);
        }
        else {
            //QueryExecution qe = qef.createQueryExecution(query);

//            if(isIndexable && !vars.isEmpty() && isCachingAllowed) {
//                //Set<Var> indexVars = Collections.singleton(vars.iterator().next());
//
//                //result = new QueryExecutionViewCachePartial(query, qef, conceptMap, indexVars, indexResultSetSizeThreshold);
//            } else {
//                //result = qef.createQueryExecution(query);
//            }
            result = qef.createQueryExecution(query);

        }


        result = new QueryExecutionExecWrapper(result,
                    () -> { Assert.isTrue(!serviceMap.containsKey(serviceNode)); serviceMap.put(serviceNode, vci); },
                    () -> { Assert.isTrue(serviceMap.containsKey(serviceNode)); serviceMap.remove(serviceNode); }
                 );


        return result;
    }
    

    /**
     * Rewrites a query to make use of the cache
     *
     *
     * @param serviceNode
     * @param rawQuery
     * @param sparqlViewCache
     * @param indexResultSetSizeThreshold
     * @return
     */
    public static Query rewriteQuery(
            //QueryExecutionFactory qef,
            Node serviceNode,
            Query rawQuery,
            SparqlViewMatcherQfpc sparqlViewCache,
            long indexResultSetSizeThreshold)
    {
        Op rawOp = Algebra.compile(rawQuery);
        rawOp = Algebra.toQuadForm(rawOp);

        // TODO We could create a mapping from (op) -> (op with replaced constants)
        // rawOp = ReplaceConstants.replace(rawOp);
        Generator<Var> generator = OpUtils.freshVars(rawOp);

        // Determine which parts of the query are cacheable
        // (i.e. those parts that correspond to projected quad filter patterns)
        Map<Op, ProjectedQuadFilterPattern> tmpCacheableOps = OpVisitorViewCacheApplier.detectPrimitiveCachableOps(rawOp);

        // If the op is a projection, associate the pqfp with the sub op in order to retain the projection
        // TODO This is necessary, if we later expand the graph pattern; yet, I am not sure this is the best way to retain the projection
        Map<Op, ProjectedQuadFilterPattern> cacheableOps = tmpCacheableOps.entrySet().stream()
            .collect(Collectors.toMap(e -> {
                Op op = e.getKey();
                Op r = op instanceof OpProject ? ((OpProject)op).getSubOp() : op;
                return r;
            }, Entry::getValue));


        Map<QuadFilterPattern, QuadFilterPatternCanonical> qfpToCanonical = cacheableOps.entrySet().stream()
            .collect(Collectors.toMap(e -> e.getValue().getQuadFilterPattern(), e -> {
                ProjectedQuadFilterPattern pqfp = e.getValue();
                QuadFilterPattern qfp = pqfp.getQuadFilterPattern();
                QuadFilterPatternCanonical r = AlgebraUtils.canonicalize2(qfp, generator);
                return r;
            }));


        // Determine for which of the cachable parts we have cache hits
        Map<Op, CacheResult> opToCacheHit = cacheableOps.entrySet().stream()
            .map(e -> {
                ProjectedQuadFilterPattern pqfp = e.getValue();
                QuadFilterPattern qfp = pqfp.getQuadFilterPattern();
                //QuadFilterPattern SparqlCacheUtils.no


                //qfp = summarize(qfp).getCanonicalPattern();

                //qfp = canonicalize(qfp, generator);
                Op op = e.getKey();
                QuadFilterPatternCanonical qfpc = qfpToCanonical.get(qfp);

                CacheResult cacheResult = null; // TODO Maybe fix this line: sparqlViewCache.lookup(qfpc);
                Entry<Op, CacheResult> r = cacheResult == null ? null : new SimpleEntry<>(op, cacheResult);
                return r;
            })
            .filter(e -> e != null)
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));


        //System.out.println("Cache hits:");
        //opToCacheHit.entrySet().forEach(e -> System.out.println(e));
        logger.debug("Cache hits:");
        opToCacheHit.entrySet().forEach(e -> logger.debug("" + e));

        // Determine the cacheable parts which do not yet have cache hits
        Set<Op> nonCachedCacheableOps = Sets.difference(cacheableOps.keySet(), opToCacheHit.keySet());


        // TODO There may be ops for which there exist partial covers via cache hits.
        // These ops are again subject to caching.

        // Execute the cacheable parts, and cache them, if possible.
        // Note: We might find out that some result sets are too large to cache them.
        // This is the map which contains the rewrites:
        // . Ops that are in the cache are replaced by cache-access ops
        // . Ops that are not in the cache but cacheable are mapped by caching ops
        Map<Op, Op> opToCachingOp = new HashMap<>();

        for(Entry<Op, CacheResult> entry : opToCacheHit.entrySet()) {
            Op op = entry.getKey();

// TODO Inject projection
//            ProjectedQuadFilterPattern pqfp = cacheableOps.get(op);
//            List<Var> projectVars = new ArrayList<Var>(pqfp.getProjectVars());


            //cacheableOps.get(key)
            //= new OpProject(op, new ArrayList<Var>(pqfp.getProjectVars()));
            //entry.getValue().getTables().


            CacheResult cacheResult = entry.getValue();
            Op executionOp = cacheResult.getReplacementPattern().toOp();
            boolean isFullCover = executionOp instanceof OpNull;
            Collection<Table> tables = cacheResult.getTables();
            for(Table table : tables) {
                OpTable opTable = OpTable.create(table);
                // If the replacement pattern is empty, OpNull is returned which we need to eliminate
                executionOp = executionOp instanceof OpNull ? opTable : OpJoin.create(opTable, executionOp);
            }

            // TODO IMPORTANT Try to optimize filter placement
// TODO Inject projection
// executionOp = new OpProject(executionOp, projectVars);


            //executionOp = Optimize.apply(new TransformFilterPlacement(true), executionOp);


            // TODO The new op may be cachable again
            Op newOp = isFullCover
                    ? executionOp
                    : AlgebraUtils.wrapWithServiceOld(op, serviceNode, executionOp);

            opToCachingOp.put(op, newOp);
        }


        // Notes: indexOp is the op that encodes the canonical projected quad filter pattern used for indexing
        // executionOp is the op used to actually execute the pattern and may make use of caching parts
        for(Op op : nonCachedCacheableOps) {
            //op = Algebra.toQuadForm(op);

            ProjectedQuadFilterPattern pqfp = cacheableOps.get(op);
            QuadFilterPattern qfp = pqfp.getQuadFilterPattern();
            QuadFilterPatternCanonical indexQfpc = qfpToCanonical.get(qfp);

            // TODO Remove clause for default graph constraint

            //ProjectedQuadFilterPattern executionPqfp = SparqlCacheUtils.optimizeFilters(pqfp);
            //executionPqfp.to

            if(pqfp == null) { // TODO Turn into an assertion
                throw new RuntimeException("Should not happen");
            }

            //pqfp.getQuadFilterPattern();

            List<Var> projectVars = new ArrayList<Var>(pqfp.getProjectVars());
            Op indexOp = indexQfpc.toOp();
            indexOp = new OpProject(indexOp, projectVars);


            //Op executionOp = Optimize.apply(new TransformFilterPlacement(true), op);
            Op executionOp = op;
            // TODO: Maybe we should wrap the executionOp with the projection again

            Op newOp = AlgebraUtils.wrapWithServiceOld(indexOp, serviceNode, executionOp);

            opToCachingOp.put(op, newOp);
        }

        // Perform the substitution
        Op rootOp = OpUtils.substitute(rawOp, false, x -> opToCachingOp.get(x));

        Query tmp = OpAsQuery.asQuery(rootOp);
        rootOp = Algebra.compile(tmp);

        //rootOp = Transformer.transform(new TransformRemoveGraph(x -> false), rootOp);

        Query result = OpAsQuery.asQuery(rootOp);

        //System.out.println("Rewritten query: " + query);

        return result;
    }
}
