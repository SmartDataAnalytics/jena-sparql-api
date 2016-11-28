package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.collections.multimaps.BiHashMultimap;
import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.aksw.jena_sparql_api.algebra.transform.TransformReplaceConstants;
import org.aksw.jena_sparql_api.concept_cache.collection.FeatureMap;
import org.aksw.jena_sparql_api.concept_cache.collection.FeatureMapImpl;
import org.aksw.jena_sparql_api.concept_cache.dirty.SparqlViewCache;
import org.aksw.jena_sparql_api.concept_cache.domain.PatternSummary;
import org.aksw.jena_sparql_api.concept_cache.domain.ProjectedQuadFilterPattern;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPattern;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.concept_cache.domain.VarOccurrence;
import org.aksw.jena_sparql_api.concept_cache.op.OpExtQuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.concept_cache.op.OpUtils;
import org.aksw.jena_sparql_api.concept_cache.trash.OpVisitorViewCacheApplier;
import org.aksw.jena_sparql_api.core.QueryExecutionExecWrapper;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.aksw.jena_sparql_api.utils.ClauseUtils;
import org.aksw.jena_sparql_api.utils.CnfUtils;
import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.jena_sparql_api.utils.Generator;
import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;
import org.aksw.jena_sparql_api.utils.QuadUtils;
import org.aksw.jena_sparql_api.utils.VarGeneratorImpl2;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpNull;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.QuadPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_OneOf;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.ExprVars;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;

/*
class QueryRewrite {
    protected Query masterQuery;
    protected Map<Node, Query>
}
*/

public class SparqlCacheUtils {


    private static final Logger logger = LoggerFactory.getLogger(SparqlCacheUtils.class);


    /**
     * Wrap a node with a caching operation
     *
     * @param subOp
     * @param cacheRef The cache entry to create and where the result set will be stored
     *
     * @return
     */
    public static Op createCachingOp(Op subOp, Node storageRef) {
        boolean silent = false;

        //Query subQuery = OpAsQuery.asQuery(subOp);
        //Element subElement = new ElementSubQuery(subQuery);

        //ElementService elt = new ElementService(storageRef, subElement, silent);
        OpService result = new OpService(storageRef, subOp, silent);

        return result;
    }

    /**
     * Utility method to quickly create a canonical quad filter pattern from a query.
     *
     * @param query
     * @return
     */
    public static QuadFilterPatternCanonical fromQuery(Query query) {
        ProjectedOp op = SparqlQueryContainmentUtils.toProjectedOp(query);
        Op resOp = op.getResidualOp();
        QuadFilterPatternCanonical result = SparqlCacheUtils.extractQuadFilterPatternCanonical(resOp);
        return result;
    }

    public static QuadFilterPatternCanonical removeDefaultGraphFilter(QuadFilterPatternCanonical qfpc) {
        Set<Quad> quads = qfpc.getQuads();
        Set<Set<Expr>> cnf = qfpc.getFilterCnf();

        Map<Var, Node> varToNode = CnfUtils.getConstants(cnf);
        Map<Var, Node> candMap = varToNode.entrySet().stream().filter(
                e -> (Quad.defaultGraphIRI.equals(e.getValue())
                    || Quad.defaultGraphNodeGenerated.equals(e.getValue())))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        Set<Var> candVars = candMap.keySet();

        // Remove all vars that occurr in positions other than the graph
        for(Quad quad : quads) {
            Node[] nodes = QuadUtils.quadToArray(quad);
            for(int i = 1; i < 4; ++i) {
                Node node = nodes[i];
                candVars.remove(node);
            }
        }

        Set<Set<Expr>> newCnf = cnf.stream().filter(clause -> {
            Entry<Var, Node> e = CnfUtils.extractEquality(clause);
            boolean r = !candMap.entrySet().contains(e);
            return r;
        }).collect(Collectors.toSet());

        QuadFilterPatternCanonical result = new QuadFilterPatternCanonical(quads, newCnf);
        return result;
    }


    public static ProjectedQuadFilterPattern optimizeFilters(ProjectedQuadFilterPattern pqfp) {
        PatternSummary summary = summarize(pqfp.getQuadFilterPattern());
        QuadFilterPatternCanonical qfpc = summary.getCanonicalPattern();
        QuadFilterPatternCanonical optimized = optimizeFilters(qfpc.getQuads(), qfpc.getFilterCnf(), pqfp.getProjectVars());

        QuadFilterPattern qfp = optimized.toQfp();
        ProjectedQuadFilterPattern result = new ProjectedQuadFilterPattern(pqfp.getProjectVars(), qfp, false);

        return result;
    }


