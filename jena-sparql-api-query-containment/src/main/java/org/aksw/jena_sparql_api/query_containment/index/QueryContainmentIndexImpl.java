package org.aksw.jena_sparql_api.query_containment.index;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import org.aksw.combinatorics.solvers.ProblemNeighborhoodAware;
import org.aksw.combinatorics.solvers.ProblemStaticSolutions;
import org.aksw.commons.collections.MapUtils;
import org.aksw.commons.collections.trees.Tree;
import org.aksw.commons.collections.trees.TreeImpl;
import org.aksw.commons.collections.trees.TreeNode;
import org.aksw.commons.collections.trees.TreeNodeImpl;
import org.aksw.commons.collections.trees.TreeUtils;
import org.aksw.commons.graph.index.core.SubgraphIsomorphismIndex;
import org.aksw.commons.graph.index.jena.SubgraphIsomorphismIndexJena;
import org.aksw.commons.graph.index.jena.transform.QueryToGraph;
import org.aksw.commons.jena.jgrapht.PseudoGraphJenaGraph;
import org.aksw.jena_sparql_api.algebra.utils.OpExtConjunctiveQuery;
import org.aksw.jena_sparql_api.algebra.utils.OpUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpNull;
import org.jgrapht.DirectedGraph;

import com.codepoetics.protonpack.functions.TriFunction;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

/*
 * Tree Matching algo:
 *
 *   Input: A user tree, an index of view trees
 *   Output: For each view tree key, a set of tree alignments between view tree and user tree, and for each tree alignment the matching and mapping information per node
 *
 * Assumptions:
 *   Per tree alignment: A single view node can only map to a single user node
 *     Implication: If a view node was aligned to a user node, it can no longer act as a candidate for further alignments
 *
 *
 *
 * Leaf Mappings:
 *   Given the leaf mappings, we simply enumerate all kPermutationsOfN.
 *   TODO Can we optimize this process by clustering by parent first?
 *
 *   For each view leaf, we map it to the candidate query leafs (note: this lookup is performed using the user leafs)
 *   In the next step, we cluster the candidates by their implied parent-alignment. (so that we are not mapping leafs to each other, which have different parents)
 *   Now we can obtain concrete aligments between the leafs. Note, that in this step, candidate view leafs that have been previously mapped are excluded from being aligned again.
 *   For each candidate alignment, we can now obtain the matchings.
 *
 *
 *   Combinations:
 *     In the old code, we did simply kCombinationsOfN over all leafs in one step. This means, that all alignments WHERE ALL VIEW LEAFS ARE ALIGNED are enumerated
 *     Now, I think we should invert this:
 *       Given the candidates of the user tree, find ALL POSSIBLE ALIGNMENTS WITH THE view.
 *
 *  The main point is, that the child alignment strategy should be independent from the tree matcher.
 *
 *
 *
 *
 * So maybe the only working approach:
 * - Create all kCombinationsOfN among leaf nodes
 * - For each leaf node combination, enumerate the isomorphism
 * - perform the tree matching / mapping upwards
 *
 */

public class QueryContainmentIndexImpl<K, G, N, A, V> {
    protected Function<? super A, A> normalizer;
    protected Function<? super A, G> opToGraph;

    protected Function<A, List<A>> parentToChildren;

    protected SubgraphIsomorphismIndex<Entry<K, Long>, G, N> index;

    protected TriFunction<A, A, TreeMapping<A, A, BiMap<N, N>, V>, Entry<BiMap<N, N>, V>> nodeMapper;
    protected Table<K, Long, TreeNode<A>> keyToNodeIndexToNode = HashBasedTable.create();



    public static DirectedGraph<Node, Triple> queryToJGraphT(Op op) {
        DirectedGraph<Node, Triple> result = null;

        if(op instanceof OpExtConjunctiveQuery) {
            Graph jenaGraph = QueryToGraph.queryToGraph(op);
            result = new PseudoGraphJenaGraph(jenaGraph);
        }

        return result;
    }

