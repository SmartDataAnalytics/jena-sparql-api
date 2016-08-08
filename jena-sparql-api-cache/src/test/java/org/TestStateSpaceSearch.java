package org;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.combinatorics.algos.KPermutationsOfNUtils;
import org.aksw.combinatorics.collections.Combination;
import org.aksw.combinatorics.solvers.ProblemContainerNeighbourhoodAware;
import org.aksw.combinatorics.solvers.ProblemNeighborhoodAware;
import org.aksw.combinatorics.solvers.ProblemStaticSolutions;
import org.aksw.commons.collections.CartesianProduct;
import org.aksw.commons.collections.stacks.NestedStack;
import org.aksw.jena_sparql_api.algebra.transform.TransformJoinToConjunction;
import org.aksw.jena_sparql_api.algebra.transform.TransformUnionToDisjunction;
import org.aksw.jena_sparql_api.concept_cache.collection.FeatureMap;
import org.aksw.jena_sparql_api.concept_cache.collection.FeatureMapImpl;
import org.aksw.jena_sparql_api.concept_cache.combinatorics.ProblemVarMappingExpr;
import org.aksw.jena_sparql_api.concept_cache.core.SparqlCacheUtils;
import org.aksw.jena_sparql_api.concept_cache.dirty.Tree;
import org.aksw.jena_sparql_api.concept_cache.dirty.TreeImpl;
import org.aksw.jena_sparql_api.concept_cache.domain.ProjectedQuadFilterPattern;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.concept_cache.op.OpUtils;
import org.aksw.jena_sparql_api.concept_cache.op.TreeUtils;
import org.aksw.jena_sparql_api.sparql.algebra.mapping.IterableUnknownSize;
import org.aksw.jena_sparql_api.sparql.algebra.mapping.IterableUnknownSizeSimple;
import org.aksw.jena_sparql_api.sparql.algebra.mapping.MatchingStrategyFactory;
import org.aksw.jena_sparql_api.sparql.algebra.mapping.SequentialMatchIterator;
import org.aksw.jena_sparql_api.sparql.algebra.mapping.TreeMapperImpl;
import org.aksw.jena_sparql_api.sparql.algebra.mapping.VarMapper;
import org.aksw.jena_sparql_api.stmt.SparqlElementParser;
import org.aksw.jena_sparql_api.stmt.SparqlElementParserImpl;
import org.aksw.jena_sparql_api.unsorted.ExprMatcher;
import org.aksw.jena_sparql_api.utils.DnfUtils;
import org.aksw.jena_sparql_api.utils.Generator;
import org.aksw.jena_sparql_api.utils.VarGeneratorImpl2;
import org.aksw.jena_sparql_api.views.index.SparqlCacheSystemImpl;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.util.ExprUtils;

import com.codepoetics.protonpack.functions.TriFunction;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;



interface ExprQuadPattern {

}

interface TreeMatcher {
    
}

public class TestStateSpaceSearch {
    
   
    
    
//    public static void main(String[] args) {
//        Multimap<String, Integer> m = HashMultimap.create();
//        
//        m.put("a", 1);
//        m.put("a", 3);
//        
//        m.put("b", 1);
//        m.put("b", 3);
//        m.put("b", 4);
//        
//        Stream<CombinationStack<String, Integer, Object>> s = KPermutationsOfNUtils.kPermutationsOfN(m);
//        
//        s.forEach(i -> System.out.println(i.asList()));
//    }
    
    //public static void 


