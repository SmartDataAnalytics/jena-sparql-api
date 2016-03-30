package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.collections.MapUtils;
import org.aksw.commons.collections.multimaps.BiHashMultimap;
import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.aksw.jena_sparql_api.concept_cache.dirty.CacheResult;
import org.aksw.jena_sparql_api.concept_cache.dirty.SparqlViewCache;
import org.aksw.jena_sparql_api.concept_cache.domain.PatternSummary;
import org.aksw.jena_sparql_api.concept_cache.domain.ProjectedQuadFilterPattern;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPattern;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.concept_cache.domain.VarOccurrence;
import org.aksw.jena_sparql_api.concept_cache.op.OpUtils;
import org.aksw.jena_sparql_api.concept_cache.trash.OpVisitorViewCacheApplier;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.aksw.jena_sparql_api.utils.ClauseUtils;
import org.aksw.jena_sparql_api.utils.CnfUtils;
import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;
import org.aksw.jena_sparql_api.utils.QuadUtils;
import org.aksw.jena_sparql_api.utils.ReplaceConstants;
import org.aksw.jena_sparql_api.utils.VarUtils;
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
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpNull;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.OpTable;
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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

/*
class QueryRewrite {
    protected Query masterQuery;
    protected Map<Node, Query>
}
*/

