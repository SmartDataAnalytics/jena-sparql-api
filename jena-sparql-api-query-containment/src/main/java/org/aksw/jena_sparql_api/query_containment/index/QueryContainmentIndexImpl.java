package org.aksw.jena_sparql_api.query_containment.index;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

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
import org.jgrapht.DirectedGraph;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;



public class QueryContainmentIndexImpl<K, G, N, O> {

    protected Function<? super O, O> normalizer;
    protected Function<? super O, G> opToGraph;

    protected Function<O, List<O>> parentToChildren;

    protected SubgraphIsomorphismIndex<Entry<K, Long>, G, N> index;

    protected Table<K, Long, TreeNode<O>> keyToNodeIndexToNode = HashBasedTable.create();

    public static DirectedGraph<Node, Triple> queryToJGraphT(Op op) {
        DirectedGraph<Node, Triple> result = null;

        if(op instanceof OpExtConjunctiveQuery) {
            Graph jenaGraph = QueryToGraph.queryToGraph(op);
            result = new PseudoGraphJenaGraph(jenaGraph);
        }

        return result;
    }

    public static <K> QueryContainmentIndexImpl<K, DirectedGraph<Node, Triple>, Node, Op> create() {

        SubgraphIsomorphismIndex<Entry<K, Long>, DirectedGraph<Node, Triple>, Node> sii = SubgraphIsomorphismIndexJena.create();

        QueryContainmentIndexImpl<K, DirectedGraph<Node, Triple>, Node, Op> result = new QueryContainmentIndexImpl<K, DirectedGraph<Node, Triple>, Node, Op>(
                QueryToGraph::normalizeOp,
                OpUtils::getSubOps,
                QueryContainmentIndexImpl::queryToJGraphT,
                sii
                );
        return result;

    }


    public QueryContainmentIndexImpl(
            Function<? super O, O> normalizer,
            Function<O, List<O>> parentToChildren,
            Function<? super O, G> opToGraph,
            SubgraphIsomorphismIndex<Entry<K, Long>, G, N> index) {
        super();
        this.normalizer = normalizer;
        this.parentToChildren = parentToChildren;
        this.opToGraph = opToGraph;
        this.index = index;
    }


    public void remove(K key) {
        Map<Long, TreeNode<O>> rows = keyToNodeIndexToNode.row(key);

        // Remove related index entries
        for(Long id : rows.keySet()) {
            Entry<K, Long> e = new SimpleEntry<>(key, id);
            index.removeKey(e);
        }

        rows.clear();
    }

    public void put(K key, O viewOp) {


        O normViewOp = normalizer.apply(viewOp);
        Tree<O> tree = TreeImpl.create(normViewOp, parentToChildren);//OpUtils.createTree((Op)normViewOp);
        //TreeNodeImpl<Op>


//        List<List<Op>> nodesPerLevel = TreeUtils.nodesPerLevel(tree);
//        List<List<TreeNode<Op>>> npl = nodesPerLevel.stream()
//                .map(l -> l.stream().map(n -> (TreeNode<Op>)new TreeNodeImpl<Op>(tree, n)).collect(Collectors.toList()))
//                .collect(Collectors.toList());
//
//        Collections.reverse(npl);

//        npl.get(0).forEach(x -> System.out.println("Item: " + x));


        // Convert the leaf nodes to graphs
        long leafNodeId[] = {0};

        TreeUtils.inOrderSearch(tree.getRoot(), tree::getChildren).forEach(op -> {
            TreeNode<O> node = new TreeNodeImpl<>(tree, op);

            G graph = opToGraph.apply(op);
            if(graph != null) {
                Entry<K, Long> e = new SimpleEntry<>(key, leafNodeId[0]);

                keyToNodeIndexToNode.put(key, leafNodeId[0], node);
                index.put(e, graph);

                leafNodeId[0]++;
            }
        });



        // Iterate the leaf nodes
    }

    /**
     * Convert the tree into layers (first all leaf nodes, then all nodes whose children are all in prior layers).
     *
     * - The lowest layer can only beconjunctive queries and VALUES
     *
     *
     * @param userOp
     */
    public void match(O userOp) {
        O normUserOp = normalizer.apply(userOp);
        Tree<O> tree = TreeImpl.create(normUserOp, parentToChildren);//OpUtils.createTree((Op)normViewOp);

        List<List<O>> nodesPerLevel = TreeUtils.nodesPerLevel(tree);

        //Collections.reverse(nodesPerLevel);
        for(List<O> level : nodesPerLevel) {
            System.out.println("Level");
        //for(List<O> level : Collections.singleton(nodesPerLevel.iterator().next())) {
            for(O op : level) {
                System.out.println("Lookup with : " + op);
                G graph = opToGraph.apply(op);
                if(graph != null) {

                    Multimap<Entry<K, Long>, BiMap<N, N>> candidates = index.lookupX(graph, false);
                    System.out.println("Candidates: " + candidates.size() + ": " + candidates);
                }
            }
        }


//        Tree<Op> tree = OpUtils.createTree(userOp);
//
//
//        // Perform basic lookup of the leaf nodes
//
//        for(List<Op> level : nodesPerLevel) {
//            for(Op op : level) {
//                G queryGraph = queryToGraph.apply(op);
//
//                Multimap<K, BiMap<N, N>> matches = index.lookupX(queryGraph, false);
//            }
//        }
//        TreeUtils.inOrderSearch(tree.getRoot(), tree::getChildren).forEach(op -> {
//            TreeNode<O> node = new TreeNodeImpl<>(tree, op);

            // Create the candidate leaf mapping for the layer



//            G graph = opToGraph.apply(op);
//            if(graph != null) {
//                Entry<K, Long> e = new SimpleEntry<>(key, leafNodeId[0]);
//
//                keyToNodeIndexToNode.put(key, leafNodeId[0], node);
//                index.put(e, graph);
//
//                leafNodeId[0]++;
//            }
//        });




        // Group matches by view queries
        // I.e. resolve the (view) pattern key to the (view) query id







    }



    //public static Op


    public static void main(String[] args) {
        QueryContainmentIndexImpl<Node, DirectedGraph<Node, Triple>, Node, Op> index = QueryContainmentIndexImpl.create();
        Op op = Algebra.toQuadForm(Algebra.compile(QueryFactory.create("PREFIX : <http://ex.org/> SELECT ?s { { SELECT DISTINCT ?s { ?s a ?t . FILTER(?t = :foo || ?t = :bar) } } UNION { ?x ?y ?z } }")));


        index.put(Node.ANY, op);

        index.match(op);

    }
}