    public static <A, B> Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> createProblems(A a, B b) {
        
        
        Map<Class<?>, GenericBinaryOp<Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>>>> map = new HashMap<>();
        map.put(OpProject.class, GenericBinaryOpImpl.create(TestStateSpaceSearch::deriveProblemProject));
        map.put(OpDistinct.class, (x, y) -> Collections.emptySet());
        
        Class<?> ac = a.getClass();
        Class<?> bc = b.getClass();
     
        Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> result;
        
        if(ac.equals(bc)) {
            GenericBinaryOp<Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>>> problemFactory = map.get(ac);
            result = problemFactory.apply(a, b);
        } else {
            result = Collections.singleton(new ProblemStaticSolutions<>(Collections.singleton(null)));
        }

        return result;
        
//        map.put(OpReduced.class, value);
//        map.put(OpFilter.class, value);
//        map.put(OpExtend.class, value);
//        map.put(OpGraph.class, value);
//        map.put(OpGroup.class, value);
//        map.put(OpOrder.class, value);
        
        
    }
    
    
    /**
     * 
     * 
     * @param cacheOp
     * @param queryOp
     * @return
     */
    public static <A, B> MatchingStrategyFactory<A, B> determineMatchingStrategy(A cacheOp, B queryOp) {
//        A cacheOp = nodeMapping.getKey();
//        B queryOp = nodeMapping.getValue();

        Map<Class<?>, MatchingStrategyFactory<A, B>> opToMatcherTest = new HashMap<>(); 
        opToMatcherTest.put(OpDisjunction.class, (as, bs, mapping) -> KPermutationsOfNUtils.createIterable(mapping));

        Function<Class<?>, MatchingStrategyFactory<A, B>> fnOpToMatcherTest = (nodeType) ->
            opToMatcherTest.getOrDefault(nodeType, (as, bs, mapping) -> SequentialMatchIterator.createIterable(as, bs, mapping));

        
        MatchingStrategyFactory<A, B> result;
        
        int c = (cacheOp == null ? 0 : 1) | (queryOp == null ? 0 : 2);
        switch(c) {
        case 0: // both null - nothing to do because the candidate mapping of the children (each tree's root node) is already the final solution
            result = (as, bs, mapping) -> SequentialMatchIterator.createIterable(as, bs, mapping); // true
            break;
        case 1: // queryOp null - no match because the cache tree has greater depth than the query 
            result = (as, bs, mapping) -> IterableUnknownSizeSimple.createEmpty(); // false
            break;
        case 2: // cacheOp null - match because a cache tree's super root (i.e. null) matches any query node (including null)
            result = (as, bs, mapping) -> SequentialMatchIterator.createIterable(as, bs, mapping); // true
            break;
        case 3: // both non-null - by default, both ops must be of equal type - the type determines the matching enumeration strategy
            Class<?> ac = cacheOp.getClass();
            Class<?> bc = queryOp.getClass();
         
            if(ac.equals(bc)) {
                result = fnOpToMatcherTest.apply(ac);
            } else {
                result = (as, bs, mapping) -> IterableUnknownSizeSimple.createEmpty();;
            }
            break;
        default:
            throw new IllegalStateException();
        }
        
        return result;
    }

    
  
    
    public static <X> List<X> getUnaryAncestors(X x, Tree<X> tree, Tree<X> multiaryTree) {
        List<X> result = new ArrayList<>();
        
        X ancestor = multiaryTree.getParent(x);
        
        X currentNode = x;
        while((currentNode = tree.getParent(currentNode)) != null && !currentNode.equals(ancestor)) {
            result.add(currentNode);
        }

        
        return result;
    }
    
    
    public static <A, B, S> Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> createProblemsFromUnaryAncestors(List<A> aOps, List<B> bOps) {
        // for now the sequences must match
        int as = aOps.size();
        int bs = bOps.size();
        
        Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> result = new ArrayList<>();
        if(as == bs) {
            for(int i = 0; i < as; ++i) {
                A a = aOps.get(i);
                B b = bOps.get(i);
                
                Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> problems = createProblems(a, b);
                result.addAll(problems);
            }            
        }
        
        return result;            
    }
    
    
    public static ProblemNeighborhoodAware<Map<Var, Var>, Var> deriveProblem(List<Var> cacheVars, List<Var> userVars) {
        List<Expr> aExprs = cacheVars.stream().map(v -> new ExprVar(v)).collect(Collectors.toList());
        List<Expr> bExprs = userVars.stream().map(v -> new ExprVar(v)).collect(Collectors.toList());
        ProblemNeighborhoodAware<Map<Var, Var>, Var> result = new ProblemVarMappingExpr(aExprs, bExprs, Collections.emptyMap());
        return result;
    }
    
    
    
    public static Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> deriveProblemProject(OpProject cacheOp, OpProject userOp) {
        ProblemNeighborhoodAware<Map<Var, Var>, Var> tmp = deriveProblem(cacheOp.getVars(), userOp.getVars());
        Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> result = Collections.singleton(tmp);        
        return result;        
    }
    
    
    
    
    