    public static <K> QueryContainmentIndexImpl<K, DirectedGraph<Node, Triple>, Node, Op, Op> create() {

        SubgraphIsomorphismIndex<Entry<K, Long>, DirectedGraph<Node, Triple>, Node> sii = SubgraphIsomorphismIndexJena.create();

        QueryContainmentIndexImpl<K, DirectedGraph<Node, Triple>, Node, Op, Op> result = new QueryContainmentIndexImpl<K, DirectedGraph<Node, Triple>, Node, Op, Op>(
                QueryToGraph::normalizeOp,
                OpUtils::getSubOps,
                QueryContainmentIndexImpl::queryToJGraphT,
                sii,
                QueryContainmentIndexImpl::mapNodes
                );
        return result;

    }

    public QueryContainmentIndexImpl(
            Function<? super A, A> normalizer,
            Function<A, List<A>> parentToChildren,
            Function<? super A, G> opToGraph,
            SubgraphIsomorphismIndex<Entry<K, Long>, G, N> index,
            TriFunction<A, A, TreeMapping<A, A, BiMap<N, N>, V>, Entry<BiMap<N, N>, V>> nodeMapper
            ) {
        super();
        this.normalizer = normalizer;
        this.parentToChildren = parentToChildren;
        this.opToGraph = opToGraph;
        this.index = index;
        this.nodeMapper = nodeMapper;
    }

    public void remove(K key) {
        Map<Long, TreeNode<A>> rows = keyToNodeIndexToNode.row(key);

        // Remove related index entries
        for(Long id : rows.keySet()) {
            Entry<K, Long> e = new SimpleEntry<>(key, id);
            index.removeKey(e);
        }

        rows.clear();
    }

    public void put(K key, A viewOp) {
        A normViewOp = normalizer.apply(viewOp);
        Tree<A> tree = TreeImpl.create(normViewOp, parentToChildren);//OpUtils.createTree((Op)normViewOp);

        // Convert the leaf nodes to graphs
        long leafNodeId[] = {0};

        //TreeUtils.inOrderSearch(tree.getRoot(), tree::getChildren)
        TreeUtils.leafStream(tree).forEach(op -> {
            TreeNode<A> node = new TreeNodeImpl<>(tree, op);

            G graph = opToGraph.apply(op);
            if(graph != null) {
                Entry<K, Long> e = new SimpleEntry<>(key, leafNodeId[0]);

                keyToNodeIndexToNode.put(key, leafNodeId[0], node);
                System.out.println("Insert: " + e);
                System.out.println("Graph: " + graph);

                index.put(e, graph);

                index.printTree();
                leafNodeId[0]++;
            }
        });

    }


    public Table<K, A, ProblemNeighborhoodAware<BiMap<N, N>, ?>> lookupLeaf(Tree<A> tree, A leaf, BiMap<N, N> baseMatching) {
        G graph = opToGraph.apply(leaf);

        Multimap<Entry<K, Long>, BiMap<N, N>> matches = index.lookupX(graph, false);

        Table<K, A, ProblemNeighborhoodAware<BiMap<N, N>, ?>> result = TreeMapper.createTable(true, true);
        for(Entry<Entry<K, Long>, BiMap<N, N>> e : matches.entries()) {
            Entry<K, Long> f = e.getKey();
            K viewKey = f.getKey();
            Long leafIndex = f.getValue();

            TreeNode<A> viewTreeNode = keyToNodeIndexToNode.get(viewKey, leafIndex);
            A leafNode = viewTreeNode.getNode();
            BiMap<N, N> matching = e.getValue();

            ProblemNeighborhoodAware<BiMap<N, N>, ?> problems = new ProblemStaticSolutions<>(Collections.singleton(matching));

            result.put(viewKey, leafNode, problems);
        }

        return result;
    }

