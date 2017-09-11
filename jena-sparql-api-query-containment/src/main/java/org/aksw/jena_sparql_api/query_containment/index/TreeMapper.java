package org.aksw.jena_sparql_api.query_containment.index;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.combinatorics.algos.KPermutationsOfNUtils;
import org.aksw.combinatorics.solvers.ProblemNeighborhoodAware;
import org.aksw.combinatorics.solvers.collections.ProblemContainer;
import org.aksw.combinatorics.solvers.collections.ProblemContainerImpl;
import org.aksw.combinatorics.solvers.collections.ProblemSolver;
import org.aksw.commons.collections.multimaps.MultimapUtils;
import org.aksw.commons.collections.trees.Tree;
import org.aksw.commons.collections.trees.TreeUtils;
import org.apache.jena.sparql.algebra.Op;

import com.codepoetics.protonpack.functions.TriFunction;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.common.collect.Tables;

/**
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
 * @param <M>
 */
public class TreeMapper<K, A, B, M, C, V> {

    protected Function<? super K, ? extends Tree<A>> viewKeyToTree;
    protected TriFunction<Tree<B>, B, M, Table<K, A, ProblemNeighborhoodAware<M, ?>>> leafMatcher;

    protected TriFunction<A, B, TreeMapping<A, B, M, V>, Entry<C, V>> nodeMapper;

    protected BiFunction<M, C, M> addMatchingContribution;
    protected BinaryOperator<M> matchingCombiner;
    protected Predicate<M> isMatchingUnsatisfiable;
    //protected Function<Tree<B>, Stream<Set<B>>> bottomUpTreeTraversalFactory;

    //protected Supplier<Table<A, B, V>> tableSupplier;
    //protected Function<Tree<A>, Stream<A>> bottomUpTraverser;



    //protected Supplier<M> createEmptyMatching;


    // We could create task-sets from sub-trees

    boolean aIdentity;
    boolean bIdentity;

    public TreeMapper(Function<? super K, ? extends Tree<A>> viewKeyToTree,
            TriFunction<Tree<B>, B, M, Table<K, A, ProblemNeighborhoodAware<M, ?>>> leafMatcher,
            TriFunction<A, B, TreeMapping<A, B, M, V>, Entry<C, V>> nodeMapper,
            BiFunction<M, C, M> addMatchingContribution,
            //Function<Tree<A>, Stream<A>> bottomUpTraverser,
            //Supplier<M> createEmptyMatching,
            BinaryOperator<M> matchingCombiner,
            Predicate<M> isMatchingUnsatisfiable,
            boolean aIdentity,
            boolean bIdentity) {
        super();
        this.viewKeyToTree = viewKeyToTree;
        this.leafMatcher = leafMatcher;
        this.nodeMapper = nodeMapper;
        this.addMatchingContribution = addMatchingContribution;
        //this.bottomUpTraverser = bottomUpTraverser;
        //this.createEmptyMatching = createEmptyMatching;
        this.matchingCombiner = matchingCombiner;
        this.isMatchingUnsatisfiable = isMatchingUnsatisfiable;
        this.aIdentity = aIdentity;
        this.bIdentity = bIdentity;
    }



    public static <R, C, V> Table<R, C, V> createTable(boolean rowIdentity, boolean columnIdentity) {
        Map<R, Map<C, V>> backingMap = createMap(rowIdentity);

        Supplier<Map<C, V>> supplier = columnIdentity
                ? () -> new IdentityHashMap<>()
                : () -> new LinkedHashMap<>();

        Table<R, C, V> result = Tables.newCustomTable(backingMap, supplier::get);
        return result;
    }


    public <X, Y> Multimap<X, Y> createMultimap() {
        Multimap<X, Y> result = MultimapUtils.newSetMultimap(aIdentity, bIdentity);
        return result;
    }

    public static <K, V> Map<K, V> createMap(boolean useIdentity) {
        return useIdentity
                ? new IdentityHashMap<>()
                : new LinkedHashMap<>();
    }