    public static void main(String[] args) {
        
        Map<Class<?>, TriFunction<List<Op>, List<Op>, Multimap<Op, Op>, Boolean>> opToMatcherTest = new HashMap<>(); 
        opToMatcherTest.put(OpDisjunction.class, (as, bs, mapping) -> true);

//        Function<Entry<Op, Op>, TriFunction<List<Op>, List<Op>, Multimap<Op, Op>, Boolean>> fnOpToMatcherTest = (nodeMapping) ->
//            determineMatchingStrategy(nodeMapping);
            //opToMatcherTest.getOrDefault(op.getClass(), (as, bs, mapping) -> SequentialMatchIterator.createStream(as, bs, mapping).findFirst().isPresent());

        Map<Class<?>, TriFunction<List<Op>, List<Op>, Multimap<Op, Op>, Stream<Map<Op, Op>>>> opToMatcher = new HashMap<>(); 
        
        opToMatcher.put(OpDisjunction.class, (as, bs, mapping) -> KPermutationsOfNUtils.kPermutationsOfN(mapping));
        //opToMatcher.put(OpLeftJoin.class, (as, bs, mapping) -> SequentialMatchIterator.createStream(as, bs, mapping));

        // Function that maps an operator to the matching strategy
        Function<Class<?>, TriFunction<List<Op>, List<Op>, Multimap<Op, Op>, Stream<Map<Op, Op>>>> fnOpToMatcher = (op) ->
            opToMatcher.getOrDefault(op, (as, bs, mapping) -> SequentialMatchIterator.createStream(as, bs, mapping));

        
        List<String> as = Arrays.asList("a", "b", "c");
        List<Integer> bs = Arrays.asList(1, 2, 3, 4);
        
        Multimap<String, Integer> ms = HashMultimap.create();
        ms.put("a", 1);
        ms.put("a", 2);
        ms.put("b", 2);
        ms.put("b", 3);
        ms.put("c", 3);
        ms.put("c", 4);

        //Iterator<Map<String, Integer>> it = new SequentialMatchIterator<>(as, bs, (a, b) -> ms.get(a).contains(b));
        Stream<Map<String, Integer>> it = SequentialMatchIterator.createStream(as, bs, ms);

        it.forEach(x -> System.out.println("seq match: " + x));

        

        int test = 0;

        
        Op opCache;

        if(test != 2) {
            opCache = Algebra.toQuadForm(Algebra.compile(QueryFactory.create("SELECT DISTINCT ?s { { { ?a ?a ?a } UNION {   { SELECT DISTINCT ?b { ?b ?b ?b} }   } } ?c ?c ?c } LIMIT 10")));
        } else {
            opCache = Algebra.toQuadForm(Algebra.compile(QueryFactory.create("SELECT * { ?a ?a ?a }")));
        }
        //Op opQuery = Algebra.toQuadForm(Algebra.compile(QueryFactory.create("SELECT DISTINCT ?s { { { ?0 ?0 ?0 } UNION { ?1 ?1 ?1 } } { { ?2 ?2 ?2 } UNION { ?3 ?3 ?3 } } { ?4 ?4 ?4 } } LIMIT 10")));
        Op opQuery = Algebra.toQuadForm(Algebra.compile(QueryFactory.create("Select * { { SELECT DISTINCT ?s { { { ?0 ?0 ?0 } UNION { {   SELECT DISTINCT ?1 { ?1 ?1 ?1 } }   } } { { ?2 ?2 ?2 } UNION { ?3 ?3 ?3 } } { ?4 ?4 ?4 } } LIMIT 10 } { ?f ?c ?k } }")));

                
        opCache = Transformer.transform(TransformJoinToConjunction.fn, Transformer.transform(TransformUnionToDisjunction.fn, opCache));
        opQuery = Transformer.transform(TransformJoinToConjunction.fn, Transformer.transform(TransformUnionToDisjunction.fn, opQuery));
                
        Tree<Op> cacheTree = TreeImpl.create(opCache, (o) -> OpUtils.getSubOps(o));
        Tree<Op> queryTree = TreeImpl.create(opQuery, (o) -> OpUtils.getSubOps(o));

        System.out.println("Query Tree:\n" + queryTree);
        System.out.println("Cache Tree:\n" + cacheTree);
        
//        System.out.println("root:" + tree.getRoot());
//        System.out.println("root:" + tree.getChildren(tree.getRoot()));
        
        Tree<Op> cacheMultiaryTree = SparqlCacheSystemImpl.removeUnaryNodes(cacheTree);
        Tree<Op> queryMultiaryTree = SparqlCacheSystemImpl.removeUnaryNodes(queryTree);
        //System.out.println("Multiary tree: " + cacheMultiaryTree);
        
        
        // The candidate multimapping from cache to query
        Multimap<Op, Op> candOpMapping = HashMultimap.create();
        List<Op> cacheLeafs = TreeUtils.getLeafs(cacheTree);        
        List<Op> queryLeafs = TreeUtils.getLeafs(queryTree);
        
        
        if(test == 0) {
            // Expected: a:1 - b:2
            candOpMapping.put(cacheLeafs.get(0), queryLeafs.get(0));
            candOpMapping.put(cacheLeafs.get(0), queryLeafs.get(2));
    
            candOpMapping.put(cacheLeafs.get(1), queryLeafs.get(0));
            candOpMapping.put(cacheLeafs.get(1), queryLeafs.get(2));
            candOpMapping.put(cacheLeafs.get(1), queryLeafs.get(3));

            candOpMapping.put(cacheLeafs.get(2), queryLeafs.get(4));
        }
        
        if(test == 1) {
            // Expected: a:1 - b:0
            candOpMapping.put(cacheLeafs.get(0), queryLeafs.get(0));
            candOpMapping.put(cacheLeafs.get(0), queryLeafs.get(1));
    
            candOpMapping.put(cacheLeafs.get(1), queryLeafs.get(0));
            candOpMapping.put(cacheLeafs.get(1), queryLeafs.get(2));
            candOpMapping.put(cacheLeafs.get(1), queryLeafs.get(3));
        }

        // test case where there the cache op is just a single node (i.e. there is no parent)
        if(test == 2) {
            // Expected: a:1 - b:0
            candOpMapping.put(cacheLeafs.get(0), queryLeafs.get(0));
   
        }
        

//        List<Set<Op>> cacheTreeLevels = TreeUtils.nodesPerLevel(cacheMultiaryTree);
//        List<Set<Op>> queryTreeLevels = TreeUtils.nodesPerLevel(queryMultiaryTree);
        
        

        // The tree mapper only determines sets of candidate mappings for each tree level
        // 
        TreeMapperImpl<Op, Op> tm = new TreeMapperImpl<Op, Op>(
                cacheMultiaryTree,
                queryMultiaryTree,
                candOpMapping,
                TestStateSpaceSearch::determineMatchingStrategy);
        
        Stream<NestedStack<Multimap<Op, Op>>> mappingStream = TreeMapperImpl.<Multimap<Op, Op>, NestedStack<Multimap<Op, Op>>>stream(tm::recurse, HashMultimap.<Op, Op>create());
        
        mappingStream.forEach(m -> { 
            //Multimap<Op, Op> fullMap = mapping
            for(Multimap<Op, Op> layer: m) {
                
                
                // TODO We need the clusters together with the mapping strategy
                
                
                
                // From the candidate mapping we now need to create the concrete mappings
                //KPermutationsOfNUtils.
                //determineMatchingStrategy(cacheOp, queryOp)
                
                
                
            }
            
            System.out.println("Tree mapping solution: " + m);
        });

        
        

        Op cacheLeaf = cacheLeafs.get(1);
        List<Op> cacheUnaryAncestors = getUnaryAncestors(cacheLeaf, cacheTree, cacheMultiaryTree);

        Op queryLeaf = queryLeafs.get(1);
        List<Op> queryUnaryAncestors = getUnaryAncestors(queryLeaf, queryTree, queryMultiaryTree);
        

//        System.out.println("unary parents: " + unaryParents);
        Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> problems = createProblemsFromUnaryAncestors(cacheUnaryAncestors, queryUnaryAncestors); 
        Stream<Map<Var, Var>> solutions = VarMapper.solve(problems);
        solutions.forEach(s -> System.out.println("found solution: " + s));

        
        
        
        
        //  
        
        // tm.recurse(0, HashMultimap.create());
        
        
        
//        Collections.reverse(cacheTreeLevels);
//        Collections.reverse(queryTreeLevels);
//        
//        int cacheMultiaryTreeDepth = cacheTreeLevels.size();
//        int queryMultiaryTreeDepth = queryTreeLevels.size();
//
//        for(int i = 0; i < cacheMultiaryTreeDepth; ++i) {
//            Set<Op> keys = cacheTreeLevels.get(cacheMultiaryTreeDepth - 1 - i);
//            Set<Op> values = queryTreeLevels.get(queryMultiaryTreeDepth - 1 - i);
//            
//            candOpMapping = Multimaps.filterEntries(candOpMapping, new Predicate<Entry<Op, Op>>() {
//                @Override
//                public boolean apply(Entry<Op, Op> input) {
//                    boolean result = keys.contains(input.getKey()) && values.contains(input.getValue());
//                    return result;
//                }            
//            });
//            
//            Stream<ClusterStack<Op, Op, Entry<Op, Op>>> stream = KPermutationsOfNUtils.<Op, Op>kPermutationsOfN(
//                    candOpMapping,
//                    cacheMultiaryTree,
//                    queryMultiaryTree);
//            
//            stream.forEach(parentMapping -> {
//               recurse(depth + 1, parentMapping); 
//            });
//        }
        
        
        // Now that we have the clusters, how to proceed?
        
        
        
        //stream.forEach(x -> System.out.println("Candidate Solution: " + x.asList().iterator().next()));
        
        
        // we need a mapping from leaf op to problem instance in order to determine which of the candidates to pick first
        //Map<Op, Problem<?>> cacheOpToProblem = new HashMap<>();
        Function<Entry<Op, Op>, Long> opMappingToCost = (e) -> 1l;
        
        //TreeMultimap<Long, Entry<Op, Op>> costToOpMapping = TreeMultimap.create();
        TreeMap<Long, Set<Entry<Op, Op>>> costToOpMappings = new TreeMap<>();
        
        // pick the cheapest mapping candidate
        // actually, this is again a problem instance - right?
        Entry<Long, Entry<Op, Op>> pick = ProblemContainerNeighbourhoodAware.firstEntry(costToOpMappings);

        SparqlCacheSystemImpl.clusterNodesByFirstMultiaryAncestor(queryTree, candOpMapping);
        
        
        
        
        
        /*
         * 
         * We now need to pick one of the mappings, and update the remaining ones.
         *
         * 1. pick a mapping
         *   
         * 2. cluster mapping by the query's parent node
         * 3. 
         *  
         */
                
    }
    