    public static Entry<BiMap<Node, Node>, Op> mapNodes(Op viewNode, Op usernode, TreeMapping<Op, Op, BiMap<Node, Node>, Op> mapping) {
        return new SimpleEntry<>(HashBiMap.create(), OpNull.create());
    }

    public Stream<Entry<K, TreeMapping<A, A, BiMap<N, N>, V>>> match(A userOp) {
        TreeMapper<K, A, A, BiMap<N, N>, BiMap<N, N>, V> treeMapper = new TreeMapper<K, A, A, BiMap<N, N>, BiMap<N, N>, V>(
            key -> keyToNodeIndexToNode.row(key).values().iterator().next().getTree(),
            this::lookupLeaf,
            nodeMapper,
            (m, c) -> MapUtils.mergeCompatible(m, c, HashBiMap::create),
            (m1, m2) -> MapUtils.mergeCompatible(m1, m2, HashBiMap::create),
            Objects::isNull,
            true,
            true
        );

        A normUserOp = normalizer.apply(userOp);
        Tree<A> userTree = TreeImpl.create(normUserOp, parentToChildren);//OpUtils.createTree((Op)normViewOp);

        BiMap<N, N> baseMatching = HashBiMap.create();
        Stream<Entry<K, TreeMapping<A, A, BiMap<N, N>, V>>> result = treeMapper.createMappings(baseMatching, userTree);

        return result;
    }


    public static void main(String[] args) {
        QueryContainmentIndexImpl<Node, DirectedGraph<Node, Triple>, Node, Op, Op> index = QueryContainmentIndexImpl.create();
        Op op = Algebra.toQuadForm(Algebra.compile(QueryFactory.create("PREFIX : <http://ex.org/> SELECT ?s { { SELECT DISTINCT ?s { ?s a ?t . FILTER(?t = :foo || ?t = :bar) } } UNION { ?x ?y ?z } }")));


        index.put(Node.ANY, op);

        index.match(op).forEach(mr -> System.out.println("Match result: " + mr));

    }
}


interface Aggregate<S, C> {
    S createAggregate();
    Entry<S, Runnable> addContribution(S aggregate, C contribution);
    Entry<S, Runnable> mergeAggregates(S a, S b);
    boolean isAcceptable(S solution);
    S finalize(S solution);
}

class Mapping<A, B, S, C> {
    A sourceNode;
    B targetNode;
    S matching;
    C mapping;
}


interface MappingCombiner<K, A, B, S, C> {
    //Function<K, S, Entry<A, B>, List<Mapping<A, B, S, C>>, Entry<S, C>> mappingCombiner;
    //Mulitmap<S, C> combine(S overallMatching, A viewParent, B userParent, Table<A, B, Entry<S, C>>> );
}