    public Stream<Entry<K, TreeMapping<A, B, M, V>>> createMappings(M baseMatching, Tree<B> userTree) {

        //Table<K, Entry<A, B>, Table<A, B, ProblemNeighborhoodAware<S, S>>> leafMappings = createLeafMappings(userTree);
        Map<K, Table<A, B, ProblemNeighborhoodAware<M, ?>>> leafMappingPerView = createLeafMappings(baseMatching, userTree);


        Stream<Entry<K, TreeMapping<A, B, M, V>>> result = leafMappingPerView.entrySet().stream()
            .flatMap(e -> {
                K viewKey = e.getKey();

                Tree<A> viewTree = viewKeyToTree.apply(viewKey);

                BottomUpTreeMapper<A, B, M, C, V> mapper = new BottomUpTreeMapper<A, B, M, C, V>(
                        viewTree, userTree,
                        nodeMapper,
                        addMatchingContribution, isMatchingUnsatisfiable,
                        () -> createTable(aIdentity, bIdentity)
//                        BottomUpTreeTraversals::postOrder//bottomUpTraverser
                        );


                Table<A, B, ProblemNeighborhoodAware<M, ?>> alignmentProblems = e.getValue();
                Multimap<A, B> mm = MultimapUtils.newSetMultimap(aIdentity, bIdentity);

                for(Cell<A, B, ProblemNeighborhoodAware<M, ?>> cell : alignmentProblems.cellSet()) {
                    mm.put(cell.getRowKey(), cell.getColumnKey());
                }

                Multimap<A, A> viewParentToChildren = MultimapUtils.groupBy(mm.keys(), viewTree::getParent, MultimapUtils.newSetMultimap(aIdentity, aIdentity));
                Multimap<B, B> userParentToChildren = MultimapUtils.groupBy(mm.values(), userTree::getParent, MultimapUtils.newSetMultimap(bIdentity, bIdentity));

                Multimap<A, B> parentCandAlignment = createMultimap();//MultimapUtils.newSetMultimap(aIdentity, bIdentity);
                for(Entry<A, Collection<B>> x : mm.asMap().entrySet()) {
                    A a = x.getKey();
                    A aParent = viewTree.getParent(a);
                    for(B y : x.getValue()) {
                        B bParent = userTree.getParent(y);
                        parentCandAlignment.put(aParent, bParent);
                    }
                }

                Stream<Map<A, B>> childAlignmentStream = KPermutationsOfNUtils.kPermutationsOfN(parentCandAlignment, () -> createMap(aIdentity)).flatMap(parentAlignment -> {
                    // For each parent alignment, create the kPermutationsOfN for the children
                    Multimap<A, B> childCandAlignment = createMultimap();
                    for(Entry<A, B> f : parentAlignment.entrySet()) {
                        A aParent = f.getKey();
                        B bParent = f.getValue();
                        for(A aChild : viewParentToChildren.get(aParent)) {
                            Collection<B> bChildren = userParentToChildren.get(bParent);
                            childCandAlignment.putAll(aChild, bChildren);
                        }

                    }

                    Stream<Map<A, B>> t = KPermutationsOfNUtils.kPermutationsOfN(childCandAlignment, () -> createMap(aIdentity));

                    return t;
                });




                // TODO Filter out mappings of view nodes with same parent to user nodes with different parents
                //Stream<Map<A, B>> xxx = KPermutationsOfNUtils.kPermutationsOfN(mm, this::createMap);


                Stream<TreeMapping<A, B, M, V>> r = childAlignmentStream.flatMap(leafAlignment -> {
                    // Get all the problems
                    Collection<ProblemNeighborhoodAware<M, ?>> problems = leafAlignment.entrySet().stream()
                            .map(f -> alignmentProblems.get(f.getKey(), f.getValue()))
                            .collect(Collectors.toList());

                    //M baseMatching = createEmptyMatching.get();

                    ProblemContainer<M> problemContainer = ProblemContainerImpl.create(problems);
                    ProblemSolver<M> solver = new ProblemSolver<>(problemContainer, baseMatching, matchingCombiner);

                    return solver.streamSolutions().map(matching -> {
                        // For each alignment and matching perform the tree mapping

                        TreeMapping<A, B, M, V> s = mapper.solve(matching, leafAlignment);

                        return s;
                    });
                });

                return r.map(item -> new SimpleEntry<>(viewKey, item));
            });

        return result;
    }



    // for each view key there may be multiple tree alignments
    // The concrete mapping: Multimap<K, Entry<S, Table<A, B, Entry<S, C>>>>
    //                                         ^ overall mapping so far


    /**
     *
     */
    public Map<K, Table<A, B, ProblemNeighborhoodAware<M, ?>>> createLeafMappings(M baseMatching, Tree<B> userTree) {
        Map<K, Table<A, B, ProblemNeighborhoodAware<M, ?>>> result = new HashMap<>();

        Collection<B> leafNodes = TreeUtils.getLeafs(userTree);

        for(B userOp : leafNodes) {

            // Create the initial matching for the leafs
            // Obtain the candidate views for that user node
            Table<K, A, ProblemNeighborhoodAware<M, ?>> leafMatchings = leafMatcher.apply(userTree, userOp, baseMatching);

            for(Cell<K, A, ProblemNeighborhoodAware<M, ?>> leafMatching : leafMatchings.cellSet()) {
                K viewKey = leafMatching.getRowKey();
                A viewOp = leafMatching.getColumnKey();
                //Tree<A> viewParentTree = viewKeyToTree.apply(viewKey);
                //A viewParentOp = viewParentTree.getParent(viewOp);
                ProblemNeighborhoodAware<M, ?> matchings = leafMatching.getValue();

                //B userParentOp = userTree.getParent(userOp);
                //Entry<A, B> parentMatch = new SimpleEntry<>(viewParentOp, userParentOp);

                Table<A, B, ProblemNeighborhoodAware<M, ?>> table = HashBasedTable.create();
                table.put(viewOp, userOp, matchings);

                result.put(viewKey, table);
                //viewCandMatching.put(viewKey, viewParentOp, table);
                //viewCandMatching.put(viewKey, parentMatch, table);
            }
        }

        return result;
    }

}


//.map(Map::entrySet)
//.map(Collection::stream)
//.collect(Collectors.toMap(
//      Entry::getKey,
//      Entry::getValue,
//      (u, v) -> { throw new IllegalStateException(); },
//      this::createMap);
