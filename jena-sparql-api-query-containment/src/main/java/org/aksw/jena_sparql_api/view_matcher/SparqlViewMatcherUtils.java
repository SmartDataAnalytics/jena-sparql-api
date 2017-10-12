package org.aksw.jena_sparql_api.view_matcher;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.aksw.combinatorics.algos.KPermutationsOfNUtils;
import org.aksw.combinatorics.solvers.ProblemNeighborhoodAware;
import org.aksw.combinatorics.solvers.ProblemStaticSolutions;
import org.aksw.commons.collections.CartesianProduct;
import org.aksw.commons.collections.FeatureMap;
import org.aksw.commons.collections.stacks.NestedStack;
import org.aksw.commons.collections.trees.Tree;
import org.aksw.commons.collections.trees.TreeUtils;
import org.aksw.commons.collections.utils.StreamUtils;
import org.aksw.commons.graph.index.jena.transform.OpDistinctExtendFilter;
import org.aksw.jena_sparql_api.algebra.utils.AlgebraUtils;
import org.aksw.jena_sparql_api.algebra.utils.ConjunctiveQuery;
import org.aksw.jena_sparql_api.algebra.utils.OpExtConjunctiveQuery;
import org.aksw.jena_sparql_api.algebra.utils.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.concept_cache.combinatorics.ProblemVarMappingExpr;
import org.aksw.jena_sparql_api.sparql.algebra.mapping.LayerMapping;
import org.aksw.jena_sparql_api.sparql.algebra.mapping.MatchingStrategyFactory;
import org.aksw.jena_sparql_api.sparql.algebra.mapping.SequentialMatchIterator;
import org.aksw.jena_sparql_api.sparql.algebra.mapping.TreeMapperImpl;
import org.aksw.jena_sparql_api.sparql.algebra.mapping.VarMapper;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpOrder;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;

public class SparqlViewMatcherUtils {

    private static final Logger logger = LoggerFactory.getLogger(SparqlViewMatcherUtils.class);

    public static Stream<OpVarMap> generateTreeVarMapping(
            Multimap<Op, Op> candOpMapping,
            Tree<Op> cacheTree,
            Tree<Op> queryTree)
    {

        Tree<Op> cacheMultiaryTree = TreeUtils.removeUnaryNodes(cacheTree);
        Tree<Op> queryMultiaryTree = TreeUtils.removeUnaryNodes(queryTree);

        Stream<OpVarMap> mappingSolutions = SparqlViewMatcherUtils.generateTreeVarMapping(
                candOpMapping,
                cacheTree, queryTree,
                cacheMultiaryTree, queryMultiaryTree);

        return mappingSolutions;
    }

