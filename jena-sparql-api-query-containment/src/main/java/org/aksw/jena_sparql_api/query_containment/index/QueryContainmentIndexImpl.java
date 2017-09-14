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
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.jgrapht.DirectedGraph;

import com.codepoetics.protonpack.functions.TriFunction;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;




/**
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

    protected TriFunction<? super A, ? super A, TreeMapping<A, A, BiMap<N, N>, V>, ? extends Entry<BiMap<N, N>, V>> nodeMapper;
    protected Table<K, Long, TreeNode<A>> keyToNodeIndexToNode = HashBasedTable.create();



    public static DirectedGraph<Node, Triple> queryToJGraphT(Op op) {
        DirectedGraph<Node, Triple> result = null;

        if(op instanceof OpExtConjunctiveQuery) {
            Graph jenaGraph = QueryToGraph.queryToGraph(op);
            result = new PseudoGraphJenaGraph(jenaGraph);
        }

        return result;
    }

    /**
     * Creates a QueryContainmentIndex instance for SPARQL queries
     * TODO Move to util class
     *
     * @param nodeMapper
     * @return
     */
    public static <K, V> QueryContainmentIndexImpl<K, DirectedGraph<Node, Triple>, Node, Op, V> create(TriFunction<? super Op, ? super Op, TreeMapping<Op, Op, BiMap<Node, Node>, V>, ? extends Entry<BiMap<Node, Node>, V>> nodeMapper) {
        SubgraphIsomorphismIndex<Entry<K, Long>, DirectedGraph<Node, Triple>, Node> sii = SubgraphIsomorphismIndexJena.create();

        QueryContainmentIndexImpl<K, DirectedGraph<Node, Triple>, Node, Op, V> result = new QueryContainmentIndexImpl<K, DirectedGraph<Node, Triple>, Node, Op, V>(
                QueryToGraph::normalizeOp,
                OpUtils::getSubOps,
                QueryContainmentIndexImpl::queryToJGraphT,
                sii,
                nodeMapper
                );
        return result;

    }

    public QueryContainmentIndexImpl(
            Function<? super A, A> normalizer,
            Function<A, List<A>> parentToChildren,
            Function<? super A, G> opToGraph,
            SubgraphIsomorphismIndex<Entry<K, Long>, G, N> index,
            TriFunction<? super A, ? super A, TreeMapping<A, A, BiMap<N, N>, V>, ? extends Entry<BiMap<N, N>, V>> nodeMapper
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

    public Stream<Entry<K, TreeMapping<A, A, BiMap<N, N>, V>>> match(A userOp) {
        TreeMapper<K, A, A, BiMap<N, N>, BiMap<N, N>, V> treeMapper = new TreeMapper<>(
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

//    public static Entry<BiMap<Node, Node>, Op> mapNodes(Op viewNode, Op userNode, TreeMapping<Op, Op, BiMap<Node, Node>, Op> mapping) {
//        Class<?> viewNodeClass = viewNode.getClass();
//        Class<?> userNodeClass = userNode.getClass();
//
//        Entry<BiMap<Node, Node>, Op> result;
//
//        if(!Objects.equals(viewNodeClass, userNodeClass)) {
//            result = null;
//        } else {
//            map(viewNode, userNode, mapping)
//
//
//            result = SimpleEntry<>(HashBiMap.create(), OpNull.create());
//        }
//
//        return result;
//    }


    public static void main(String[] args) {
        NodeMapperOpEquality nodeMapper = new NodeMapperOpEquality();
        QueryContainmentIndexImpl<Node, DirectedGraph<Node, Triple>, Node, Op, Op> index = QueryContainmentIndexImpl.create(nodeMapper);

        Op opA = Algebra.toQuadForm(Algebra.compile(QueryFactory.create("PREFIX : <http://ex.org/> SELECT ?s { ?s a :Airport }")));
        Op opB = Algebra.toQuadForm(Algebra.compile(QueryFactory.create("PREFIX : <http://ex.org/> SELECT ?s { { SELECT DISTINCT ?s { ?s a ?t . FILTER(?t = :foo || ?t = :bar) } } UNION { ?x ?y ?z } }")));


        index.put(NodeFactory.createURI("http://a"), opA);
        index.put(NodeFactory.createURI("http://b"), opB);

        index.match(opB).forEach(mr -> System.out.println("Match result: " + mr));
    }
}