    //public static void pick(})
    
    
    public static void matchSequence(Tree<Op> cacheTree, Tree<Op> queryTree, Op cacheNode, Op queryNode) {
        
    }
    
    public static void matchAnyComination() {
        
    }
    
    
   
    
    
    public static void main2(String[] args) throws FileNotFoundException {
        {
//            QueryExecutionFactory qef = FluentQueryExecutionFactory
//                    .http("http://linkedgeodata.org/test/vsparql")
//                    .config()
//                        .withParser(SparqlQueryParserImpl.create())
//                        .withDatasetDescription(DatasetDescriptionUtils.createDefaultGraph("http://linkedgeodata.org/ne/"))
//                        .withQueryTransform(F_QueryTransformDatesetDescription.fn)
//                    .end()
//                    .create();
//            Model model = qef.createQueryExecution("CONSTRUCT WHERE { ?s ?p ?o }").execConstruct();
//
//            //System.out.println(ResultSetFormatter.asText(qef.createQueryExecution("SELECT (count(*) As ?c) FROM <http://linkedgeodata.org/ne/> WHERE { ?s ?p ?o }").execSelect()));
//            model.write(new FileOutputStream(new File("/tmp/ne.nt")), "NTRIPLES");
            //model.write(System.out);
        }


        // TODO How could we combine the state space

        {
            Set<Expr> a = DnfUtils.toSetDnf(ExprUtils.parse("?a = <http://foo> && strStarts(?b, 'foo')")).iterator().next();
            Set<Expr> b = DnfUtils.toSetDnf(ExprUtils.parse("?a = <http://foo> && strStarts(?b, 'foob') && strStarts(?b, 'foobar') && ?x = ?y && ?a = <http://bar>")).iterator().next();

            System.out.println("a: " + a);
            System.out.println("b: " + b);

            CartesianProduct<Combination<Expr, Expr, Expr>> c = ExprMatcher.match(a, b);

            for(List<Combination<Expr, Expr, Expr>> com : c) {
                System.out.println("GOT COMBINATION:" + com);
            }

        }

        // We could create a graph over quads and expressions that variables

        SparqlElementParser elementParser = SparqlElementParserImpl.create(Syntax.syntaxSPARQL_10, null);
        Element queryElement = elementParser.apply("?x <my://type> <my://Airport> ; <my://label> ?n ; ?h ?i . FILTER(langMatches(lang(?n), 'en')) . FILTER(<mp://fn>(?x, ?n))");

        Element cacheElement = elementParser.apply("?s <my://type> <my://Airport> ; ?p ?l . FILTER(?p = <my://label> || ?p = <my://name>)");


        FeatureMap<String, String> tagsToCache = new FeatureMapImpl<>();
        Op cacheOp = Algebra.compile(cacheElement);
        cacheOp = Algebra.toQuadForm(cacheOp);

        //IndexSystem<Op, Op, ?> indexSystem = IndexSystem.create();

//        Set<String> cacheFeatures = OpVisitorFeatureExtractor.getFeatures(cacheOp, (op) -> op.getClass().getSimpleName());
//
//        tagsToCache.put(cacheFeatures, "cache1");


        Op queryOp = Algebra.compile(queryElement);
        queryOp = Algebra.toQuadForm(queryOp);
        //Set<String> queryFeatures = OpVisitorFeatureExtractor.getFeatures(queryOp, (op) -> op.getClass().getSimpleName());

//        Collection<Entry<Set<String>, String>> cacheCandidates = tagsToCache.getIfSubsetOf(queryFeatures);
//
//        cacheCandidates.forEach(x -> {
//           System.out.println("cache candidate: " + x.getValue());
//        });

        SparqlCacheSystemImpl cacheSystem = new SparqlCacheSystemImpl();
        cacheSystem.registerCache("test", cacheOp);

        cacheSystem.rewriteQuery(queryOp);

//        if(true) {
//            System.out.println("weee");
//            System.exit(0);
//        }

        ProjectedQuadFilterPattern cachePqfp = SparqlCacheUtils.transform(cacheElement);
        System.out.println("ProjectedQuadFilterPattern[cache]: " + cachePqfp);

        ProjectedQuadFilterPattern queryPqfp = SparqlCacheUtils.transform(queryElement);
        System.out.println("ProjectedQuadFilterPattern[query]: " + queryPqfp);

        Generator<Var> generator = VarGeneratorImpl2.create();
        QuadFilterPatternCanonical cacheQfpc = SparqlCacheUtils.canonicalize2(cachePqfp.getQuadFilterPattern(), generator);
        System.out.println("QuadFilterPatternCanonical[cache]: " + cacheQfpc);


        QuadFilterPatternCanonical queryQfpc = SparqlCacheUtils.canonicalize2(queryPqfp.getQuadFilterPattern(), generator);
        System.out.println("QuadFilterPatternCanonical[query]: " + queryQfpc);


        Stream<Map<Var, Var>> candidateSolutions = VarMapper.createVarMapCandidates(cacheQfpc, queryQfpc);
        candidateSolutions.forEach(cs -> System.out.println("Candidate solution: " + cs));
        System.out.println("Done.");
    }
}


        //ContainmentMap<Expr, CacheEntry> featuresToCache = indexDnf(queryQfpc.getFilterDnf());


        // Index the clauses of the cache