public class SparqlCacheUtils {

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
            Map<Node, QueryExecutionFactory> serviceMap,
            //Node serviceNode,
            Query rawQuery,
            SparqlViewCache conceptMap,
            long indexResultSetSizeThreshold)
    {
        Node serviceNode = NodeFactory.createURI("cache://" + qef.getId());

        Query query = rewriteQuery(serviceNode, rawQuery, conceptMap, indexResultSetSizeThreshold);


        serviceMap.put(serviceNode, new QueryExecutionFactoryViewCacheFragment(qef, conceptMap, indexResultSetSizeThreshold));
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

        System.out.println("Preparing query: " + query.toString().substring(0, Math.min(2000, query.toString().length())));

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


        return result;
    }

    public static Query rewriteQuery(
            //QueryExecutionFactory qef,
            Node serviceNode,
            Query rawQuery,
            SparqlViewCache conceptMap,
            long indexResultSetSizeThreshold)
    {
        Op rawOp = Algebra.compile(rawQuery);
        rawOp = Algebra.toQuadForm(rawOp);
        rawOp = ReplaceConstants.replace(rawOp);

        // Determine which parts of the query are cacheable
        Map<Op, ProjectedQuadFilterPattern> cacheableOps = OpVisitorViewCacheApplier.detectPrimitiveCachableOps(rawOp);

        // Determine for which of the cachable parts we have cache hits
        Map<Op, CacheResult> opToCacheHit = cacheableOps.entrySet().stream()
            .map(e -> {
                ProjectedQuadFilterPattern pqfp = e.getValue();
                QuadFilterPattern qfp = pqfp.getQuadFilterPattern();
                CacheResult cacheResult = conceptMap.lookup(qfp);
                Entry<Op, CacheResult> r = cacheResult == null ? null : new SimpleEntry<>(e.getKey(), cacheResult);
                return r;
            })
            .filter(e -> e != null)
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        // Determine the cacheable parts which do not yet have cache hits
        Set<Op> nonCachedCacheableOps = Sets.difference(cacheableOps.keySet(), opToCacheHit.keySet());

        //ElementService
        //OpService

        // Execute the cacheable parts, and cache them, if possible.
        // Note: We might find out that some result sets are too large to cache them.
        // This is the map which contains the rewrites:
        // . Ops that are in the cache are replaced by cache-access ops
        // . Ops that are not in the cache but cacheable are mapped by caching ops
        Map<Op, Op> opToCachingOp = new HashMap<>();

        for(Entry<Op, CacheResult> entry : opToCacheHit.entrySet()) {
            Op op = entry.getKey();
            CacheResult cacheResult = entry.getValue();
            Op newOp = cacheResult.getReplacementPattern().toOp();
            Collection<Table> tables = cacheResult.getTables();
            for(Table table : tables) {
                OpTable opTable = OpTable.create(table);
                // If the replacement pattern is empty, OpNull is returned which we need to eliminate
                newOp = newOp instanceof OpNull ? opTable : OpJoin.create(opTable, newOp);
            }

            opToCachingOp.put(op, newOp);
        }

        for(Op op : nonCachedCacheableOps) {
            ProjectedQuadFilterPattern pqfp = cacheableOps.get(op);

            if(pqfp == null) { // TODO Turn into an assertion
                throw new RuntimeException("Should not happen");
            }

            boolean silent = true;
            Query subQuery = OpAsQuery.asQuery(op);
            Element subElement = new ElementSubQuery(subQuery);
            //Op newOp = new OpSubQueryExecution(qef, subQuery);
            //Node serviceNode = NodeFactory.createURI("cache://");

            // TODO Chose the index vars
            //Set<Var> indexVars = OpVars.mentionedVars(op);


            ElementService elt = new ElementService(serviceNode, subElement, silent);
            Op newOp = new OpService(serviceNode, op, elt, silent);

            opToCachingOp.put(op, newOp);
        }

        // Perform the substitution
        Op rootOp = OpUtils.substitute(rawOp, false, x -> opToCachingOp.get(x));

        Query result = OpAsQuery.asQuery(rootOp);

        //System.out.println("Rewritten query: " + query);

        return result;
    }



    /**
     * ([?s ?s ?s ?o], (fn(?s, ?o))
     *
     * @param quad
     * @param expr
     */
    public static Set<Set<Expr>> normalize(Quad quad, Set<Set<Expr>> cnf) {
        List<Var> componentVars = Arrays.asList(Var.alloc("g"), Var.alloc("s"), Var.alloc("p"), Var.alloc("o"));

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

    public static ProjectedQuadFilterPattern transform(Query query) {

        Op op = Algebra.compile(query);
        op = Algebra.toQuadForm(op);
        op = ReplaceConstants.replace(op);
        ProjectedQuadFilterPattern result = transform(op);
        return result;
    }

    public static ProjectedQuadFilterPattern transform(Element element) {

        Op op = Algebra.compile(element);
        ProjectedQuadFilterPattern result = transform(op);
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

        //QuadFilterPattern result = null;

        //op = Algebra.optimize(op);
//        op = Algebra.toQuadForm(op);
//        op = ReplaceConstants.replace(op);

//        if(op instanceof OpDistinct) {
//            op = ((OpDistinct)op).getSubOp();
//        }

        //OpProject opProject;
        if(op instanceof OpProject) {
            OpProject tmp = (OpProject)op;
            projectVars = new HashSet<>(tmp.getVars());

            op = tmp.getSubOp();
        }

        OpFilter opFilter;
        if(op instanceof OpFilter) {
            opFilter = (OpFilter)op;
        } else {
            opFilter = (OpFilter)OpFilter.filter(NodeValue.TRUE, op);
        }

        Op subOp = opFilter.getSubOp();



        if(subOp instanceof OpGraph) {
            OpGraph opGraph = (OpGraph)subOp;
            Node graphNode = opGraph.getNode();

            // The graphNode must be a variable which is not constrained by the filter

            Set<Var> filterVars = ExprVars.getVarsMentioned(opFilter.getExprs());
            if(graphNode.isVariable() && !filterVars.contains(graphNode)) {
                subOp = opGraph.getSubOp();
            } else {
                subOp = null;
            }
        }

        if(subOp instanceof OpQuadPattern) {
            OpQuadPattern opQuadPattern = (OpQuadPattern)opFilter.getSubOp();

            QuadPattern quadPattern = opQuadPattern.getPattern();
            List<Quad> quads = quadPattern.getList();

            ExprList exprs = opFilter.getExprs();
            Expr expr = ExprUtils.andifyBalanced(exprs);

            if(projectVars == null) {
                projectVars = new HashSet<>(OpVars.mentionedVars(opQuadPattern));
            }

            QuadFilterPattern qfp = new QuadFilterPattern(quads, expr);
            result = new ProjectedQuadFilterPattern(projectVars, qfp);
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

    public static PatternSummary summarize(QuadFilterPattern originalPattern) {

        Expr expr = originalPattern.getExpr();
        Set<Quad> quads = new HashSet<Quad>(originalPattern.getQuads());


        Set<Set<Expr>> filterCnf = CnfUtils.toSetCnf(expr);

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

        //System.out.println("varOccurrences: " + varOccurrences);

        // Remove all variables that only occur in the same quad
        boolean pruneVarOccs = false;
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

        //Set<Set<Set<Expr>>> quadCnfs = new HashSet<Set<Set<Expr>>>(quadCnfList);

        QuadFilterPatternCanonical canonicalPattern = new QuadFilterPatternCanonical(quads, filterCnf);

        PatternSummary result = new PatternSummary(originalPattern, canonicalPattern, quadToCnf, varOccurrences);


        for(Entry<Var, Collection<VarOccurrence>> entry : varOccurrences.asMap().entrySet()) {
            //System.out.println("Summary: " + entry.getKey() + ": " + entry.getValue().size());
            //System.out.println(entry);
        }

        return result;
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
     * Helper function to convert a multimap into a map.
     * Each key may only have at most one corresponding value,
     * otherwise an exception will be thrown.
     *
     * @param mm
     * @return
     */
    public static <K, V> Map<K, V> toMap(Map<K, ? extends Collection<V>> mm) {
        // Convert the multimap to an ordinate map
        Map<K, V> result = new HashMap<K, V>();
        for(Entry<K, ? extends Collection<V>> entry : mm.entrySet()) {
            K k = entry.getKey();
            Collection<V> vs = entry.getValue();

            if(!vs.isEmpty()) {
                if(vs.size() > 1) {
                    throw new RuntimeException("Ambigous mapping for " + k + ": " + vs);
                }

                V v = vs.iterator().next();
                result.put(k, v);
            }
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

    /**
     * Find a mapping of variables from cand to query, such that the pattern of
     * cand becomes a subset of that of query
     *
     * null if no mapping can be established
     *
     * @param query
     * @param cand
     * @return
     */
//
//    public Iterator<Map<Var, Var>> computeVarMapQuadBased(PatternSummary query, PatternSummary cand, Set<Set<Var>> candVarCombos) {
//
//        IBiSetMultimap<Set<Set<Expr>>, Quad> cnfToCandQuad = cand.getQuadToCnf().getInverse();
//        IBiSetMultimap<Set<Set<Expr>>, Quad> cnfToQueryQuad = query.getQuadToCnf().getInverse();
//
//        //IBiSetMultimap<Quad, Quad> candToQuery = new BiHashMultimap<Quad, Quad>();
////        Map<Set<Set<Expr>>, QuadGroup> cnfToQuadGroup = new HashMap<Set<Set<Expr>>, QuadGroup>();
//        List<QuadGroup> quadGroups = new ArrayList<QuadGroup>();
//        for(Entry<Set<Set<Expr>>, Collection<Quad>> entry : cnfToCandQuad.asMap().entrySet()) {
//
//            //Quad candQuad = entry.getKey();
//            Set<Set<Expr>> cnf = entry.getKey();
//
//            Collection<Quad> candQuads = entry.getValue();
//            Collection<Quad> queryQuads = cnfToQueryQuad.get(cnf);
//
//            if(queryQuads.isEmpty()) {
//                return Collections.<Map<Var, Var>>emptySet().iterator();
//            }
//
//            QuadGroup quadGroup = new QuadGroup(candQuads, queryQuads);
//            quadGroups.add(quadGroup);
//
//            // TODO We now have grouped together quad having the same constraint summary
//            // Can we derive some additional constraints form the var occurrences?
//
//
////            SetMultimap<Quad, Quad> summaryToQuadsCand = quadJoinSummary(new ArrayList<Quad>(candQuads));
////            System.out.println("JoinSummaryCand: " + summaryToQuadsCand);
////
////            SetMultimap<Quad, Quad> summaryToQuadsQuery = quadJoinSummary(new ArrayList<Quad>(queryQuads));
////            System.out.println("JoinSummaryQuery: " + summaryToQuadsQuery);
////
////            for(Entry<Quad, Collection<Quad>> candEntry : summaryToQuadsCand.asMap().entrySet()) {
////                queryQuads = summaryToQuadsQuery.get(candEntry.getKey());
////
////                // TODO What if the mapping is empty?
////                QuadGroup group = new QuadGroup(candEntry.getValue(), queryQuads);
////
////                cnfToQuadGroup.put(cnf, group);
////            }
//        }
//
//        // Figure out which quads have ambiguous mappings
//
////        for(Entry<Set<Set<Expr>>, QuadGroup>entry : cnfToQuadGroup.entrySet()) {
////            System.out.println(entry.getKey() + ": " + entry.getValue());
////        }
//
//        // Order the quad groups by number of candidates - least number of candidates first
////        List<QuadGroup> quadGroups = new ArrayList<QuadGroup>(cnfToQuadGroup.values());
//        Collections.sort(quadGroups, new Comparator<QuadGroup>() {
//            @Override
//            public int compare(QuadGroup a, QuadGroup b) {
//                int i = getNumMatches(a);
//                int j = getNumMatches(b);
//                int r = j - i;
//                return r;
//            }
//        });
//
//
//        List<Iterable<Map<Var, Var>>> cartesian = new ArrayList<Iterable<Map<Var, Var>>>(quadGroups.size());
//
//        // TODO Somehow obtain a base mapping
//        Map<Var, Var> baseMapping = Collections.<Var, Var>emptyMap();
//
//        for(QuadGroup quadGroup : quadGroups) {
//            Iterable<Map<Var, Var>> it = IterableVarMapQuadGroup.create(quadGroup, baseMapping);
//            cartesian.add(it);
//        }
//
//        CartesianProduct<Map<Var, Var>> cart = new CartesianProduct<Map<Var,Var>>(cartesian);
//
//        Iterator<Map<Var, Var>> result = new IteratorVarMapQuadGroups(cart.iterator());
//
//        return result;
//    }

    public static <K, V> Map<K, V> mergeCompatible(Iterable<Map<K, V>> maps) {
        Map<K, V> result = new HashMap<K, V>();

        for(Map<K, V> map : maps) {
            if(MapUtils.isPartiallyCompatible(map, result)) {
                result.putAll(map);
            } else {
                result = null;
                break;
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


    public static Table createTable(ResultSet rs) {

        List<Var> vars = VarUtils.toList(rs.getResultVars());

        Table result = TableFactory.create(vars);

        while(rs.hasNext()) {
            Binding binding = rs.nextBinding();
            result.addBinding(binding);
        }

        return result;
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



}