//public void createMappings(Tree<B> userTree) {
//
//  // Matching phase
//  // Mapping phase
//
//  Stream<Set<B>> userTreeBottomUpTraversal = bottomUpTreeTraversalFactory.apply(userTree);
//
//  Iterator<Set<B>> it = userTreeBottomUpTraversal.iterator();
//
//  //Table<K, Entry<A, B>, Table<A, B, ProblemNeighborhoodAware<S, S>>> viewCandMatching = HashBasedTable.create();
//
//  while(it.hasNext()) {
//      Set<B> nodes = it.next();
//      Multimap<B, B> parentGroups = TreeUtils.groupByParent(userTree, nodes, MultimapUtils.newSetMultimap(bIdentity, bIdentity));
//
//      Table<K, Entry<A, B>, Table<A, B, ProblemNeighborhoodAware<S, S>>> leafMappings;
//
//      for(Entry<B, Collection<B>> group : parentGroups.asMap().entrySet()) {
//          // There are 2 cases for child nodes:
//          // (a) Leaf nodes: for those we get candidate mappings
//          // (b) Prior mapped nodes: In this case we have concrete mappings in the stack
//
//          for(B child : group.getValue()) {
//              Table<K, A, ProblemNeighborhoodAware<S, S>> leafMappingContribs = createLeafMappings(userTree, child);
//
//              ProblemContainer<S> problemContainer = ProblemContainerImpl.create(concreteLeafAlignment.values());
//              ProblemSolver<S> solver = new ProblemSolver<>(problemContainer, baseMatching, matchingCombiner);
//
//
//
//
//              //Table<K, Entry<A, B>, Table<A, B, ProblemNeighborhoodAware<S, S>>> leafMappingContribs = createLeafMappings(userTree, child);
//
//
//              //if(userTree.getChildren(node))
//
//          }
//
//
//
//
//      }
//
//
//      //for(K viewKey : leafMappings.
//      for(Cell<K, Entry<A, B>, Table<A, B, ProblemNeighborhoodAware<S, S>>> entry : leafAlignments) {
//
//
//          // Obtain the leaf alignments for the parent node
//          Collection<Table<A, B, ProblemNeighborhoodAware<S, S>>> concreteLeafAligments;
//
//
//          S baseMatching;
//          for(Table<A, B, ProblemNeighborhoodAware<S, S>> concreteLeafAlignment : concreteLeafAligments) {
//              ProblemContainer<S> problemContainer = ProblemContainerImpl.create(concreteLeafAlignment.values());
//              ProblemSolver<S> solver = new ProblemSolver<>(problemContainer, baseMatching, matchingCombiner);
//
//              Iterable<S> matchings = () -> solver.streamSolutions().iterator();
//
//              for(S matching : matchings) {
//
//
//                  // Now we have a concrete alignment and a concrete mapping
//                  // Next step is to do recursion...
//
//                  recurse(viewKey, matching);
//
//
//              }
//
//          }
//
//
//          //ProblemContainerImpl.create(
//
//      }
//
//      // For the current child alignment, iterate the matchings
//
//
//
//
////      for(B node : nodes) {
////
////      }
//
//  }
//
//}



/**
 * Given leaf candidate mapping: Check whether based on the parent mapping there are valid child mappings
 *
 */
//public Object createParentMappings(Tree<B> userTree, Table<K, Entry<A, B>, Table<A, B, ProblemNeighborhoodAware<S, S>>> viewCandMatchings) {
//    for(Entry<K, Map<Entry<A, B>, Table<A, B, ProblemNeighborhoodAware<S, S>>>> viewCandMatching : viewCandMatchings.rowMap().entrySet()) {
//        K viewKey = viewCandMatching.getKey();
//        Tree<A> viewTree = keyToViewTree.apply(viewKey);
//
//        // For each parent node's candidate child mappings:
//        // Generate all possible satisfiable child matchings
//        for(Entry<Entry<A, B>, Table<A, B, ProblemNeighborhoodAware<S, S>>> e : viewCandMatching.getValue().entrySet()) {
//
////            // Collect the problems
////        	List<>
////
////            ProblemContainerImpl.create(problems);
//
//            Entry<A, B> parentMatching = e.getKey();
//            A viewParentOp = parentMatching.getKey();
//            B userParentOp = parentMatching.getValue();
//            Table<A, B, ProblemNeighborhoodAware<S, S>> childMatchings = e.getValue();
//
//            Collection<A> viewChildren = viewTree.getChildren(viewParentOp);
//            Collection<B> userChildren = userTree.getChildren(userParentOp);
//
//
//            // The matcherFactory yields concrete matchings
//            TriFunction<Collection<A>, Collection<B>, Table<A, B, ProblemNeighborhoodAware<S, S>>, Collection<Table<A, B, S>>> matcherFactory = matcherFactoryFactory.apply(viewParentOp, userParentOp);
//
//
//            Collection<Table<A, B, S>> concreteMatchings = matcherFactory.apply(viewChildren, userChildren, childMatchings);
//
//
//            // For each candidate mapping: Compute the replacement operator for B - lets call it C
//            Table<B, A, Multimap<S, C>> childMapping = HashBasedTable.create();
//            for(Table<A, B, S> concreteMatching : concreteMatchings) {
//
//                // Compute the overall matching
//                S overallMatching = createEmptyMatching.get();
//                boolean isSatisfiable = isMatchingSatisfiable.test(overallMatching);
//                for(Cell<A, B, S> candMatchingEntry : concreteMatching.cellSet()) {
//                    A a = candMatchingEntry.getRowKey();
//                    B b = candMatchingEntry.getColumnKey();
//                    S matching = candMatchingEntry.getValue();
//
//                    overallMatching = matchingCombiner.apply(overallMapping, matching);
//                    isSatisfiable = isMatchingSatisfiable.test(overallMatching);
//                    if(!isSatisfiable) {
//                        break;
//                    }
//                }
//
//                if(isSatisfiable) {
//
//                    Table<A, B, Multimap<S, C>> childMappings = HashBasedTable.create();
//                    TriFunction<A, B, S, Multimap<S, C>> mapper = mapperFactory.apply(viewParentOp, userParentOp);
//                    for(Cell<A, B, S> candMatchingEntry : concreteMatching.cellSet()) {
//                        A a = candMatchingEntry.getRowKey();
//                        B b = candMatchingEntry.getColumnKey();
//                        Multimap<S, C> mappings = mapper.apply(a, b, overallMatching);
//                    }
//                }
//
//
//            }
//
//
//            viewCandMatching.put(viewKey, viewParentOp, childMapping);
//
//
//            // Return the stream of mappings
//        }
//    }
//
//
//
//}