    public static Stream<OpProblemVarMap> processStack(
            NestedStack<LayerMapping<Op, Op, Iterable<Map<Op, Op>>>> stack,
            Tree<Op> cacheTree, Tree<Op> queryTree,
            Tree<Op> cacheMultiaryTree, Tree<Op> queryMultiaryTree
    ) {
        PredicateFail<Object> pred = new PredicateFail<>(x -> x != null);

        // Create the iterators for the node mappings
        // TODO Exit early if any iterable being collected is null
        List<Iterable<Map<Op, Op>>> tmpChildNodeMappingCandidates = stack.stream()
            .flatMap(layerMapping -> layerMapping.getNodeMappings().stream()
                .map(nodeMapping -> nodeMapping.getValue()))
                //.takeWhile(pred) // TODO Re-enable once java 9 works
                .peek(x -> pred.test(x))
                .filter(x -> !pred.isFailed())
                .collect(Collectors.toList());

        boolean skip = pred.isFailed();

        List<Iterable<Map<Op, Op>>> childNodeMappingCandidates = skip
                ? Collections.emptyList()
                : tmpChildNodeMappingCandidates;

        CartesianProduct<Map<Op, Op>> cartX = CartesianProduct.create(childNodeMappingCandidates);

        Stream<Map<Op, Op>> completeNodeMapStream = cartX.stream()
            .map(listOfMaps -> {

                // Reset the predicate
                pred.setFailed(false);

                Map<Op, Op> completeNodeMap = listOfMaps.stream()
                    .flatMap(map -> {
                        // The entry set here corresponds to mappings of nodes in the multiary tree

                        Set<Entry<Op, Op>> entrySet = map.entrySet();

                        // For each entry, add the mapping of the unary ancestors
                        Stream<Entry<Op, Op>> augmented = entrySet.stream().flatMap(e -> {
                            Op cacheOp = e.getKey();
                            Op queryOp = e.getValue();

                          // TODO If any of the unary mappings is unsatisfiable, the completeNodeMap is unsatisfiable
                          Stream<Entry<Op, Op>> unaryMappingStream = SparqlViewMatcherUtils.augmentUnaryMappings2(
                                  cacheOp, queryOp,
                                  cacheTree, queryTree,
                                  cacheMultiaryTree, queryMultiaryTree);

                          //unaryMappingStream = Stream.empty();
                          //unaryMappingStream.isEmpty();

                            Stream<Entry<Op, Op>> s = Stream.concat(
                                Stream.of(e),
                                unaryMappingStream);

                            return s;
                        });

                            //Stream
                        return augmented;
                     })
                    //.takeWhile(pred) // TODO Re-enable once java 9 works
                    .peek(x -> pred.test(x))
                    .filter(x -> !pred.isFailed())
                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

                if(pred.isFailed()) {
                    completeNodeMap = null;
                }


                return completeNodeMap;
            })
            //.peek(item -> System.out.println("peek: "+ item))
            .filter(item -> item != null);

        // Next step: Now that we have a node mapping on the multiary tree,
        // we need to add the node mappings of the unary ops unary ops


        Function<Map<Op, Op>, List<ProblemNeighborhoodAware<Map<Var, Var>, Var>>> mapToProblems = (nodeMap) ->
                nodeMap.entrySet().stream()
                .flatMap(e -> SparqlViewMatcherUtils.createProblems(e.getKey(), e.getValue()).stream())
                .collect(Collectors.toList());


        // For each complete node mapping, associate the possible variable mappings
        Stream<OpProblemVarMap> nodeMappingProblemStream = completeNodeMapStream.map(completeNodeMap -> {
            List<ProblemNeighborhoodAware<Map<Var, Var>, Var>> ps = mapToProblems.apply(completeNodeMap);

            if(logger.isDebugEnabled()) {
                for(ProblemNeighborhoodAware<Map<Var, Var>, Var> p : ps) {
                    logger.debug("Solving problem: " + p);
                }
            }

            OpProblemVarMap r = new OpProblemVarMap(completeNodeMap, ps);
            return r;
        });

        return nodeMappingProblemStream;
    }


    public static Stream<OpVarMap> generateTreeVarMapping(
            Multimap<Op, Op> candOpMapping,
            Tree<Op> cacheTree, Tree<Op> queryTree,
            Tree<Op> cacheMultiaryTree, Tree<Op> queryMultiaryTree)
    {
        // The tree mapper only determines sets of candidate mappings for each tree level
        TreeMapperImpl<Op, Op, Iterable<Map<Op, Op>>> tm = new TreeMapperImpl<Op, Op, Iterable<Map<Op, Op>>>(
                cacheMultiaryTree,
                queryMultiaryTree,
                candOpMapping,
                (a, b) -> SparqlViewMatcherUtils.determineMatchingStrategy(cacheMultiaryTree, queryMultiaryTree, a, b),
                (iterable) -> iterable != null
                );

        // TODO Ultimately, we want
        // (x) a stream of node mappings of which each is associated with the possible var mappings
        //   Var mappings are obtained from problem instances
        // (x) However, instead of generating the full op-mappings, we could create the

        Stream<NestedStack<LayerMapping<Op, Op, Iterable<Map<Op, Op>>>>> mappingStream =
                StreamUtils.<Map<Op, Op>, NestedStack<LayerMapping<Op, Op, Iterable<Map<Op, Op>>>>>
                    stream(tm::recurse, Collections.emptyMap());

        // Turn each stack into stream of candidate node mappings together with the problems to solve
        Stream<OpProblemVarMap> nodeMappingToProblems =
            mappingStream.flatMap(stack -> {
                return processStack(stack, cacheTree, queryTree, cacheMultiaryTree, queryMultiaryTree);
            });


        // Solve the problems for each node mapping
        Stream<OpVarMap> result = nodeMappingToProblems.map(e -> {
            Map<Op, Op> nodeMapping = e.getNodeMapping();
            //Stream<Map<Var, Var>> varMappings = VarMapper.solve(e.getProblems());
            List<ProblemNeighborhoodAware<Map<Var, Var>, Var>> problems = e.getProblems();

            Iterable<Map<Var, Var>> it = () -> VarMapper.solve(problems).iterator();

            OpVarMap r = new OpVarMap(nodeMapping, it);
            return r;
        });

        return result;
    }

