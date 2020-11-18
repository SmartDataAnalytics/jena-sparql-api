package org.aksw.jena_sparql_api.query_containment.index;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.combinatorics.algos.KPermutationsOfNUtils;
import org.aksw.combinatorics.solvers.GenericProblem;
import org.aksw.combinatorics.solvers.collections.ProblemSolver2;
import org.aksw.combinatorics.solvers.collections.Solution;
import org.aksw.commons.collections.multimaps.MultimapUtils;
import org.aksw.commons.collections.trees.Tree;
import org.aksw.commons.collections.trees.TreeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codepoetics.protonpack.StreamUtils;
import com.codepoetics.protonpack.functions.TriFunction;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.common.collect.Tables;

/**
 *
 *
 *
 * TODO Depth-First iteration vs Per Layer iteration (bottom-up breadth first)
 *
 * @author raven
 *
 * @param <K>
 * @param <G>
 * @param <N>
 * @param <A>
 * @param <B>
 * @param <C>
 * @param <L> leaf matching type - may differ from the node matching type M
 * @param <V>
 */
public class TreeMapper<K, TCA, TCB, LCA, LCB, A, B, L, V, C, R, TM extends TreeMapping<A, B, V, R>> {

    private static final Logger logger = LoggerFactory.getLogger(TreeMapper.class);

    // Two functions: One maps a key to some object XA, the other extracts the tree from it
    protected Function<? super K, ? extends TCA> viewKeyToViewContext;
    protected Function<? super TCA, ? extends Tree<A>> viewContextToTree;


    //protected Function<?>

    protected Function<? super TCB, ? extends Tree<B>> getTree;
    protected Function<? super TCB, Map<B, LCB>> getLeafNodes;

    // A function that takes the tree context B, a leaf of B together with its leaf context, a base matching
    // and which is expected to yield candidate leaf matches of A together with mappings of type L
    // Note that (possibly lossy) conversions between L  and M are intended -
    // e.g. converting a graph isomorphism mapping L to a containment mapping M
    protected TriFunction<TCB, Entry<B, LCB>, V, Table<K, A, ? extends GenericProblem<L, ?>>> leafMatcher;


    //protected TriFunction<? super A, ? super B, TreeMapping<A, B, M, V>, ? extends Entry<C, V>> nodeMapper;
    //protected BiFunction<? super XA, ?super XB, ? extends NodeMapper<A, B, M, V>> nodeMapperFactory;
    protected TriFunction<? super TCA, ? super TCB, ? super Table<A, B, L>, ? extends NodeMapper<A, B, V, C, R>> nodeMapperFactory;


    // Transformation function to obtain a 'node-level' mapping from the lower 'leaf-level' mapping
    protected Function<? super L, ? extends V> leafToNodeMatching;
    protected Function<? super V, ? extends L> nodeToLeafMatching;

    //protected BinaryOperator<L> leafMatchingCombiner;


    protected BiFunction<V, C, V> addMatchingContribution;
    protected BinaryOperator<V> matchingCombiner;
    protected Predicate<V> isMatchingUnsatisfiable;

    protected TreeMappingFactory<A, B, V, R, ? extends TM> treeMappingFactory;

    // Whether nodes of a tree are to be compared by .equals or ==
    protected boolean aIdentity;
    protected boolean bIdentity;