/**
 * So here is the thing:
 * - For leaf nodes, we support getting the candidate matchings as problem containers.
 * When matching the inner nodes, we then request the concrete mappings, and check whether we can process the remainder of the tree
 * using such a mapping.
 *
 * - For bottom up processing, we can do 'depth' first or layer first processing.
 *   The difference is, that if we used layers, the isomorphy matching would have to be global, but we have more leaf candidates
 *   to check whether the matching is unsatisfiable
 * - The difference is essentially, whether the base table contains only 1 entry, or multiple entries (i.e. for the whole layer)
 *
 * But i think we then still need
 * LayerBasedTraverser and NodeBasedTraverser
 *
 *
 * Note:
 * - Mappings are only computed from concrete matchings.
 * -
 *
 *
 *
 * Generating candidate matches for nodes:
 * For a leaf node, we simply obtain all candidate matches via a lookup
 * For inner nodes: For every user child we obtain (per view) its candidate matchings
 *
 * For each candidate child mapping, we need to combine the child matching contributions to an overall matching
 *
 *
 * Candidate Mapping structure:
 *   Table<K, A, Table<A, B, ProblemNeighborhoodAware<S, S>>>
 *   For each mapping of a the children of a view parent node A, there is the set of possible matchings
 *
 * Concrete Mapping structure:
 *   Table<K, A, Multimap<S, Table<A, B, S>>
 *   For each view/parent node, for each overall matching, there is a mapping between the child nodes with a matching contribution
 *
 *
 *
 * @param userTree
 * @param userOp
 * @param viewCandMatching
 * @return
 */