    /**
     * Notes:
     *
     * (x) Matching any cache op with a query sequence could introduce a dummy sequence node
     * on the cache side.
     *
     * This means, that insead of matching cacheOp with queryOp, we would generate a
     * dummy sequence node with parent parentOf(cacheOp) and child cacheOp.
     *
     * (x) Matching unions:
     * matching a cache DISJ(1, 2) with a query DISJ(a, b, c) with a model mapping of 1:a, 2:b could be rewritten as
     * DISJ(DISJ(a, b), c) - hence all children of DISJ(1, 2) would match with DISJ(a, b).
     *
     * (x) If the cacheOp is a root node, partial matches are usually acceptable -
     *     e.g. matching a cache sequence of size 2 and a query sequence of size 3
     *
     *
     * @param cacheOp
     * @param queryOp
     * @return
     */
    public static <A, B> MatchingStrategyFactory<A, B> determineMatchingStrategy(Tree<A> aTree, Tree<B> treeB, A cacheOp, B queryOp) {
        // TODO Aggregate collections of problem instances with the node mapping and
        // discard candidates which yield problem instances without solutions.

        Map<Class<?>, MatchingStrategyFactory<A, B>> opToMatcherTest = new HashMap<>();
        opToMatcherTest.put(OpDisjunction.class, (as, bs, mapping) -> KPermutationsOfNUtils.createIterable(mapping, false, false));
        opToMatcherTest.put(OpSequence.class, (as, bs, mapping) -> KPermutationsOfNUtils.createIterable(mapping, false, false));

        Function<Class<?>, MatchingStrategyFactory<A, B>> fnOpToMatchingStrategyFactory = (nodeType) ->
            opToMatcherTest.getOrDefault(nodeType, (as, bs, mapping) -> SequentialMatchIterator.createIterable(as, bs, mapping));

        MatchingStrategyFactory<A, B> result;

        int c = (cacheOp == null ? 0 : 1) | (queryOp == null ? 0 : 2);
        switch(c) {
        case 0: // both null - nothing to do because the candidate mapping of the children (each tree's root node) is already the final solution
            result = (as, bs, mapping) -> SequentialMatchIterator.createIterable(as, bs, mapping); // true
            break;
        case 1: // queryOp null - no match because the cache tree has greater depth than the query
            result = (as, bs, mapping) -> null;
            break;
        case 2: // cacheOp null - match because a cache tree's super root (i.e. null) matches any query node (including null)
            result = (as, bs, mapping) -> SequentialMatchIterator.createIterable(as, bs, mapping); // true
            break;
        case 3: // both non-null - by default, both ops must be of equal type - the type determines the matching enumeration strategy
            Class<?> ac = cacheOp.getClass();
            Class<?> bc = queryOp.getClass();

            if(ac.equals(bc)) {
                MatchingStrategyFactory<A, B> tmp = fnOpToMatchingStrategyFactory.apply(ac);
                // True if *all* of the two parents' children must have correspondences
                boolean requireCompleteCover = true;
                if(requireCompleteCover) {
                    //System.out.println("Rejecting incomplete cover");

                    result = (as, bs, mapping) -> (aTree.getParent(cacheOp) != null && as.size() != bs.size()
                            ? null//IterableUnknownSizeSimple.createEmpty()
                            : tmp.apply(as, bs, mapping));
                } else {
                    result = tmp;
                }

            } else {
                result = (as, bs, mapping) -> null; //IterableUnknownSizeSimple.createEmpty();
            }
            break;
        default:
            throw new IllegalStateException();
        }

        return result;
    }