//        FeatureMap<Expr, Multimap<Expr, Expr>> cacheIndex = SparqlCacheUtils.indexDnf(cacheQfpc.getFilterDnf());
//        FeatureMap<Expr, Multimap<Expr, Expr>> queryIndex = SparqlCacheUtils.indexDnf(queryQfpc.getFilterDnf());

//
//        Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> problems = new ArrayList<>();
//        for(Entry<Set<Expr>, Collection<Multimap<Expr, Expr>>> entry : queryIndex.entrySet()) {
//            Set<Expr> querySig = entry.getKey();
//            Collection<Multimap<Expr, Expr>> queryMaps = entry.getValue();
//
//            System.out.println("CAND LOOKUP with " + querySig);
//            Collection<Entry<Set<Expr>, Multimap<Expr, Expr>>> cands = cacheIndex.getIfSubsetOf(querySig);
//
//            for(Entry<Set<Expr>, Multimap<Expr, Expr>> e : cands) {
//                Multimap<Expr, Expr> cacheMap = e.getValue();
//                System.out.println("  CACHE MAP: " + cacheMap);
//                for(Multimap<Expr, Expr> queryMap : queryMaps) {
//                    Map<Expr, Entry<Set<Expr>, Set<Expr>>> group = MapUtils.groupByKey(cacheMap.asMap(), queryMap.asMap());
//
//                    Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> localProblems = group.values().stream()
//                        .map(x -> {
//                            Set<Expr> cacheExprs = x.getKey();
//                            Set<Expr> queryExprs = x.getValue();
//                            ProblemNeighborhoodAware<Map<Var, Var>, Var> p = new ProblemVarMappingExpr(cacheExprs, queryExprs, Collections.emptyMap());
//
//                            //System.out.println("cacheExprs: " + cacheExprs);
//                            //System.out.println("queryExprs: " + queryExprs);
//
//                            //Stream<Map<Var, Var>> r = p.generateSolutions();
//
//                            return p;
//                        })
//                        .collect(Collectors.toList());
//
//                    problems.addAll(localProblems);
//                    //problems.stream().forEach(p -> System.out.println("COMPLEX: " + p.getEstimatedCost()));
//
//
//                    //problemStream.forEach(y -> System.out.println("GOT SOLUTION: " + y));
//
//
//
//                    //System.out.println("    QUERY MAP: " + queryMap);
//                }
//            }
//
//            //cands.forEach(x -> System.out.println("CAND: " + x.getValue()));
//        }
//
//        ProblemVarMappingQuad quadProblem = new ProblemVarMappingQuad(cacheQfpc.getQuads(), queryQfpc.getQuads(), Collections.emptyMap());
//        problems.add(quadProblem);
//
//        for(int i = 0; i < 1000; ++i) {
//            Stopwatch sw = Stopwatch.createStarted();
//
//            ProblemContainerNeighbourhoodAware.solve(
//                    problems,
//                    Collections.emptyMap(),
//                    Map::keySet,
//                    MapUtils::mergeIfCompatible,
//                    Objects::isNull,
//                    (s) -> { System.out.println("solution: " + s); });