    public TreeMapper(
            Function<? super K, ? extends TCA> viewKeyToXA,
            Function<? super TCA, ? extends Tree<A>> xaToTree,

            Function<? super TCB, ? extends Tree<B>> getTree,
            Function<? super TCB, Map<B, LCB>> getLeafNodes,

            TriFunction<TCB, Entry<B, LCB>, V, Table<K, A, ? extends GenericProblem<L, ?>>> leafMatcher,
            //TriFunction<? super A, ? super B, TreeMapping<A, B, M, V>, ? extends Entry<C, V>> nodeMapper,
            //BiFunction<? super XA, ?super XB, ? extends NodeMapper<A, B, M, V>> nodeMapperFactory,
            TriFunction<? super TCA, ? super TCB, ? super Table<A, B, L>, ? extends NodeMapper<A, B, V, C, R>> nodeMapperFactory,

            Function<? super L, ? extends V> matchingTransformer,


            BiFunction<V, C, V> addMatchingContribution,
            //Function<Tree<A>, Stream<A>> bottomUpTraverser,
            //Supplier<M> createEmptyMatching,
            BinaryOperator<V> matchingCombiner,
            Predicate<V> isMatchingUnsatisfiable,

            TreeMappingFactory<A, B, V, R, ? extends TM> treeMappingFactory,

            boolean aIdentity,
            boolean bIdentity) {
        super();
        this.viewKeyToViewContext = viewKeyToXA;
        this.viewContextToTree = xaToTree;

        this.getTree = getTree;
        this.getLeafNodes = getLeafNodes;


        this.leafMatcher = leafMatcher;
        this.nodeMapperFactory = nodeMapperFactory;

        this.leafToNodeMatching = matchingTransformer;

        this.addMatchingContribution = addMatchingContribution;
        //this.bottomUpTraverser = bottomUpTraverser;
        //this.createEmptyMatching = createEmptyMatching;
        this.matchingCombiner = matchingCombiner;
        this.isMatchingUnsatisfiable = isMatchingUnsatisfiable;

        this.treeMappingFactory = treeMappingFactory;

        this.aIdentity = aIdentity;
        this.bIdentity = bIdentity;
    }



    public static <R, C, V> Table<R, C, V> createTable(boolean rowIdentity, boolean columnIdentity) {
        Map<R, Map<C, V>> backingMap = createMap(rowIdentity);

        Supplier<Map<C, V>> supplier = columnIdentity
                ? IdentityHashMap::new
                : LinkedHashMap::new;

        Table<R, C, V> result = Tables.newCustomTable(backingMap, supplier::get);
        return result;
    }


    public static <K, V> Map<K, V> createMap(boolean useIdentity) {
        Map<K, V> result = useIdentity
                ? new IdentityHashMap<>()
                : new LinkedHashMap<>();

        return result;
    }