    public static <A, B> Stream<Entry<A, B>> augmentUnaryMappings2(
            A sourceNode,
            B targetNode,
            Tree<A> sourceTree,
            Tree<B> targetTree,
            Tree<A> sourceMultiaryTree,
            Tree<B> targetMultiaryTree)
    {
        //Op cacheLeaf = cacheLeafs.get(1);
        List<A> sourceUnaryAncestors = TreeUtils.getUnaryAncestors(sourceNode, sourceTree, sourceMultiaryTree);

        //Op queryLeaf = queryLeafs.get(1);
        List<B> targetUnaryAncestors = TreeUtils.getUnaryAncestors(targetNode, targetTree, targetMultiaryTree);

        int n = sourceUnaryAncestors.size();
        int m = targetUnaryAncestors.size();

        // If the list of sourceUnaryAncestors includes the source's root node,
        // we do not require all of the ancestors to match
        if(n > 0 && Iterables.getLast(sourceUnaryAncestors) == sourceTree.getRoot()) {
            m = Math.min(m, n);
        }


        boolean sameSize = n == m;


        //List<Entry<Map<T, T>, Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>>>> result = new ArrayList<>(n);

        Stream<Entry<A, B>> result;
        if(sameSize) {
            result = IntStream.range(0, n)
                .mapToObj(i -> {
                    A sourceAncestor = sourceUnaryAncestors.get(i);
                    B targetAncestor = targetUnaryAncestors.get(i);

                    Entry<A, B> r = new SimpleEntry<A, B>(sourceAncestor, targetAncestor);
                    return r;
                });
        } else if (n == 0) {
            // If there are no ancestors in the source tree, the mapping is satisfiable

            result = Stream.empty();
        } else {
            result = Stream.of((Entry<A, B>)null);//Stream.empty();
        }

        return result;
    }