//            ProblemContainer<Map<Var, Var>> container = ProblemContainerImpl.create(problems);
//
//            State<Map<Var, Var>> state = container.isEmpty()
//                    ? new StateProblemContainer<Map<Var, Var>>(null, Objects::isNull, container, (a, b) -> MapUtils.mergeIfCompatible(a, b))
//                    : new StateProblemContainer<Map<Var, Var>>(Collections.emptyMap(), Objects::isNull, container, (a, b) -> MapUtils.mergeIfCompatible(a, b));
//            Stream<Map<Var, Var>> xxx = StateSearchUtils.depthFirstSearch(state, 10000);
//
//            for(Map<Var, Var> m : xxx.collect(Collectors.toList())) {
//                if(m != null) {
//                    System.out.println("SOLUTION: " + m);
//                    NodeTransform nodeTransform = new NodeTransformRenameMap(m);
//                    QuadFilterPatternCanonical foo = cacheQfpc.applyNodeTransform(nodeTransform);
//                    System.out.println("Cache after var mapping: " + foo);
//                    QuadFilterPatternCanonical diff = queryQfpc.diff(foo);
//                    System.out.println("DIFF: " + diff + "\nfrom cache " + foo + "\nand query " + queryQfpc);
//                }
//            }

            //xxx.forEach(x -> System.out.println("SOLUTION: " + x));


