package org.aksw.jena_sparql_api.query_containment.index;

import java.util.List;
import java.util.function.Function;

import org.aksw.commons.collections.trees.Tree;
import org.aksw.commons.collections.trees.TreeUtils;
import org.aksw.commons.graph.index.core.SubgraphIsomorphismIndex;
import org.aksw.commons.graph.index.jena.SubgraphIsomorphismIndexJena;
import org.aksw.commons.graph.index.jena.transform.OpTransformNormalizeUnaryOps;
import org.aksw.commons.graph.index.jena.transform.QueryToGraph;
import org.aksw.commons.jena.jgrapht.PseudoGraphJenaGraph;
import org.aksw.jena_sparql_api.algebra.utils.OpUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transformer;
import org.jgrapht.DirectedGraph;



public class QueryContainmentIndexImpl<K, G, N, O> {

    protected Function<? super O, O> normalizer;
    protected Function<? super O, G> queryToGraph;

    protected SubgraphIsomorphismIndex<K, G, N> index;

    public static DirectedGraph<Node, Triple> queryToJGraphT(Op op) {
        Graph jenaGraph = QueryToGraph.queryToGraph(op);
        DirectedGraph<Node, Triple> result = new PseudoGraphJenaGraph(jenaGraph);
        return result;
    }

    public static <K> QueryContainmentIndexImpl<K, DirectedGraph<Node, Triple>, Node, Op> create() {

        SubgraphIsomorphismIndex<K, DirectedGraph<Node, Triple>, Node> sii = SubgraphIsomorphismIndexJena.create();

        QueryContainmentIndexImpl<K, DirectedGraph<Node, Triple>, Node, Op> result = new QueryContainmentIndexImpl<K, DirectedGraph<Node, Triple>, Node, Op>(
                QueryToGraph::normalizeOp,
                QueryContainmentIndexImpl::queryToJGraphT,
                sii
                );
        return result;

    }


    public QueryContainmentIndexImpl(Function<? super O, O> normalizer, Function<? super O, G> queryToGraph,
            SubgraphIsomorphismIndex<K, G, N> index) {
        super();
        this.normalizer = normalizer;
        this.queryToGraph = queryToGraph;
        this.index = index;
    }

    public void put(K key, O viewOp) {


        O normViewOp = normalizer.apply(viewOp);

        Tree<Op> tree = OpUtils.createTree((Op)normViewOp);
        List<List<Op>> nodesPerLevel = TreeUtils.nodesPerLevel(tree);

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
//        Tree<Op> tree = OpUtils.createTree(userOp);
//        List<List<Op>> nodesPerLevel = TreeUtils.nodesPerLevel(tree);
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

        // Group matches by view queries
        // I.e. resolve the (view) pattern key to the (view) query id







    }



    //public static Op


    public static void main(String[] args) {
        QueryContainmentIndexImpl<Node, DirectedGraph<Node, Triple>, Node, Op> index = QueryContainmentIndexImpl.create();
        Op op = Algebra.toQuadForm(Algebra.compile(QueryFactory.create("PREFIX : <http://ex.org/> SELECT DISTINCT ?s { ?s a ?t . FILTER(?t = :foo || ?t = :bar) }")));

        System.out.println(op);
        op = QueryToGraph.normalizeOp(op);
//        op = Transformer.transform(new OpTransformNormalizeUnaryOps(), op);

        System.out.println("normalized args: " + op);

        index.put(Node.ANY, op);

    }
}