    public static QuadFilterPatternCanonical optimizeFilters(Collection<Quad> quads, Set<Set<Expr>> cnf, Set<Var> projection) {

        Map<Var, Node> varToNode = CnfUtils.getConstants(cnf);

        // A view on the set of variables subject the optimization
        Set<Var> optVars = varToNode.keySet();

        // Remove all equalities for projected variables
        optVars.remove(projection);

        Set<Quad> newQuads = new HashSet<Quad>();
        for(Quad quad : quads) {
            Node[] nodes = QuadUtils.quadToArray(quad);
            for(int i = 0; i < 4; ++i) {
                Node node = nodes[i];
                Node subst = varToNode.get(node);

                // Update in place, because the array is a copy anyway
                nodes[i] = subst == null ? node : subst;
            }

            Quad newQuad = QuadUtils.arrayToQuad(nodes);
            newQuads.add(newQuad);
        }

        // Remove the clauses from which the mapping was obtained
        Set<Set<Expr>> newCnf = new HashSet<>();
        for(Set<Expr> clause : cnf) {
            Entry<Var, Node> equality = CnfUtils.extractEquality(clause);
            boolean retainClause = equality == null || !optVars.contains(equality.getKey());

            if(retainClause) {
                newCnf.add(clause);
            }
        }

        QuadFilterPatternCanonical result = new QuadFilterPatternCanonical(newQuads, newCnf);
        return result;
    }