//            System.out.println("TIME TAKEN: " + sw.stop().elapsed(TimeUnit.MILLISECONDS));
//        }
//        System.out.println("PROBLEMS: " + problems.size());




//        System.out.println(cacheSigToExprs);
//
//        ContainmentMap<Expr, Set<Expr>> querySigToExprs = new ContainmentMapImpl<>();
//        for(Set<Expr> clause : queryQfpc.getFilterDnf()) {
//            Set<Expr> clauseSig = ClauseUtils.signaturize(clause);
//            querySigToExprs.put(clauseSig, clause);
//        }
//
//        for(Set<Expr> queryClause : querySigToExprs.keySet()) {
//            System.out.println("CAND LOOKUP with " + queryClause);
//            Collection<Entry<Set<Expr>, Set<Expr>>> cands = cacheSigToExprs.getAllEntriesThatAreSubsetOf(queryClause);
//            cands.forEach(x -> System.out.println("CAND: " + x));
//        }

//if(false) {
//        //ClauseUtils.signaturize(clause)
//        IBiSetMultimap<Quad, Set<Set<Expr>>> queryQuadIndex = SparqlCacheUtils.createMapQuadsToFilters(queryQfpc);
//
//        IBiSetMultimap<Quad, Set<Set<Expr>>> cacheQuadIndex = SparqlCacheUtils.createMapQuadsToFilters(cacheQfpc);
//        System.out.println("Index: " + cacheIndex);
//
//        // Features are objects that describe view
//        // A query needs to cover all features of view
//        // so it must hold that |featuresOf(query)| >= |featuresOf(cache)|
//        Set<Object> features = new HashSet<Object>();
//        cacheQuadIndex.asMap().values().stream().flatMap(cnfs -> cnfs.stream())
//            .flatMap(cnf -> cnf.stream())
//            .filter(clause -> clause.size() == 1)
//            .flatMap(clause -> clause.stream())
//            .forEach(feature -> features.add(feature));
//
//
//        FeatureMap<Object, Object> featuresToCache = new FeatureMapImpl<>();
//        featuresToCache.put(features, cacheQfpc);
//
//
//        // The problem graph
//        //Graph<Problem<Map<Var, Var>>, DefaultEdge> problemGraph = new SimpleGraph<>(DefaultEdge.class);
//
//
//
//
//
//        // Probably cache entries should be indexed using DNFs and the table system,
//        // whereas lookups could be made using CNFs
//
//        Collection<Entry<Set<Object>, Object>> candidates = featuresToCache.getIfSubsetOf(
//                new HashSet<>(Arrays.asList(
//                        ExprUtils.parse("?o = <my://Airport>"),
//                        ExprUtils.parse("?p = <my://type>")
//              )));
//
//        System.out.println("Candidates: " + candidates);
//
//
//        problems.forEach(p -> System.out.println("SOLUTIONS for " + p + " " + p.generateSolutions().collect(Collectors.toList())));
//}

        //ProblemContainerImpl<Map<Var, Var>> container = ProblemContainerImpl.create(problems);
        //container.