    public static <A, B> Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> createProblems(A a, B b) {


            Map<Class<?>, GenericBinaryOp<Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>>>> map = new HashMap<>();
            map.put(OpProject.class, GenericBinaryOpImpl.create(SparqlViewMatcherUtils::deriveProblemsProject));
            map.put(OpSequence.class, (x, y) -> Collections.emptySet()); //GenericBinaryOpImpl.create(TestStateSpaceSearch::deriveProblemsSequence));
            map.put(OpDisjunction.class, (x, y) -> Collections.emptySet()); //GenericBinaryOpImpl.create(TestStateSpaceSearch::deriveProblemsDisjunction));
            map.put(OpDistinct.class, (x, y) -> Collections.emptySet());
            map.put(OpSlice.class, GenericBinaryOpImpl.create(SparqlViewMatcherUtils::deriveProblemsSlice));
            map.put(OpExtend.class, GenericBinaryOpImpl.create(SparqlViewMatcherUtils::deriveProblemsExtend));

            map.put(OpExtConjunctiveQuery.class, GenericBinaryOpImpl.create(SparqlViewMatcherUtils::deriveProblemsOpCq));
            map.put(OpDistinctExtendFilter.class, (x, y) -> Collections.emptySet());

            map.put(OpLeftJoin.class, (x, y) -> Collections.emptySet());

            map.put(OpFilter.class, GenericBinaryOpImpl.create(SparqlViewMatcherUtils::deriveProblemsFilter));

            map.put(OpOrder.class, GenericBinaryOpImpl.create(SparqlViewMatcherUtils::deriveProblemsOrder));

            Class<?> ac = a.getClass();
            Class<?> bc = b.getClass();

            Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> result;

            if(ac.equals(bc)) {
                GenericBinaryOp<Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>>> problemFactory = map.get(ac);
                if(problemFactory == null) {
                    throw new RuntimeException("No factory found for type: " + ac);
                }

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

    public static Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> deriveProblemsSlice(OpSlice cacheOp, OpSlice userOp) {
        Range<Long> cacheRange = QueryUtils.toRange(cacheOp);
        Range<Long> queryRange = QueryUtils.toRange(userOp);

        Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> result = cacheRange.equals(queryRange)
                ? Collections.emptySet()
                : Collections.singleton(new ProblemStaticSolutions<>(Collections.singleton(null)));

        return result;
    }

    // TODO We should use something like E_Assign instead of equals, as the latter is symmetric.
    public static Expr varExprListToExpr(Var v, Expr e) {
        return new E_Equals(new ExprVar(v), e);
    }

    public static Set<Set<Expr>> toDnf(VarExprList vel) {
        Set<Set<Expr>> result = vel.getExprs().entrySet().stream()
                .map(e -> Collections.singleton(varExprListToExpr(e.getKey(), e.getValue())))
                .collect(Collectors.toSet());

//    	Expr tmp = ExprUtils.andifyBalanced(exprs);
//
//    	Set<Set<Expr>> result = DnfUtils.toSetDnf(tmp);
        return result;
    }

    public static Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> deriveProblemsExtend(OpExtend cacheOp, OpExtend userOp) {
        Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> result = new ArrayList<>();

        FeatureMap<Expr, Multimap<Expr, Expr>> cacheIndex = AlgebraUtils.indexDnf(toDnf(cacheOp.getVarExprList()));
        FeatureMap<Expr, Multimap<Expr, Expr>> queryIndex = AlgebraUtils.indexDnf(toDnf(userOp.getVarExprList()));

        VarMapper.createProblems(cacheIndex, queryIndex).forEach(result::add);

        return result;
    }

    public static Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> deriveProblemsProject(OpProject cacheOp, OpProject userOp) {
        ProblemNeighborhoodAware<Map<Var, Var>, Var> tmp = deriveProblem(cacheOp.getVars(), userOp.getVars());
        Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> result = Collections.singleton(tmp);
        return result;
    }

    public static ProblemNeighborhoodAware<Map<Var, Var>, Var> deriveProblem(List<Var> cacheVars, List<Var> userVars) {
        List<Expr> aExprs = cacheVars.stream().map(v -> new ExprVar(v)).collect(Collectors.toList());
        List<Expr> bExprs = userVars.stream().map(v -> new ExprVar(v)).collect(Collectors.toList());
        ProblemNeighborhoodAware<Map<Var, Var>, Var> result = new ProblemVarMappingExpr(aExprs, bExprs, Collections.emptyMap());
        return result;
    }

    public static Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> deriveProblemsFilter(OpFilter cacheOp, OpFilter userOp) {
        ProblemNeighborhoodAware<Map<Var, Var>, Var> tmp = new ProblemVarMappingExpr(cacheOp.getExprs().getList(), userOp.getExprs().getList(), Collections.emptyMap());
        Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> result = Collections.singleton(tmp);
        return result;
    }

    public static Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> deriveProblemsOrder(OpOrder cacheOp, OpOrder userOp) {
        //ProblemNeighborhoodAware<Map<Var, Var>, Var> tmp = new ProblemVarMappingExpr(cacheOp.getExprs().getList(), userOp.getExprs().getList(), Collections.emptyMap());
        //Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> result = Collections.singleton(tmp);
        //return result;
        logger.warn("OpOrder support not implemented");
        return Collections.emptySet();
    }


    public static Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> deriveProblemsSequence(OpSequence cacheOp, OpSequence userOp) {
        return Collections.emptySet();
        //ProblemNeighborhoodAware<Map<Var, Var>, Var> tmp = deriveProblem(cacheOp.getVars(), userOp.getVars());
        //Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> result = Collections.singleton(tmp);
        //return result;
    }

    public static Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> deriveProblemsDisjunction(OpDisjunction cacheOp, OpDisjunction userOp) {
        return Collections.emptySet();
        //ProblemNeighborhoodAware<Map<Var, Var>, Var> tmp = deriveProblem(cacheOp.getVars(), userOp.getVars());
        //Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> result = Collections.singleton(tmp);
        //return result;
    }

    public static List<Integer> setOfSizes(Collection<? extends Collection<?>> nestedCollection) {
        List<Integer> result = new ArrayList<>(nestedCollection.size());
        for(Collection<?> item : nestedCollection) {
            result.add(item.size());
        }

        Collections.sort(result);

        return result;
    }

    public static Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> deriveProblemsOpCq(OpExtConjunctiveQuery cacheOp, OpExtConjunctiveQuery userOp) {
        ConjunctiveQuery viewCq = cacheOp.getQfpc();
        ConjunctiveQuery userCq = userOp.getQfpc();

        Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> result =
                deriveProblemsCq(viewCq, userCq);

        // TODO Validate projection

        return result;
    }

    public static Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> deriveProblemsCq(ConjunctiveQuery viewCq, ConjunctiveQuery userCq) {
        QuadFilterPatternCanonical viewQfpc = viewCq.getPattern();
        QuadFilterPatternCanonical userQfpc = userCq.getPattern();

        Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> result =
                deriveProblemsQfpc(viewQfpc, userQfpc);

        return result;
    }



    public static Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> deriveProblemsQfpc(QuadFilterPatternCanonical cacheQfpc, QuadFilterPatternCanonical queryQfpc) {

        if(logger.isDebugEnabled()) {
            logger.debug("Deriving problems for:");
            logger.debug("  " + cacheQfpc);
            logger.debug("  " + queryQfpc);
        }


        // Potential Hack: The cache qfpc and the query qfpc must be of equal size -
        // i.e. same number of quads and triples
        boolean isEqualSize = cacheQfpc.getQuads().size() == queryQfpc.getQuads().size()
                && setOfSizes(cacheQfpc.getFilterDnf()).equals(setOfSizes(queryQfpc.getFilterDnf()));

        Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> result = isEqualSize
                ? VarMapper.createProblems(cacheQfpc, queryQfpc)
                : Collections.singleton(new ProblemStaticSolutions<>(Collections.singleton(null)));

        if(logger.isDebugEnabled()) {
            for(ProblemNeighborhoodAware<Map<Var, Var>, Var> x : result) {
                long estimatedCost = x.getEstimatedCost();
                if(estimatedCost > 10000) {
                    logger.warn("High estimated cost detected!");
                }

                logger.debug("  Estimated cost: " + estimatedCost);
                logger.debug("  Actual size: " + x.generateSolutions().count());
            }
        }

        return result;
    }

}




//        List<Op> cacheLeafs = TreeUtils.getLeafs(cacheTree);
//        List<Op> queryLeafs = TreeUtils.getLeafs(queryTree);

//        System.out.println("Query Tree:\n" + queryTree);
//        System.out.println("Cache Tree:\n" + cacheTree);

//        System.out.println("root:" + tree.getRoot());
//        System.out.println("root:" + tree.getChildren(tree.getRoot()));

        //System.out.println("Multiary tree: " + cacheMultiaryTree);



//

//        Op cacheLeaf = cacheLeafs.get(1);
//        List<Op> cacheUnaryAncestors = getUnaryAncestors(cacheLeaf, cacheTree, cacheMultiaryTree);
//
//        Op queryLeaf = queryLeafs.get(1);
//        List<Op> queryUnaryAncestors = getUnaryAncestors(queryLeaf, queryTree, queryMultiaryTree);
//
////        System.out.println("unary parents: " + unaryParents);
//        Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> problems = createProblemsFromUnaryAncestors(cacheUnaryAncestors, queryUnaryAncestors);
//        Stream<Map<Var, Var>> solutions = VarMapper.solve(problems);
//        solutions.forEach(s -> System.out.println("found solution: " + s));