    // TODO Not used, can probably be removed
//    public static ResultSet executeCached(QueryExecutionFactory qef, Query query, ProjectedQuadFilterPattern pqfp, SparqlViewCache sparqlViewCache, long indexResultSetSizeThreshold) {
//        if(pqfp == null) {
//            throw new RuntimeException("Query is not indexable: " + query);
//        }
//
//        Set<Var> indexVars = new HashSet<>(query.getProjectVars());
//
//        QueryExecution qe = new QueryExecutionViewCacheFragment(query, pqfp, qef, sparqlViewCache, indexVars, indexResultSetSizeThreshold);
//        ResultSet result = qe.execSelect();
//        return result;
//    }


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
            SparqlViewCache conceptMap,
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
     * Create a service node with a union where the first member is to be interpreted as the
     * pattern that should be used for caching, and the second argument is the pattern to be
     * executed.
     *
     * @param patternOp
     * @param serviceNode
     * @param executionOp
     * @return
     */
    public static OpService wrapWithServiceOld(Op patternOp, Node serviceNode, Op executionOp) {
        boolean silent = false;
        OpUnion union = new OpUnion(patternOp, executionOp);

        Query subQuery = OpAsQuery.asQuery(union);
        Element subElement = new ElementSubQuery(subQuery);

        ElementService elt = new ElementService(serviceNode, subElement, silent);
        OpService result = new OpService(serviceNode, union, elt, silent);

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
            SparqlViewCache sparqlViewCache,
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
                QuadFilterPatternCanonical r = canonicalize2(qfp, generator);
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

                CacheResult cacheResult = sparqlViewCache.lookup(qfpc);
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
                    : wrapWithServiceOld(op, serviceNode, executionOp);

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

            Op newOp = wrapWithServiceOld(indexOp, serviceNode, executionOp);

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



    /**
     * Rename all variables to ?g ?s ?p ?o based on the given quad and the cnf
     * This is used for looking up triples having a certain expression over its components
     *
     * ([?g ?s ?s ?o], (fn(?s, ?o))
     *
     * @param quad
     * @param expr
     */
    public static Set<Set<Expr>> normalize(Quad quad, Set<Set<Expr>> cnf) {
        List<Var> componentVars = Vars.gspo;

        Map<Var, Var> renameMap = new HashMap<Var, Var>();
        Set<Set<Expr>> extra = new HashSet<Set<Expr>>();
        for(int i = 0; i < 4; ++i) {
            Node tmp = QuadUtils.getNode(quad, i);
            if(!tmp.isVariable()) {
                throw new RuntimeException("Expected variable normalized quad");
            }

            Var quadVar = (Var)tmp;
            Var componentVar = componentVars.get(i);

            Var priorComponentVar = renameMap.get(quadVar);
            // We need to rename
            if(priorComponentVar != null) {
                extra.add(Collections.<Expr>singleton(new E_Equals(new ExprVar(priorComponentVar), new ExprVar(componentVar))));
            } else {
                renameMap.put(quadVar, componentVar);
            }
        }

        NodeTransformRenameMap transform = new NodeTransformRenameMap(renameMap);
        Set<Set<Expr>> result = ClauseUtils.applyNodeTransformSet(cnf, transform);
        result.addAll(extra);

        //System.out.println(result);
        return result;
    }

    public static Set<Set<Expr>> add(Quad quad, Set<Set<Expr>> cnf) {
        Set<Set<Expr>> result = new HashSet<Set<Expr>>();

        for(Set<Expr> clause : cnf) {
            Set<Var> clauseVars = ClauseUtils.getVarsMentioned(clause);
            Set<Var> exprVars = QuadUtils.getVarsMentioned(quad);

            boolean isApplicable = exprVars.containsAll(clauseVars);
            if(isApplicable) {
                result.add(clause);
            }
        }

        return result;
    }


    /**
     * Cut away the projection (TODO: and maybe extend) of an op (if any), and return
     * the projection as a standalone object together with the remaining op.
     *
     * @param residualOp
     * @return
     */
    public static ProjectedOp cutProjection(Op op) {
        Op residualOp = op;

        Set<Var> projectVars = null;

        boolean isDistinct = false;
        if(residualOp instanceof OpDistinct) {
            isDistinct = true;
            residualOp = ((OpDistinct)residualOp).getSubOp();
        }

        if(residualOp instanceof OpProject) {
            OpProject tmp = (OpProject)residualOp;
            projectVars = new HashSet<>(tmp.getVars());

            residualOp = tmp.getSubOp();
        }

        ProjectedOp result = projectVars == null
                ? new ProjectedOp(OpUtils.visibleNamedVars(residualOp), false, residualOp)
                : new ProjectedOp(projectVars, isDistinct, residualOp);

        return result;
    }

    public static ProjectedQuadFilterPattern transform(Query query) {
        Op op = Algebra.compile(query);
        op = Algebra.toQuadForm(op);
        op = TransformReplaceConstants.transform(op);
        ProjectedQuadFilterPattern result = transform(op);
        return result;
    }

    public static QuadFilterPatternCanonical transform2(Query query) {
        Op op = Algebra.compile(query);
        op = Algebra.toQuadForm(op);
        op = TransformReplaceConstants.transform(op);
        ProjectedQuadFilterPattern pqfp = transform(op);
        QuadFilterPatternCanonical result = pqfp == null ? null : canonicalize2(pqfp.getQuadFilterPattern(), VarGeneratorImpl2.create("v"));

        return result;
    }


    public static ProjectedQuadFilterPattern transform(Element element) {

        Op op = Algebra.compile(element);
        op = Algebra.toQuadForm(op);
        ProjectedQuadFilterPattern result = transform(op);
        return result;
    }

    public static QuadFilterPatternCanonical extractQuadFilterPatternCanonical(Op op) {
        QuadFilterPattern qfp = SparqlCacheUtils.extractQuadFilterPattern(op);
        QuadFilterPatternCanonical result;
        if(qfp != null) {
            Generator<Var> generator = VarGeneratorImpl2.create();
            result = SparqlCacheUtils.canonicalize2(qfp, generator);
        } else {
            result = null;
        }
        return result;
    }

    public static QuadFilterPattern extractQuadFilterPattern(Op op) {
        QuadFilterPattern result = null;

//        //
//        if(op instanceof OpQuadFilterPatternCanonical) {
//        	result = ((OpQuadFilterPatternCanonical)op).getQfpc().toQfp();
//        }

        OpFilter opFilter;
        // TODO allow nested filters
        if(op instanceof OpFilter) {
            opFilter = (OpFilter)op;
        } else {
            opFilter = (OpFilter)OpFilter.filter(NodeValue.TRUE, op);
        }

        op = opFilter.getSubOp();

        if(op instanceof OpGraph) {
            OpGraph opGraph = (OpGraph)op;
            Node graphNode = opGraph.getNode();

            // The graphNode must be a variable which is not constrained by the filter

            Set<Var> filterVars = ExprVars.getVarsMentioned(opFilter.getExprs());
            if(graphNode.isVariable() && !filterVars.contains(graphNode)) {
                op = opGraph.getSubOp();
            } else {
                op = null;
            }
        }

        if(op instanceof OpQuadPattern) {
            OpQuadPattern opQuadPattern = (OpQuadPattern)opFilter.getSubOp();

            QuadPattern quadPattern = opQuadPattern.getPattern();
            List<Quad> quads = quadPattern.getList();

            ExprList exprs = opFilter.getExprs();
            Expr expr = ExprUtils.andifyBalanced(exprs);

            result = new QuadFilterPattern(quads, expr);
        }

        return result;
    }

    /**
     * Note assumes that this has been applied so far:
     *  op = Algebra.toQuadForm(op);
     * op = ReplaceConstants.replace(op);
     *
     * @param op
     * @return
     */
    public static ProjectedQuadFilterPattern transform(Op op) {

        ProjectedQuadFilterPattern result = null;

        Set<Var> projectVars = null;

        boolean isDistinct = false;
        if(op instanceof OpDistinct) {
            isDistinct = true;
            op = ((OpDistinct)op).getSubOp();
        }

        if(op instanceof OpProject) {
            OpProject tmp = (OpProject)op;
            projectVars = new HashSet<>(tmp.getVars());

            op = tmp.getSubOp();
        }

        QuadFilterPattern qfp = extractQuadFilterPattern(op);

        if(qfp != null) {
            if(projectVars == null) {
                projectVars = new HashSet<>(OpVars.mentionedVars(op));
            }

            result = new ProjectedQuadFilterPattern(projectVars, qfp, isDistinct);
        }


        return result;
    }

    public static Map<Quad, Set<Set<Expr>>> quadToCnf(QuadFilterPattern qfp) {
        Map<Quad, Set<Set<Expr>>> result = new HashMap<Quad, Set<Set<Expr>>>();

        Expr expr = qfp.getExpr();
        if(expr == null) {
            expr = NodeValue.TRUE;
        }

        Set<Set<Expr>> filterCnf = CnfUtils.toSetCnf(expr);

        Set<Quad> quads = new HashSet<Quad>(qfp.getQuads());

        for(Quad quad : quads) {

            Set<Var> quadVars = QuadUtils.getVarsMentioned(quad);

            Set<Set<Expr>> cnf = new HashSet<Set<Expr>>();

            for(Set<Expr> clause : filterCnf) {
                Set<Var> clauseVars = ClauseUtils.getVarsMentioned(clause);

                boolean containsAll = quadVars.containsAll(clauseVars);
                if(containsAll) {
                    cnf.add(clause);
                }
            }


            //Set<Set<Expr>> quadCnf = normalize(quad, cnf);
            //quadCnfList.add(quadCnf);
            result.put(quad, cnf);
        }

        return result;
    }



//    public static QuadFilterPattern canonicalize(QuadFilterPattern qfp, Generator<Var> generator) {
//        QuadFilterPatternCanonical tmp = replaceConstants(qfp.getQuads(), generator);
//        Set<Set<Expr>> cnf = CnfUtils.toSetCnf(qfp.getExpr());
//        cnf.addAll(tmp.getFilterCnf());
//        QuadFilterPatternCanonical canonical = new QuadFilterPatternCanonical(tmp.getQuads(), cnf);
//
//
//        //QuadFilterPatternCanonical qfpc = summarize(qfp).getCanonicalPattern();
//
////        QuadFilterPatternCanonical tmp = canonicalize(qfpc, generator);
//        QuadFilterPattern result = canonical.toQfp();
//
//        return result;
//    }

    public static OpExtQuadFilterPatternCanonical tryCreateCqfp(Op op, Generator<Var> generator) {
        QuadFilterPattern qfp = extractQuadFilterPattern(op);
        OpExtQuadFilterPatternCanonical result;
        if(qfp == null) {
            result = null;
        } else {
            QuadFilterPatternCanonical tmp = canonicalize2(qfp, generator);
            result = new OpExtQuadFilterPatternCanonical(tmp);
        }
        return result;
    }

    public static QuadFilterPatternCanonical canonicalize2(QuadFilterPattern qfp, Generator<Var> generator) {
        QuadFilterPatternCanonical tmp = replaceConstants(qfp.getQuads(), generator);
        tmp = removeDefaultGraphFilter(tmp);
        Set<Set<Expr>> cnf = CnfUtils.toSetCnf(qfp.getExpr());
        cnf.addAll(tmp.getFilterCnf());
        QuadFilterPatternCanonical result = new QuadFilterPatternCanonical(tmp.getQuads(), cnf);

        return result;
    }

    public static QuadFilterPatternCanonical canonicalize(QuadFilterPatternCanonical qfpc, Generator<Var> generator) {
        QuadFilterPatternCanonical tmp = replaceConstants(qfpc.getQuads(), generator);

        Set<Set<Expr>> newCnf = new HashSet<>();
        newCnf.addAll(qfpc.getFilterCnf());
        newCnf.addAll(tmp.getFilterCnf());

        QuadFilterPatternCanonical result = new QuadFilterPatternCanonical(tmp.getQuads(), newCnf);
        return result;
    }

    public static QuadFilterPatternCanonical replaceConstants(Iterable<Quad> quads, Generator<Var> generator) {
        Set<Set<Expr>> cnf = new HashSet<>();

        Map<Node, Var> constantToVar = new HashMap<>();

        Set<Quad> newQuads = new LinkedHashSet<>();
        for(Quad quad : quads) {
            Node[] nodes = QuadUtils.quadToArray(quad);
            for(int i = 0; i < 4; ++i) {
                Node node = nodes[i];

                if(!node.isVariable()) {
                    Var v = constantToVar.get(node);
                    if(v == null) {
                        v = generator.next();
                        constantToVar.put(node, v);

                        Expr expr = new E_Equals(new ExprVar(v), NodeValue.makeNode(node));
                        cnf.add(Collections.singleton(expr));
                    }
                    nodes[i] = v;
                }
                // If it is a variable, just retain it
            }

            Quad newQuad = QuadUtils.arrayToQuad(nodes);
            newQuads.add(newQuad);
        }

        QuadFilterPatternCanonical result = new QuadFilterPatternCanonical(newQuads, cnf);
        return result;
    }


    public static PatternSummary summarize(QuadFilterPattern originalPattern) {

        Expr expr = originalPattern.getExpr();
        Set<Quad> quads = new LinkedHashSet<Quad>(originalPattern.getQuads());


        Set<Set<Expr>> filterCnf = CnfUtils.toSetCnf(expr);

        IBiSetMultimap<Quad, Set<Set<Expr>>> quadToCnf = createMapQuadsToFilters(quads, filterCnf);
        IBiSetMultimap<Var, VarOccurrence> varOccurrences = createMapVarOccurrences(quadToCnf, false);

        //System.out.println("varOccurrences: " + varOccurrences);
        //Set<Set<Set<Expr>>> quadCnfs = new HashSet<Set<Set<Expr>>>(quadCnfList);

        QuadFilterPatternCanonical canonicalPattern = new QuadFilterPatternCanonical(quads, filterCnf);
        //canonicalPattern = canonicalize(canonicalPattern, generator);


        PatternSummary result = new PatternSummary(originalPattern, canonicalPattern, quadToCnf, varOccurrences);


        //for(Entry<Var, Collection<VarOccurrence>> entry : varOccurrences.asMap().entrySet()) {
            //System.out.println("Summary: " + entry.getKey() + ": " + entry.getValue().size());
            //System.out.println(entry);
        //}

        return result;
    }


    private static IBiSetMultimap<Var, VarOccurrence> createMapVarOccurrences(
            IBiSetMultimap<Quad, Set<Set<Expr>>> quadToCnf, boolean pruneVarOccs) {
        Set<Quad> quads = quadToCnf.keySet();

        // Iterate the quads again, and for each variable map it to where it to the component where it occurs in
        IBiSetMultimap<Var, VarOccurrence> varOccurrences = new BiHashMultimap<Var, VarOccurrence>();
        //for(int i = 0; i < quads.size(); ++i) {
            //Quad quad = quads.get(i);
        for(Quad quad : quads) {
            Set<Set<Expr>> quadCnf = quadToCnf.get(quad).iterator().next(); //quadCnfList.get(i);

            for(int j = 0; j < 4; ++j) {
                Var var = (Var)QuadUtils.getNode(quad, j);

                VarOccurrence varOccurence = new VarOccurrence(quadCnf, j);

                varOccurrences.put(var, varOccurence);
            }
        }

        // Remove all variables that only occur in the same quad
        //boolean pruneVarOccs = false;
        if(pruneVarOccs) {
            Iterator<Entry<Var, Collection<VarOccurrence>>> it = varOccurrences.asMap().entrySet().iterator();

            while(it.hasNext()) {
                Entry<Var, Collection<VarOccurrence>> entry = it.next();

                Set<Set<Set<Expr>>> varQuadCnfs = new HashSet<Set<Set<Expr>>>();
                for(VarOccurrence varOccurrence : entry.getValue()) {
                    varQuadCnfs.add(varOccurrence.getQuadCnf());

                    // Bail out early
                    if(varQuadCnfs.size() > 1) {
                        break;
                    }
                }

                if(varQuadCnfs.size() == 1) {
                    it.remove();
                }
            }
        }

        return varOccurrences;
    }


    /**
     * Note: the result map contains all quads - quads without constraints map to an empty set
     *
     *
     *
     * @param quads
     * @param filterCnf
     * @return
     */
    public static IBiSetMultimap<Quad, Set<Set<Expr>>> createMapQuadsToFilters(QuadFilterPatternCanonical qfpc) {
        Set<Quad> quads = qfpc.getQuads();
        Set<Set<Expr>> filterCnf = qfpc.getFilterDnf();
        if(filterCnf == null) {
            filterCnf = Collections.singleton(Collections.emptySet());
        }

        IBiSetMultimap<Quad, Set<Set<Expr>>> result = createMapQuadsToFilters(quads, filterCnf);
        return result;
    }

    public static IBiSetMultimap<Quad, Set<Set<Expr>>> createMapQuadsToFilters(
            Set<Quad> quads, Set<Set<Expr>> filterCnf) {
        // This is part of the result
        //List<Set<Set<Expr>>> quadCnfList = new ArrayList<Set<Set<Expr>>>(quads.size());
        IBiSetMultimap<Quad, Set<Set<Expr>>> quadToCnf = new BiHashMultimap<Quad, Set<Set<Expr>>>();



        for(Quad quad : quads) {
            Set<Var> quadVars = QuadUtils.getVarsMentioned(quad);

            Set<Set<Expr>> cnf = new HashSet<Set<Expr>>(); //new HashSet<Clause>();

            for(Set<Expr> clause : filterCnf) {
                Set<Var> clauseVars = ClauseUtils.getVarsMentioned(clause);

                boolean containsAll = quadVars.containsAll(clauseVars);
                if(containsAll) {
                    cnf.add(clause);
                }
            }


            Set<Set<Expr>> quadCnf = normalize(quad, cnf);
            //quadCnfList.add(quadCnf);
            quadToCnf.put(quad, quadCnf);
        }
        return quadToCnf;
    }


    public static Expr createExpr(ResultSet rs, Map<Var, Var> varMap) {

        //ResultSet copy = ResultSetFactory.copyResults(rs);

        Expr result;

        if(rs.getResultVars().size() == 1) {
            String varName = rs.getResultVars().iterator().next();
            Var var = Var.alloc(varName);

            Set<Node> nodes = getResultSetCol(rs, var);
            ExprList exprs = nodesToExprs(nodes);

            Var inVar = varMap.get(var);
            ExprVar ev = new ExprVar(inVar);

            result = new E_OneOf(ev, exprs);
        } else {
            throw new RuntimeException("Not supported yet");
        }

        return result;
    }

    public static Set<Node> getResultSetCol(ResultSet rs, Var v) {
        Set<Node> result = new HashSet<Node>();
        while(rs.hasNext()) {
            Binding binding = rs.nextBinding();
            Node node = binding.get(v);

            if(node != null) {
                result.add(node);
            }
        }

        return result;
    }

    public static ExprList nodesToExprs(Iterable<Node> nodes) {
        ExprList result = new ExprList();
        for(Node node : nodes) {
            Expr expr = NodeValue.makeNode(node);
            result.add(expr);
        }

        return result;
    }


    /**
     * TODO this has complexity O(n^2)
     * We can surely do better than that because joins are sparse and we
     * don't have to consider quads that do not join...
     *
     *
     * @param sub
     * @return
     */
    public static SetMultimap<Quad, Quad> quadJoinSummary(List<Quad> sub) {

        Node[] tmp = new Node[4];

        SetMultimap<Quad, Quad> result = HashMultimap.create();
        for(int i = 0; i < sub.size(); ++i) {
            Quad a = sub.get(i);
            for(int j = i + 1; j < sub.size(); ++j) {
                Quad b = sub.get(j);

                for(int k = 0; k < 4; ++k) {
                    Node na = QuadUtils.getNode(a, k);
                    Node nb = QuadUtils.getNode(b, k);
                    boolean isEqual = na.equals(nb);
                    Node c = isEqual ? NodeValue.TRUE.asNode() : NodeValue.FALSE.asNode();

                    tmp[k] = c;
                }
                Quad summary = QuadUtils.create(tmp);

                result.put(summary, a);
                result.put(summary, b);
            }
        }

        return result;
    }

    public static void backtrackMeh(PatternSummary query, PatternSummary cand, Map<Var, Set<Var>> candToQuery, List<Var> varOrder, int index) {
        Var var = varOrder.get(index);

        Set<Var> queryVars = candToQuery.get(var);

        // Try a mapping, and backtrack if we hit a dead end
        for(Var queryVar : queryVars) {

            //

        }

    }


    // Return the variables that we cannot optimize away, as they
    // are referenced in the following portions
    // - projection
    // - order
    // - group by
    public static Set<Var> getRefVars(Query query) {
        //query.getProjectVars();
        return null;
    }


    public static FeatureMap<Expr, Multimap<Expr, Expr>> indexDnf(Set<Set<Expr>> dnf) {
        if(dnf == null) {
            // A disjunction containing an empty conjunction (latter is generally treated as true - if i'm not mistaken)
            dnf = Collections.singleton(Collections.emptySet());
            //dnf = Collections.emptySet();
        }

        FeatureMap<Expr, Multimap<Expr, Expr>> result = new FeatureMapImpl<>();
        for(Set<Expr> clause : dnf) {
            Multimap<Expr, Expr> exprSigToExpr = HashMultimap.create();
            Set<Expr> clauseSig = new HashSet<>();
            for(Expr expr : clause) {
                Expr exprSig = org.aksw.jena_sparql_api.utils.ExprUtils.signaturize(expr);
                exprSigToExpr.put(exprSig, expr);
                clauseSig.add(exprSig);
            }

            //Set<Expr> clauseSig = ClauseUtils.signaturize(clause);
            result.put(clauseSig, exprSigToExpr);
        }

        return result;
    }




    /**
     * So we need to know which variables of a quad pattern are either projected or required for evaluation (e.g. filters, joins) of the overall query.
     *
     *
     *
     *
     * For each quad filter pattern of the given algebra expression determine which variables are projected and
     * whether distinct applies.
     * So actually we want to push distinct down - but this also does not make sense, because distinct only applies to
     * after a projection...
     *
     *
     * The mean thing is, that variables are actually scoped:
     * Select ?x {
     *   { Select Distinct ?s As ?x{ // Within this subtree, unique(?x) applies
     *     ?s a foaf:Person
     *   } }
     *   Union {
     *     ?x a foaf:Agent
     *   }
     * }
     *
     *
     *
     *
     * @param opIndex
     */
//    public static VarUsage analyzeQuadFilterPatterns(OpIndex opIndex) {
//    	Tree<Op> tree = opIndex.getTree();
//    	List<Op> leafs = TreeUtils.getLeafs(tree);
//    	for(Op leaf : leafs) {
//    		VarUsage ps = OpUtils.analyzeVarUsage(tree, leaf);
//    		System.out.println(ps);
//    	}
//    	return null;
//    }
}


//// Variables that are projected in the current iteration
////Set<Var> projectedVars = new HashSet<>(availableVars);
//
//// Variables that are referenced
//Set<Var> referencedVars = new HashSet<>();
//
//// Any variable that is aggregated on must not be non-unique (otherwise it would distort the result)
//// Note that an overall query neither projects nor references a nonUnique variable
//Set<Var> nonUnique = new HashSet<>();
//
//// Maps variables to which other vars they depend on
//// E.g. Select (?x + 1 As ?y) { ... } will create the entry ?y -> { ?x } - i.e. ?y depends on ?x
//// Transitive dependencies are resolved immediately
//Multimap<Var, Var> varDeps = HashMultimap.create();
//availableVars.forEach(v -> varDeps.put(v, v));
//
//Op placeholder = new OpBGP();
//Op parent;
//while((parent = tree.getParent(current)) != null) {
//	// Class<?> opClass = current.getClass();
//	// System.out.println("Processing: " + parent);
//	// Compute referenced vars for joins (non-disjunctive multi argument expressions)
//	// boolean isDisjunction = parent instanceof OpUnion || parent instanceof OpDisjunction;
//	// boolean isJoin = parent instanceof OpJoin || parent instanceof OpSequence;
//	if(parent instanceof OpJoin || parent instanceof OpLeftJoin || parent instanceof OpSequence) {
//		List<Op> children = tree.getChildren(parent);
//		List<Op> tmp = new ArrayList<>(children);
//		Set<Var> visibleVars = new HashSet<>();
//		for(int i = 0; i < tmp.size(); ++i) {
//			Op child = tmp.get(i);
//			if(child != current) {
//				OpVars.visibleVars(child, visibleVars);
//			}
//		}
//
//		if(parent instanceof OpLeftJoin) {
//			OpLeftJoin olj = (OpLeftJoin)parent;
//			ExprList exprs = olj.getExprs();
//			if(exprs != null) {
//				Set<Var> vms = ExprVars.getVarsMentioned(exprs);
//				visibleVars.addAll(vms);
//			}
//		}
//
//		Set<Var> originalVars = getAll(varDeps, visibleVars);
//		//Set<Var> overlapVars = Sets.intersection(projectedVars, originalVars);
//		referencedVars.addAll(originalVars);
//
//	} else if(parent instanceof OpProject) {
//		OpProject o = (OpProject)parent;
//		Set<Var> vars = new HashSet<>(o.getVars());
//		Set<Var> removals = new HashSet<>(Sets.difference(varDeps.keySet(), vars));
//		varDeps.removeAll(removals);
//	} else if(parent instanceof OpExtend) { // TODO same for OpAssign
//		OpExtend o = (OpExtend)parent;
//		VarExprList vel = o.getVarExprList();
//
//		Multimap<Var, Var> updates = HashMultimap.create();
//		vel.forEach((v, ex) -> {
//			Set<Var> vars = ExprVars.getVarsMentioned(ex);
//			vars.forEach(w -> {
//				Collection<Var> deps = varDeps.get(w);
//				updates.putAll(w, deps);
//			});
//
//			updates.asMap().forEach((k, w) -> {
//				varDeps.replaceValues(k, w);
//			});
//		});
//
////	} else if(parent instanceof OpAssign) {
////		OpAssign o = (OpAssign)parent;
////		projectedVars.remove(o.getVarExprList().getVars());
//	} else if(parent instanceof OpGroup) {
//		// TODO: This is similar to a projection
//
//		OpGroup o = (OpGroup)parent;
//		// Original variables used in the aggregators are declared as non-unique and referenced
//		List<ExprAggregator> exprAggs = o.getAggregators();
//		exprAggs.forEach(ea -> {
//			Var v = ea.getVar();
//			ExprList el = ea.getAggregator().getExprList();
//			Set<Var> vars = ExprVars.getVarsMentioned(el);
//			Set<Var> origVars = getAll(varDeps, vars);
//			//referencedVars.addAll(origVars);
//			varDeps.putAll(v, origVars);
//			nonUnique.addAll(origVars);
//		});
//
//		// Original variables in the group by expressions are declared as referenced
//		VarExprList vel = o.getGroupVars();
//		vel.forEach((v, ex) -> {
//			Set<Var> vars = ExprVars.getVarsMentioned(ex);
//			Set<Var> origVars = MultiMaps.transitiveGetAll(varDeps.asMap(), vars);
//			referencedVars.addAll(origVars);
//			varDeps.putAll(v, origVars);
//		});
//
//	} else {
////		referencedVars.addAll(availableVars);
//////		isDistinct = false;
////
////
////		System.out.println("Unknown Op type: " + opClass);
////		projectedVars.clear();
//	}
//
//	current = parent;
//}
//
////referencedVars.addAll(varDeps.values());