//        ContainmentMap<Set<Expr>, Quad> clauseToQuads = new ContainmentMapImpl<>();
//        for(Entry<Quad, Set<Set<Expr>>> entry : index.entries()) {
//            clauseToQuads.put(entry.getValue(), entry.getKey());
//        }
//
        // given a query, we need to conver at least all constraints of the cache
        //clauseToQuads.getAllEntriesThatAreSubsetOf(prototye)


//        SparqlViewCacheImpl x;
//        x.lookup(queryQfpc)

        //QuadFilterPatternCanonical


//        Expr a = ExprUtils.parse("(?z = ?x + 1)");
//        Expr b = ExprUtils.parse("?a = ?b || (?c = ?a + 1) && (?k = ?i + 1)");
        //Expr b = ExprUtils.parse("?x = ?y || (?z = ?x + 1)");
//
//        Set<Set<Expr>> ac = CnfUtils.toSetCnf(b);
//        Set<Set<Expr>> bc = CnfUtils.toSetCnf(a);

//        Problem<Map<Var, Var>> p = new ProblemVarMappingExpr(ac, bc, Collections.emptyMap());
//
//        System.out.println("p");
//        System.out.println(p.getEstimatedCost());
//        ProblemVarMappingExpr.createVarMap(a, b).forEach(x -> System.out.println(x));

//        Collection<Quad> as = Arrays.asList(new Quad(Vars.g, Vars.s, Vars.p, Vars.o));
//        Collection<Quad> bs = Arrays.asList(new Quad(Vars.l, Vars.x, Vars.y, Vars.z));
//
//
//        //Collection<Quad> cq =
//        System.out.println("q");
//        Problem<Map<Var, Var>> q = new ProblemVarMappingQuad(as, bs, Collections.emptyMap());
//        System.out.println(q.getEstimatedCost());
//
//        q.generateSolutions().forEach(x -> System.out.println(x));


        //Maps.com

//        System.out.println("pc");
//        ProblemContainerImpl<Map<Var, Var>> pc = ProblemContainerImpl.create(p, q);
//        StateProblemContainer<Map<Var, Var>> state = new StateProblemContainer<>(Collections.emptyMap(), pc, SparqlCacheUtils::mergeCompatible);
        //SearchUtils.depthFirstSearch(state, isFinal, vertexToResult, vertexToEdges, edgeCostComparator, edgeToTargetVertex, depth, maxDepth)
//        StateSearchUtils.depthFirstSearch(state, 10).forEach(x -> System.out.println(x));


        // Next level: Matching Ops


        // Problem: We can now find whether there exist variable mappings between two expressions or sets of quads
        // But the next step is to determine which exact parts of the query can be substituted
        // The thing is: We need to compute the variable mapping, but once we have obtained it,
        // we could use the state configuration that led to the solution to efficiently determine
        // the appropriate substitutions



        //p.generateSolutions().forEach(x -> System.out.println(x));
//    }


//    public static ContainmentMap<Int, Multimap<Expr, Expr>> indexDnf(QuadFilterPatternCanonical qfpc) {
//        IBiSetMultimap<Quad, Set<Set<Expr>>> index = SparqlCacheUtils.createMapQuadsToFilters(qfpc);
//
//        IBiSetMultimap<Set<Set<Expr>>, Quad> map = index.getInverse();
//
//        ContainmentMap<Expr, Multimap<Expr, Expr>> result = new ContainmentMapImpl<>();
//        for(Set<Expr> clause : dnf) {
//            Multimap<Expr, Expr> exprSigToExpr = HashMultimap.create();
//            Set<Expr> clauseSig = new HashSet<>();
//            for(Expr expr : clause) {
//                Expr exprSig = org.aksw.jena_sparql_api.utils.ExprUtils.signaturize(expr);
//                exprSigToExpr.put(exprSig, expr);
//                clauseSig.add(exprSig);
//            }
//
//            //Set<Expr> clauseSig = ClauseUtils.signaturize(clause);
//            result.put(clauseSig, exprSigToExpr);
//        }
//
//        return result;
//    }

//}