    public Stream<Entry<K, TM>> createMappings(V baseMatching, TCB queryContext) {

        //Table<K, Entry<A, B>, Table<A, B, ProblemNeighborhoodAware<S, S>>> leafMappings = createLeafMappings(userTree);
        Map<K, Table<A, B, GenericProblem<L, ?>>> leafMappingPerView = createLeafMappings(baseMatching, queryContext);

        Tree<B> queryTree = getTree.apply(queryContext);

        Stream<Entry<K, TM>> result = leafMappingPerView.entrySet().stream()
            .flatMap(e -> {
                K viewKey = e.getKey();
                //logger.debug("Processing view with key: " + viewKey);

                TCA viewContext = viewKeyToViewContext.apply(viewKey);
                Tree<A> viewTree = viewContextToTree.apply(viewContext);//viewKeyToTree.apply(viewKey);

                //XB xb = null;
                //NodeMapper<A, B, M, V>


                Table<A, B, ? extends GenericProblem<L, ?>> alignmentProblems = e.getValue();
                Multimap<A, B> mm = MultimapUtils.newSetMultimap(aIdentity, bIdentity);

                for(Cell<A, B, ? extends GenericProblem<L, ?>> cell : alignmentProblems.cellSet()) {
                    mm.put(cell.getRowKey(), cell.getColumnKey());
                }


                Multimap<A, A> viewAncestorToChildren = MultimapUtils.groupBy(mm.keys(), (node) -> TreeUtils.getFirstMultiaryAncestor(viewTree, node), MultimapUtils.newSetMultimap(aIdentity, aIdentity));
                //Multimap<B, B> userMultiaryAncestorToChildren = MultimapUtils.groupBy(mm.values(), (node) -> TreeUtils.getFirstMultiaryAncestor(userTree, node), MultimapUtils.newSetMultimap(bIdentity, bIdentity));

                Multimap<A, B> ancestorCandAlignment = MultimapUtils.newSetMultimap(aIdentity, bIdentity);
                for(Entry<A, A> f : viewAncestorToChildren.entries()) {
                    A aAncestor = f.getKey();
                    A aChild = f.getValue();

                    Collection<B> bChildren = alignmentProblems.row(aChild).keySet();
                    for(B b : bChildren) {
                        B bAncestor = TreeUtils.getFirstMultiaryAncestor(queryTree, b);
                        ancestorCandAlignment.put(aAncestor, bAncestor);
                    }
                }


//                for(Entry<A, Collection<B>> x : mm.asMap().entrySet()) {
//                    A a = x.getKey();
//                    A aAncestor = TreeUtils.getFirstMultiaryAncestor(viewTree, a); //viewTree.getParent(a);
//                    for(B b : x.getValue()) {
//                        B bAncestor = TreeUtils.getFirstMultiaryAncestor(userTree, b);
//                        multiaryAncestorCandAlignment.put(aAncestor, bAncestor);
//                    }
//                }

                Stream<Map<A, B>> childAlignmentStream = KPermutationsOfNUtils.kPermutationsOfN(ancestorCandAlignment, aIdentity, bIdentity).flatMap(parentAlignment -> {
                    // For each parent alignment, create the kPermutationsOfN for the children
                    Multimap<A, B> childCandAlignment = MultimapUtils.newSetMultimap(aIdentity, bIdentity);
                    for(Entry<A, B> f : parentAlignment.entrySet()) {
                        A aMultiaryAncestor = f.getKey();

                        Collection<A> aChildren = viewAncestorToChildren.get(aMultiaryAncestor);

                        for(A aChild : aChildren) {
                            Set<B> bChildren = alignmentProblems.row(aChild).keySet();
                            for(B bChild : bChildren) {
                                B bAncestor = TreeUtils.getFirstMultiaryAncestor(queryTree, bChild);
                                boolean isMatch = ancestorCandAlignment.containsEntry(aMultiaryAncestor, bAncestor);
                                if(isMatch) {
                                    childCandAlignment.put(aChild, bChild);
                                }
                            }
                        }

//                        B bParent = f.getValue();
//                        for(A aChild : viewMultiaryAncestorToChildren.get(aMultiaryAncestor)) {
//                            Collection<B> bChildren = userMultiaryAncestorToChildren.get(bParent);
//                            childCandAlignment.putAll(aChild, bChildren);
//                        }

                    }

                    // TODO Based on the parent alignment, we could use different strategies to match the children
                    // e.g. sequential


                    Stream<Map<A, B>> t = KPermutationsOfNUtils.kPermutationsOfN(childCandAlignment, aIdentity, bIdentity);

                    return t;
                });




                // TODO Filter out mappings of view nodes with same parent to user nodes with different parents
                //Stream<Map<A, B>> xxx = KPermutationsOfNUtils.kPermutationsOfN(mm, this::createMap);


                Stream<TM> r = childAlignmentStream.flatMap(leafAlignment -> {
                    // TODO On the one hand, we need to compute an overall matching from all leaf alignments
                    // on the other hand, we want to pass each raw (= the graph isomorphism) matching contribution
                    // to the node mapper

                    // But this means, that we need separate types for problem solutions
                    // (pairs of the original solution and the effective one)
                    // and refinements (uses only the effective one)


                    // Create a list of all problems for passing them to the solver
                    // Note, that we need to zip this list with the (viewOp, queryOp) leafAlignment entries
                    List<? extends Collection<? extends GenericProblem<L, ?>>> problems = leafAlignment.entrySet().stream()
                            .map(f -> alignmentProblems.get(f.getKey(), f.getValue()))
                            .map(problem -> Collections.singleton(problem))
                            //.map(problem -> problem.map(matchingTransformer))
                            .collect(Collectors.toList());

                    Stream<Solution<V, L>> solutions = ProblemSolver2.solve(
                            (L)null,
                            problems,
                            (BinaryOperator<L>)null, // TODO - right now we don't expect that we need to combine solutions from leaf mappings / contributionCombiner,
                            leafToNodeMatching,
                            matchingCombiner,
                            nodeToLeafMatching
                            );




                    //M baseMatching = createEmptyMatching.get();
//System.out.println("Got child alignment");
                    // When obtaining the overall matching for leafs, we apply the matchingCleaner
                    //ProblemContainer<M> problemContainer = ProblemContainerImpl.create(problems);
                    //ProblemSolver<M> solver = new ProblemSolver<>(problemContainer, baseMatching, matchingCombiner);

                    return solutions.map(solution -> {
                        // For each alignment and matching perform the tree mapping

                        // TODO probable we should create an instance of the tree mapper for each solution
                        List<L> contribs = solution.getContributions();
                        Table<A, B, L> table = createTable(aIdentity, bIdentity);

                        StreamUtils.zipWithIndex(leafAlignment.entrySet().stream()).forEach(entry -> {
                            int index = (int)entry.getIndex();
                            L contrib = contribs.get(index);

                            A a = entry.getValue().getKey();
                            B b = entry.getValue().getValue();

                            table.put(a, b, contrib);
                        });


                        V matching = solution.getSolution();

                        // TODO Revise which arguments of the treeMapper should go to the ctor and which are part of the solve method
                        // TODO Maybe turn the whole treemapper into a static function?
                        NodeMapper<A, B, V, C, R> nodeMapper = nodeMapperFactory.apply(viewContext, queryContext, table);

                        BottomUpTreeMapper<A, B, V, C, R, TM> treeMapper = new BottomUpTreeMapper<A, B, V, C, R, TM>(
                                viewTree,
                                queryTree,
                                nodeMapper,
                                addMatchingContribution, isMatchingUnsatisfiable,
                                () -> createTable(aIdentity, bIdentity),
                                treeMappingFactory
//                                BottomUpTreeTraversals::postOrder//bottomUpTraverser
                                );

                        TM s = treeMapper.solve(matching, leafAlignment);

                        return s;
                    });
                });

                Stream<Entry<K, TM>> t = r
                        .filter(item -> item != null)
                        .map(item -> new SimpleEntry<>(viewKey, item));
                return t;
            });

        return result;
    }



