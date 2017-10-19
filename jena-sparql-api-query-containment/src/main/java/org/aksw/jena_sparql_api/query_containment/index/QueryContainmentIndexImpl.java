package org.aksw.jena_sparql_api.query_containment.index;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.aksw.combinatorics.solvers.ProblemNeighborhoodAware;
import org.aksw.combinatorics.solvers.ProblemStaticSolutions;
import org.aksw.commons.collections.MapUtils;
import org.aksw.commons.collections.trees.Tree;
import org.aksw.commons.collections.trees.TreeNode;
import org.aksw.commons.collections.trees.TreeNodeImpl;
import org.aksw.commons.collections.trees.TreeUtils;
import org.aksw.commons.graph.index.core.SubgraphIsomorphismIndex;
import org.aksw.commons.graph.index.jena.transform.QueryToGraph;
import org.aksw.commons.graph.index.jena.transform.QueryToGraphVisitor;
import org.aksw.commons.jena.jgrapht.PseudoGraphJenaGraph;
import org.aksw.jena_sparql_api.algebra.utils.ExtendedQueryToGraphVisitor;
import org.aksw.jena_sparql_api.algebra.utils.OpExtConjunctiveQuery;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.jgrapht.DirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class QueryContainmentIndexImpl<K, X, Y, G, N, A, V>
	implements QueryContainmentIndex<K, G, N, A, V>
{	
	private static final Logger logger = LoggerFactory.getLogger(QueryContainmentIndexImpl.class);

	
    //protected Function<? super A, A> normalizer;
    
    protected Function<? super A, X> treePreprocessor;
    protected Function<? super X, ? extends Tree<A>> getTree;
    
    
    protected BiFunction<? super A, ? super X, Y> leafPreprocessor;
    protected Function<? super Y, G> getGraph;

    // We do not map inputs directly to graphs, but to an intermediate object of type X from which the graph for indexing is obtained
    //protected Function<? super A, G> opToGraph;

    //protected Function<A, List<A>> parentToChildren;

    protected SubgraphIsomorphismIndex<Entry<K, Long>, G, N> index;

    //protected TriFunction<? super A, ? super A, TreeMapping<A, A, BiMap<N, N>, V>, ? extends Entry<BiMap<N, N>, V>> nodeMapper;
    
    //protected BiFunction<? super X, ?super X, ? extends NodeMapper<A, A, BiMap<N, N>, V>> nodeMapperFactory;
    //protected BiFunction<? super X, ? super X, ? extends NodeMapper<? super A, ? super A, ? super BiMap<Node, Node>, ? extends V>> nodeMapperFactory;
    protected BiFunction<? super X, ? super X, ? extends NodeMapper<A, A, BiMap<N, N>, BiMap<N, N>, V>> nodeMapperFactory;
    
    protected Table<K, Long, LeafInfo<Entry<K, Long>, A, X, Y, G>> keyToNodeIndexToInfo = HashBasedTable.create();



    public static DirectedGraph<Node, Triple> queryToJGraphT(Op op) {
        DirectedGraph<Node, Triple> result = null;

        if(op instanceof OpExtConjunctiveQuery) {
            Graph jenaGraph = QueryToGraph.queryToGraph(op);
            result = new PseudoGraphJenaGraph(jenaGraph);
        }

        return result;
    }
    
    public static OpGraph queryToOpGraph(Op op) {
        OpGraph result = null;

        if(op instanceof OpExtConjunctiveQuery) {
            OpExtConjunctiveQuery ocq = (OpExtConjunctiveQuery)op;
            //ConjunctiveQuery cq = SparqlCacheUtils.tryExtractConjunctiveQuery(op, generator)

            //System.out.println("indexing: " + ocq.getQfpc());

            Supplier<Supplier<Node>> ssn = () -> { int[] x = {0}; return () -> NodeFactory.createBlankNode("_" + x[0]++); };
            QueryToGraphVisitor q2g = new ExtendedQueryToGraphVisitor(ssn.get());
            q2g.visit(ocq);
            
            result = new OpGraph(q2g.getGraph(), q2g.getNodeToExpr());
        }

        return result;    	    	
    }
    
    
    /* (non-Javadoc)
	 * @see org.aksw.jena_sparql_api.query_containment.index.QueryContainmentIndex#getIndex()
	 */
    @Override
	public SubgraphIsomorphismIndex<Entry<K, Long>, G, N> getIndex() {
		return index;
	}

    /**
     * Creates a QueryContainmentIndex instance for SPARQL queries
     * TODO Move to util class
     *
     * @param nodeMapper
     * @return
     */
//    public static <K, V> QueryContainmentIndex<K, DirectedGraph<Node, Triple>, Node, Op, V> create(TriFunction<? super Op, ? super Op, TreeMapping<Op, Op, BiMap<Node, Node>, V>, ? extends Entry<BiMap<Node, Node>, V>> nodeMapper) {
//        SubgraphIsomorphismIndex<Entry<K, Long>, DirectedGraph<Node, Triple>, Node> sii = SubgraphIsomorphismIndexJena.create();
//
//        QueryContainmentIndex<K, DirectedGraph<Node, Triple>, Node, Op, V> result = create(sii, nodeMapper);
//        return result;
//    }
//
//    public static <K, V> QueryContainmentIndex<K, DirectedGraph<Node, Triple>, Node, Op, V> createFlat(TriFunction<? super Op, ? super Op, TreeMapping<Op, Op, BiMap<Node, Node>, V>, ? extends Entry<BiMap<Node, Node>, V>> nodeMapper) {
//        SubgraphIsomorphismIndex<Entry<K, Long>, DirectedGraph<Node, Triple>, Node> sii = SubgraphIsomorphismIndexJena.createFlat();
//
//        QueryContainmentIndex<K, DirectedGraph<Node, Triple>, Node, Op, V> result = create(sii, nodeMapper);
//        return result;
//    }

    public static <K, V> QueryContainmentIndex<K, DirectedGraph<Node, Triple>, Node, Op, V> create(
    		SubgraphIsomorphismIndex<Entry<K, Long>, DirectedGraph<Node, Triple>, Node> sii,
    		BiFunction<OpContext, OpContext, ? extends NodeMapper<Op, Op, BiMap<Node, Node>, BiMap<Node, Node>, V>> nodeMapperFactory) {

    		QueryContainmentIndex<K, DirectedGraph<Node, Triple>, Node, Op, V> result =
        		new QueryContainmentIndexImpl<K, OpContext, OpGraph, DirectedGraph<Node, Triple>, Node, Op, V>(
        				OpContext::create,
		                OpContext::getNormalizedOpTree,		                
		                (op, opContext) -> QueryContainmentIndexImpl.queryToOpGraph(op),
		                opGraph -> opGraph.getJGraphTGraph(),//opContext.getOpAsGraph().getJGraphTGraph(),
		                sii,
		                nodeMapperFactory
		                );
        return result;
    }

    public QueryContainmentIndexImpl(
            Function<? super A, X> preprocessor,
            Function<? super X, Tree<A>> getTree,
            BiFunction<? super A, ? super X, Y> leafPreprocessor,
            Function<? super Y, G> getGraph,
            
            SubgraphIsomorphismIndex<Entry<K, Long>, G, N> index,
            BiFunction<? super X, ?super X, ? extends NodeMapper<A, A, BiMap<N, N>, BiMap<N, N>, V>> nodeMapperFactory
    		) {
        super();
        this.treePreprocessor = preprocessor;
        this.getTree = getTree;
        this.leafPreprocessor = leafPreprocessor;
        this.getGraph = getGraph;
        //this.metaGraphToGraph = metaGraphToGraph;        
        this.index = index;
        this.nodeMapperFactory = nodeMapperFactory;
    }


    /* (non-Javadoc)
	 * @see org.aksw.jena_sparql_api.query_containment.index.QueryContainmentIndex#remove(K)
	 */
    @Override
	public void remove(K key) {
        Map<Long, ?> rows = keyToNodeIndexToInfo.row(key);

        // Remove related index entries
        for(Long id : rows.keySet()) {
            Entry<K, Long> e = new SimpleEntry<>(key, id);
            index.removeKey(e);
        }

        rows.clear();
    }

    /* (non-Javadoc)
	 * @see org.aksw.jena_sparql_api.query_containment.index.QueryContainmentIndex#put(K, A)
	 */
    @Override
	public void put(K key, A viewOp) {
    	// TODO The normalizer should become part of the preprocessor
        //A normViewOp = normalizer.apply(viewOp);
        
    	X preprocessedA = treePreprocessor.apply(viewOp);
    	Tree<A> tree = getTree.apply(preprocessedA);
    	
    	
        //CA contextA = analyzer.apply(normViewOp);
        
        //Tree<A> tree = TreeImpl.create(normViewOp, parentToChildren);//OpUtils.createTree((Op)normViewOp);

        // Convert the leaf nodes to graphs
        long leafNodeId[] = {0};

        //TreeUtils.inOrderSearch(tree.getRoot(), tree::getChildren)
        TreeUtils.leafStream(tree).forEach(op -> {
        	//System.out.println("Processing op: " + op);
            TreeNode<A> node = new TreeNodeImpl<>(tree, op);

            Y leafPreprocessed = leafPreprocessor.apply(op, preprocessedA);
            
            //X preprocessed = treePreprocessor.apply(op);
            G graph = getGraph.apply(leafPreprocessed);
            
            //G graph = getGraph.apply(preprocessed);//opToGraph.apply(op);
            
//            System.out.println();
//            System.out.println("Graph for " + key);
//            System.out.println(op);
//            System.out.println(graph);
            
            if(graph != null) {
                Entry<K, Long> e = new SimpleEntry<>(key, leafNodeId[0]);

                LeafInfo<Entry<K, Long>, A, X, Y, G> leafInfo = new LeafInfo<>(e, preprocessedA, leafPreprocessed, graph, node);
                
                keyToNodeIndexToInfo.put(key, leafNodeId[0], leafInfo);
                //System.out.println("Insert: " + e);
//                System.out.println("Graph: " + graph);
                //System.out.println("Graph of size: " + graph);

                index.put(e, graph);

                //index.printTree();
                leafNodeId[0]++;
            }
        });

    }


    public Table<K, A, ProblemNeighborhoodAware<BiMap<N, N>, ?>> lookupLeaf(Tree<A> tree, A leaf, BiMap<N, N> baseMatching) {
        Table<K, A, ProblemNeighborhoodAware<BiMap<N, N>, ?>> result = TreeMapper.createTable(true, true);

        // FIXME the user processing may require different preprocessing efforts than that of the view, so enhance to different preprocessors
        
        Y preprocessed = leafPreprocessor.apply(leaf, null);
        //G graph = opToGraph.apply(leaf);
        G graph = getGraph.apply(preprocessed);
        if(graph == null) {
        	// TODO Maybe we should raise an exception here
        	logger.warn("Graph was null for node: " + leaf);
        	//throw new RuntimeException("Graph was null for node: " + leaf);
        } else {
	        Multimap<Entry<K, Long>, BiMap<N, N>> matches = index.lookupX(graph, false);
	
	        for(Entry<Entry<K, Long>, BiMap<N, N>> e : matches.entries()) {
	            Entry<K, Long> f = e.getKey();
	            K viewKey = f.getKey();
	            Long leafIndex = f.getValue();
	
	            LeafInfo<Entry<K, Long>, A, X, Y, G> leafInfo = keyToNodeIndexToInfo.get(viewKey, leafIndex); 
	            
	            TreeNode<A> viewTreeNode = leafInfo.getNode();
	            A leafNode = viewTreeNode.getNode();
	            BiMap<N, N> matching = e.getValue();
	
	            ProblemNeighborhoodAware<BiMap<N, N>, ?> problems = new ProblemStaticSolutions<>(Collections.singleton(matching));
	
	            result.put(viewKey, leafNode, problems);
	        }	
        }
        return result;
    }

    /* (non-Javadoc)
	 * @see org.aksw.jena_sparql_api.query_containment.index.QueryContainmentIndex#match(A)
	 */
    @Override
	public Stream<Entry<K, TreeMapping<A, A, BiMap<N, N>, V>>> match(A userOp) {
        TreeMapper<K, X, X, A, A, BiMap<N, N>, BiMap<N, N>, V> treeMapper = new TreeMapper<>(
            key -> keyToNodeIndexToInfo.row(key).values().iterator().next().getMetaGraph(),
            getTree,
            //(preprocessed) -> preprocessed.getNormalizedOpTree(), 
            this::lookupLeaf,
            nodeMapperFactory,
            (m, c) -> MapUtils.mergeCompatible(m, c, HashBiMap::create),
            (m1, m2) -> MapUtils.mergeCompatible(m1, m2, HashBiMap::create),
            Objects::isNull,
            true,
            true
        );

        //A normUserOp = normalizer.apply(userOp);
        X preprocessedUserOp = treePreprocessor.apply(userOp);
        //A normUserOp = getNormalizedOp.apply(normUserContext);
        
        //Tree<A> userTree = TreeImpl.create(normUserOp, parentToChildren);//OpUtils.createTree((Op)normViewOp);
        Tree<A> userTree = getTree.apply(preprocessedUserOp);
        
        
        BiMap<N, N> baseMatching = HashBiMap.create();                
        Stream<Entry<K, TreeMapping<A, A, BiMap<N, N>, V>>> result = treeMapper.createMappings(baseMatching, userTree, preprocessedUserOp);

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


//    public static void main(String[] args) {
//        NodeMapperOpEquality nodeMapper = new NodeMapperOpEquality();
//        QueryContainmentIndexImpl<Node, DirectedGraph<Node, Triple>, Node, Op, Op> index = QueryContainmentIndexImpl.create(nodeMapper);
//
//        Op opA = Algebra.toQuadForm(Algebra.compile(QueryFactory.create("PREFIX : <http://ex.org/> SELECT ?s { ?s a :Airport }")));
//        Op opB = Algebra.toQuadForm(Algebra.compile(QueryFactory.create("PREFIX : <http://ex.org/> SELECT ?s { { SELECT DISTINCT ?s { ?s a ?t . FILTER(?t = :foo || ?t = :bar) } } UNION { ?x ?y ?z } }")));
//
//
//        index.put(NodeFactory.createURI("http://a"), opA);
//        index.put(NodeFactory.createURI("http://b"), opB);
//
//        index.match(opB).forEach(mr -> System.out.println("Match result: " + mr.getKey() + ": " + mr.getValue().getNodeMappings().row(mr.getValue().getaTree().getRoot())));
//    }
}