//public Map<K, Table<A, B, C>> matchInner(S baseMatching, Tree<B> userTree, B userOp, Table<K, A, Table<A, B, ProblemNeighborhoodAware<S, S>>> viewCandMatching) {
//    Collection<B> userChildren = userTree.getChildren(userOp);//userTree.getChildren(userOp);
//
//
//
//    //
//    //Map<K, Map<O, >>
//    //
//    BiFunction<A, B, TriFunction<Collection<A>, Collection<B>, Table<A, B, ProblemNeighborhoodAware<S, S>>, Collection<Table<A, B, S>>>> matcherFactoryFactory;
//
//
//
//    BiFunction<A, B, TriFunction<A, B, S, C>> mapperFactory;
//
//
//
//
//    // For each child compute the candidate matchings and mappings
//    if(userChildren.isEmpty()) {
//        // Obtain the candidate views for that user node
//        Table<K, A, ProblemNeighborhoodAware<S, S>> leafMatchings = leafMatcher.apply(userTree, userOp, baseMatching);
//
//        for(Cell<K, A, ProblemNeighborhoodAware<S, S>> leafMatching : leafMatchings.cellSet()) {
//            K viewKey = leafMatching.getRowKey();
//            A viewOp = leafMatching.getColumnKey();
//            Tree<A> viewParentTree = keyToViewTree.apply(viewKey);
//            A viewParentOp = viewParentTree.getParent(viewOp);
//            ProblemNeighborhoodAware<S, S> matchings = leafMatching.getValue();
//
//            B userParentOp = userTree.getParent(userOp);
//            //Entry<A, B> parentMatch = new SimpleEntry<>(viewParentOp, userParentOp);
//
//            Table<A, B, ProblemNeighborhoodAware<S, S>> table = HashBasedTable.create();
//            table.put(viewOp, userParentOp, matchings);
//            viewCandMatching.put(viewKey, viewParentOp, table);
//        }
//    } else {
//
//
//        //Table<K, A, Table<A, B, Multimap<S, C>>> childMapping;
//        Table<K, A, Table<A, B, ProblemNeighborhoodAware<S, S>>> childMapping;
//        for(B child : userChildren) {
//            // If all children are leaf nodes...
//
//
//
//
//            // Returns for each candidate view key the candidate mappings of that child
//            // -> K -> U -> V -> S
//            //Table<K, Entry<A, B>, Table<A, B, Collection<S>>> someChildMatchingOrMatching = matchInner(userTree, userOp, viewCandMatching);
//            //Table<K, A, Table<A, B, Collection<S>>> someChildMatchingOrMatching = matchInner(userTree, userOp, viewCandMatching);
//             matchInner(baseMatching, userTree, userOp, viewCandMatching);
//        }
//
//
//        // For each candidate view
//        //for(Entry<K, Map<A, Table<A, B, Collection<S>>>> viewEntry : viewCandMatching.rowMap().entrySet()) {
//        for(Entry<K, Map<A, Table<A, B, ProblemNeighborhoodAware<S, S>>>> viewEntry : childMapping.rowMap().entrySet()) {
//            K viewKey = viewEntry.getKey();
//            Tree<A> viewTree = keyToView.get(viewKey);
//
//            // For each parent node's candidate child mappings:
//            // Generate all possible satisfiable child matchings
//            for(Entry<A, Table<A, B, ProblemNeighborhoodAware<S, S>>> e : viewEntry.getValue().entrySet()) {
//
////                // Collect the problems
////            	List<>
////
////                ProblemContainerImpl.create(problems);
//
//                A viewParentOp = e.getKey();
//
//                // The matcherFactory yields concrete matchings
//                TriFunction<Collection<A>, Collection<B>, Table<A, B, ProblemNeighborhoodAware<S, S>>, Entry<S, Collection<Table<A, B, S>>>> matcherFactory = matcherFactoryFactory.apply(viewParentOp, userParentOp);
//
//                Collection<A> viewChildren =  viewTree.getChildren(viewParentOp);
//
//                Table<A, B, Multimap<S, C>> childMatchings = e.getValue();
//
//                // Given the child mapping candidates together with their matchings, find out all combinations
//                // where the compound matching is satisfiable
//
//
//                // Collect the candidate mapping into a multimap
//                Multimap<A, B> candMatchings = MultimapUtils.newIdentitySetMultimap();
//                for(Entry<A, Map<B, Multimap<S, C>>> entries : childMatchings.rowMap().entrySet()) {
//                    A a = entries.getKey();
//                    Collection<B> bs = entries.getValue().keySet();
//                    candMatchings.putAll(a, bs);
//                }
//
//
//
//                // Determine how to handle the candidate mappings of the children
//                Collection<Map<A, B>> concreteMatchings = matcherFactory.apply(viewChildren, userChildren, candMatchings);
//
//
//                // Create the possible candidate mappings
//                // TODO: A mapper may produce additional matching information; 2 ways for handling this:
//                // (1) Mapper returns a Collection<Entry<S, C>> (or Map) (2) wrap this in C
//                TriFunction<A, B, S, C> mapper = mapperFactory.apply(viewParentOp, userParentOp);
//
//                //Table<A, B, S> concreteMatching;
//
//
//
//                // For each candidate mapping: Compute the replacement operator for B - lets call it C
//                Table<B, A, Multimap<S, C>> childMapping = HashBasedTable.create();
//                for(Map<A, B> concreteMatching : concreteMatchings) {
//                    for(Entry<A, B> candMatchingEntry : concreteMatching.entrySet()) {
//                        A a = candMatchingEntry.getKey();
//                        B b = candMatchingEntry.getValue();
//
//                        Multimap<S, C> matchingInfos = childMatchings.get(a, b);
//                        for(S matchingInfo : matchingInfos) {
//                            C mapping = mapper.apply(a, b, matchingInfo);
//
//                            Entry<S, C> matchingAndMapping = new SimpleEntry<>(matchingInfo, mapping);
//                            childMapping.put(b, a, matchingAndMapping);
//                        }
//                    }
//                }
//
//
//                viewCandMatching.put(viewKey, viewParentOp, childMapping);
//
//
//                // Return the stream of mappings
//            }
//        }
//
//
//
//    }
//
//
//
//    return null;
//}