    // for each view key there may be multiple tree alignments
    // The concrete mapping: Multimap<K, Entry<S, Table<A, B, Entry<S, C>>>>
    //                                         ^ overall mapping so far


    /**
     *
     */
    public Map<K, Table<A, B, GenericProblem<L, ?>>> createLeafMappings(V baseMatching, TCB userContext) { //Tree<B> userTree) {

        //Collection<B> leafNodes = TreeUtils.getLeafs(userTree);
        Map<B, LCB> leafMap = getLeafNodes.apply(userContext);

        Map<K, Table<A, B, GenericProblem<L, ?>>> result = createLeafMappings(baseMatching, userContext, leafMap.entrySet());

        return result;
    }

    public Map<K, Table<A, B, GenericProblem<L, ?>>> createLeafMappings(V baseMatching, TCB userContext, Collection<Entry<B, LCB>> leafEntries) {
        //for(B userOp : leafNodes) {
        Map<K, Table<A, B, GenericProblem<L, ?>>> result = new HashMap<>();
        for(Entry<B, LCB> leafEntry : leafEntries) {

            //Tree<B> userTree = getTree.apply(userContext);
            B userOp = leafEntry.getKey();
            //YB leafData = leafEntry.getValue();

            // Create the initial matching for the leafs
            // Obtain the candidate views for that user node
            Table<K, A, ? extends GenericProblem<L, ?>> matchTable = leafMatcher.apply(userContext, leafEntry, baseMatching);

            logger.debug("Found " + matchTable.rowMap().size() + " candidate leafs  for" + userOp);

            for(Entry<K, ? extends Map<A, ? extends GenericProblem<L, ?>>> matchEntry : matchTable.rowMap().entrySet()) {

                K viewKey = matchEntry.getKey();

                Table<A, B, GenericProblem<L, ?>> table = result.computeIfAbsent(viewKey,
                        (k) -> TreeMapper.createTable(aIdentity, bIdentity));

                for(Entry<A, ? extends GenericProblem<L, ?>> leafMatching : matchEntry.getValue().entrySet()) {
                    A viewOp = leafMatching.getKey();
                    GenericProblem<L, ?> matching = leafMatching.getValue();

                    table.put(viewOp, userOp, matching);
                }
            }
        }

        return result;
    }

}