// Instead of Collection<BiMap<N, N>> we could use Problem<A, B, BiMap<N, N>>
//public Object computeChildMatching(Tree<B> tree, B op, Table<K, A, Table<A, B, Collection<Entry<S, C>>>> viewCandMapping) {
//    return null;
//}

//
//
//public Object computeChildMatchingImpl(Tree<B> tree, B op, Table<K, A, Table<TreeNode<A>, TreeNode<B>, Collection<S>>> viewCandMapping) {
//
////TreeNode<O> userNode = new TreeNodeImpl<>(tree, op);
//    //B op = userNode.getNode();
//
////      System.out.println("Lookup with : " + op);
//      G graph = opToGraph.apply(op);
//      if(graph != null) {
//
//          Multimap<Entry<K, Long>, BiMap<N, N>> candidates = index.lookupX(graph, false);
////          System.out.println("Candidates: " + candidates.size() + ": " + candidates);
//
//          // Group all candidates belonging to the same query
//          for(Entry<Entry<K, Long>, Collection<BiMap<N, N>>> xxx : candidates.asMap().entrySet()) {
//              Entry<K, Long> e = xxx.getKey();
//              K key = e.getKey();
//              Long nodeId = e.getValue();
//              Collection<BiMap<N, N>> isosContribs = xxx.getValue();
//
//              TreeNode<O> viewNode = keyToNodeIndexToNode.get(key, nodeId);
//
//              Table<TreeNode<O>, TreeNode<O>, Collection<BiMap<N, N>>> table = candToMappings.computeIfAbsent(key, x -> HashBasedTable.create());//x -> new LinkedHashMap<>());
//              Map<TreeNode<O>, Collection<BiMap<N, N>>> toIsos = table.row(viewNode);
//              Collection<BiMap<N, N>> isos = toIsos.computeIfAbsent(userNode, x -> new LinkedHashSet<>());
//              isos.addAll(isosContribs);
//
//
//
//              // For testing: get the parent OpDef nodes
////              OpDistinctExtendFilter viewDef = (OpDistinctExtendFilter)viewNode.getParent().getNode();
////              OpDistinctExtendFilter userDef = (OpDistinctExtendFilter)viewNode.getParent().getNode();
////
////              for(BiMap<N, N> iso : isos) {
////                  TreeMatcher.match(viewDef, userDef, (Map<Node, Node>)iso);
////              }
//
//              table.put(viewNode, userNode, isos);
//          }
//      }
//
//      System.out.println("Level mapping:");
//      for(Entry<K, Table<TreeNode<O>, TreeNode<O>, Collection<BiMap<N, N>>>> e : candToMappings.entrySet()) {
//          System.out.println("  Key: " + e.getKey()); // + " " + e.getValue());
//          for(Entry<TreeNode<O>, Map<TreeNode<O>, Collection<BiMap<N, N>>>> f : e.getValue().rowMap().entrySet()) {
//              System.out.println("    View: " + f.getKey());
//
//              for(Entry<TreeNode<O>, Collection<BiMap<N, N>>> g : f.getValue().entrySet()) {
//                  System.out.println("      User: " + g.getKey() + " via " + g.getValue());
//              }
//          }
//
//      }
//
//
//    return null;
//}
//
//
///**
// * Convert the tree into layers (first all leaf nodes, then all nodes whose children are all in prior layers).
// *
// * - The lowest layer can only beconjunctive queries and VALUES
// *
// *
// * @param userOp
// */
//@SuppressWarnings("unchecked")
//public void match(O userOp) {
//    O normUserOp = normalizer.apply(userOp);
//    Tree<O> tree = TreeImpl.create(normUserOp, parentToChildren);//OpUtils.createTree((Op)normViewOp);
//
//    List<List<O>> nodesPerLevel = TreeUtils.nodesPerLevel(tree);
//
//    Collections.reverse(nodesPerLevel);
//
//    for(List<O> level : Collections.singleton(nodesPerLevel.iterator().next())) { //nodesPerLevel) {
//
//        //Map<K, Multimap<TreeNode<O>, Entry<TreeNode<O>, Collection<BiMap<Node, Node>>>>> candMappings = new HashMap<>();//HashMultimap.create();
//        Map<K, Table<TreeNode<A>, TreeNode<B>, Collection<BiMap<N, N>>>> candToMappings = new HashMap<>();
//
////        System.out.println("Level");
//    //for(List<O> level : Collections.singleton(nodesPerLevel.iterator().next())) {
//        for(O op : level) {
//
//
//        }
//    }
//
//
////    Tree<Op> tree = OpUtils.createTree(userOp);
////
////
////    // Perform basic lookup of the leaf nodes
////
////    for(List<Op> level : nodesPerLevel) {
////        for(Op op : level) {
////            G queryGraph = queryToGraph.apply(op);
////
////            Multimap<K, BiMap<N, N>> matches = index.lookupX(queryGraph, false);
////        }
////    }
////    TreeUtils.inOrderSearch(tree.getRoot(), tree::getChildren).forEach(op -> {
////        TreeNode<O> node = new TreeNodeImpl<>(tree, op);
//
//        // Create the candidate leaf mapping for the layer
//
//
//
////        G graph = opToGraph.apply(op);
////        if(graph != null) {
////            Entry<K, Long> e = new SimpleEntry<>(key, leafNodeId[0]);
////
////            keyToNodeIndexToNode.put(key, leafNodeId[0], node);
////            index.put(e, graph);
////
////            leafNodeId[0]++;
////        }
////    });
//
//
//
//
//    // Group matches by view queries
//    // I.e. resolve the (view) pattern key to the (view) query id
//
//
//
//
//
//
//
//}
//


//public static Op

//TreeNodeImpl<Op>


//List<List<Op>> nodesPerLevel = TreeUtils.nodesPerLevel(tree);
//List<List<TreeNode<Op>>> npl = nodesPerLevel.stream()
//      .map(l -> l.stream().map(n -> (TreeNode<Op>)new TreeNodeImpl<Op>(tree, n)).collect(Collectors.toList()))
//      .collect(Collectors.toList());
//
//Collections.reverse(npl);

//npl.get(0).forEach(x -> System.out.println("Item: " + x));
