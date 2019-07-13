package org.aksw.jena_sparql_api.query_containment.index;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.aksw.combinatorics.solvers.ProblemNeighborhoodAware;
import org.aksw.combinatorics.solvers.ProblemStaticSolutions;
import org.aksw.commons.collections.MapUtils;
import org.aksw.commons.collections.collectors.CollectorUtils;
import org.aksw.commons.collections.trees.Tree;
import org.aksw.commons.collections.trees.TreeNode;
import org.aksw.commons.collections.trees.TreeNodeImpl;
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
import org.apache.jena.sparql.core.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 *
 * @param <K> Key type for views
 * @param <TC> TreeContext type (object to hold meta information about algebra objects of type A)
 * @param <LC> LeafContext type (matching of expression works leaf based; leaf context may e.g.g comprise graph representations)
 * @param <G> The graph type (the leaf context must provide this)
 * @param <V> The node type in the graph which will be used in graph isomorphisms (e.g. jena Nodes)
 * @param <I> The containment mapping type (e.g. Jena Vars)
 * @param <A> The type for view algebra expressions
 * @param <R> Type for residual information (obtained during the mapping phase)
 */
public class QueryContainmentIndexImpl<K, TC, LC, G, L, V, A, R, TM extends TreeMapping<A, A, BiMap<V, V>, R>>
	implements QueryContainmentIndex<K, V, A, R, TM>
{	
	private static final Logger logger = LoggerFactory.getLogger(QueryContainmentIndexImpl.class);

	
    //protected Function<? super A, A> normalizer;
    
    protected Function<? super A, TC> treePreprocessor;    
    
    // TODO Group all TC-based functions into an 'accessor' style interface
    protected Function<? super TC, Map<A, LC>> preprocessLeafs;
    
    protected Function<? super TC, ? extends Tree<A>> getTree;
    
    protected Function<? super TC, ? extends A> getNormalizedOp;
    
    //protected BiFunction<? super A, ? super X, Y> leafPreprocessor;
    protected Function<? super LC, G> getGraph;

    // We do not map inputs directly to graphs, but to an intermediate object of type X from which the graph for indexing is obtained
    //protected Function<? super A, G> opToGraph;

    //protected Function<A, List<A>> parentToChildren;

    protected Map<K, TC> keyToTreeContext = new LinkedHashMap<>();
    protected SubgraphIsomorphismIndex<Entry<K, Long>, G, L> index;
    
    // This function transforms yields a containment mapping from an sub graph isomorphism
    // Essentially it returns a mapping of only the variables
    protected Function<? super BiMap<L, L>, ? extends BiMap<V, V>> graphIsoToContainmentMapping;
    protected Function<? super BiMap<V, V>, ? extends BiMap<L, L>> containmentMappingToSolutionContribution;
    
    
    
    //protected TriFunction<? super A, ? super A, TreeMapping<A, A, BiMap<N, N>, V>, ? extends Entry<BiMap<N, N>, V>> nodeMapper;
    
    //protected BiFunction<? super X, ?super X, ? extends NodeMapper<A, A, BiMap<N, N>, V>> nodeMapperFactory;
    //protected BiFunction<? super X, ? super X, ? extends NodeMapper<? super A, ? super A, ? super BiMap<Node, Node>, ? extends V>> nodeMapperFactory;

    
    protected TriFunction<? super TC, ? super TC, ? super Table<A, A, BiMap<L, L>>, ? extends NodeMapper<A, A, BiMap<V, V>, BiMap<V, V>, R>> nodeMapperFactory;
    
    
    //protected NodeMapperFactory<A, A, BiMap<N, N>, BiMap<N, N>, V> nodeMapperFactory;
    
    
    protected Table<K, Long, LeafInfo<Entry<K, Long>, A, TC, LC, G>> keyToNodeIndexToInfo = HashBasedTable.create();


    protected TreeMappingFactory<A, A, BiMap<V, V>, R, ? extends TM> treeMappingFactory;
    

    public static org.jgrapht.Graph<Node, Triple> queryToJGraphT(Op op) {
    	org.jgrapht.Graph<Node, Triple> result = null;

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
            
            result = new OpGraph(q2g.getGraph(), q2g.getNodeToExpr(), q2g.getNodeToQuad());
        }

        return result;    	    	
    }
    
    
    /* (non-Javadoc)
	 * @see org.aksw.jena_sparql_api.query_containment.index.QueryContainmentIndex#getIndex()
	 */
//    @Override
//	public SubgraphIsomorphismIndex<Entry<K, Long>, G, L> getIndex() {
//		return index;
//	}

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

    // Use SparqlQueryContainmentIndex.create()
    @Deprecated
    public static <K, R> QueryContainmentIndex<K, Var, Op, R, TreeMapping<Op, Op, BiMap<Var, Var>, R>> createOld(
    		SubgraphIsomorphismIndex<Entry<K, Long>, org.jgrapht.Graph<Node, Triple>, Node> sii,
    		TriFunction<? super OpContext, ? super OpContext, ? super Table<Op, Op, BiMap<Node, Node>>, ? extends NodeMapper<Op, Op, BiMap<Var, Var>, BiMap<Var, Var>, R>> nodeMapperFactory) {

    	
//(a, b, v, r) -> new TreeMapping<OpContext, OpContext, BiMap<Var, Var>, R>(a, b, v, r)//TreeMapping<OpContext, OpContext, BiMap<Var, Var>, R>::new

    	//TreeMapping<Op, Op, BiMap<Var, Var>, R>
    		
        TreeMappingFactory<Op, Op, BiMap<Var, Var>, R, TreeMapping<Op, Op, BiMap<Var, Var>, R>> treeMappingFactory = TreeMapping<Op, Op, BiMap<Var, Var>, R>::new;
        //TreeMappingFactory<Op, Op, BiMap<Var, Var>, R, TreeMapping<Op, Op, BiMap<Var, Var>, R>> treeMappingFactory = (a, b, s, r) -> new TreeMapping<Op, Op, BiMap<Var, Var>, R>(a, b, s, r);

        
    		QueryContainmentIndex<K, Var, Op, R, TreeMapping<Op, Op, BiMap<Var, Var>, R>> result =
        		new QueryContainmentIndexImpl<K, OpContext, OpGraph, org.jgrapht.Graph<Node, Triple>, Node, Var, Op, R, TreeMapping<Op, Op, BiMap<Var, Var>, R>>(
        				OpContext::create,
        				OpContext::getNormalizedOp,
        				OpContext::getLeafOpGraphs,
		                OpContext::getNormalizedOpTree,
		                
		                OpGraph::getJGraphTGraph,
		                //opGraph -> opGraph.getJGraphTGraph(),//opContext.getOpAsGraph().getJGraphTGraph(),

		                sii,

		                QueryContainmentIndexImpl::retainVarMappingsOnlyAsVars,
		                QueryContainmentIndexImpl::toNodes,
		                
		                nodeMapperFactory,
		                treeMappingFactory
		                //(a, b, v, r) -> new TreeMapping<OpContext, OpContext, BiMap<Var, Var>, R>(a, b, v, r)
        				);
        return result;
    }

    
    public static BiMap<Var, Var> retainVarMappingsOnlyAsVars(BiMap<Node, Node> iso) {
    	return toVars(retainVarMappingsOnly(iso));
    }

    public static BiMap<Node, Node> retainVarMappingsOnly(BiMap<Node, Node> iso) {
    	BiMap<Node, Node> result = iso.entrySet().stream()
    		.filter(e -> e.getKey().isVariable())
    		.collect(CollectorUtils.toMap(Entry::getKey, Entry::getValue, HashBiMap::create));
    	return result;
    }

    
    public static BiMap<Var, Var> toVars(BiMap<Node, Node> iso) {
    	BiMap<Var, Var> result = iso.entrySet().stream()
        		.filter(e -> e.getKey().isVariable() && e.getValue().isVariable())
        		.collect(CollectorUtils.toMap(e -> (Var)e.getKey(), e -> (Var)e.getValue(), HashBiMap::create));
        	return result;    	
    }
    
    public static BiMap<Node, Node> toNodes(BiMap<Var, Var> containmentMapping) {
    	BiMap<Node, Node> result = containmentMapping.entrySet().stream()
        		.collect(CollectorUtils.toMap(Entry::getKey, Entry::getValue, HashBiMap::create));
        	return result;    	
    }
    
    
    
    public QueryContainmentIndexImpl(
            Function<? super A, TC> preprocessor,
    		Function<? super TC, ? extends A> getNormalizedOp,
            Function<? super TC, Map<A, LC>> preprocessLeafs,
            Function<? super TC, Tree<A>> getTree,            		
            //BiFunction<? super A, ? super X, Y> leafPreprocessor,
            Function<? super LC, G> getGraph,
            
            SubgraphIsomorphismIndex<Entry<K, Long>, G, L> index,
            Function<? super BiMap<L, L>, ? extends BiMap<V, V>> graphIsoToContainmentMapping,
            Function<? super BiMap<V, V>, ? extends BiMap<L, L>> containmentMappingToSolutionContribution,            
            
            		
            TriFunction<? super TC, ? super TC, ? super Table<A, A, BiMap<L, L>>, ? extends NodeMapper<A, A, BiMap<V, V>, BiMap<V, V>, R>> nodeMapperFactory,
            TreeMappingFactory<A, A, BiMap<V, V>, R, ? extends TM> treeMappingFactory
    ) {
        super();
        this.treePreprocessor = preprocessor;
        this.getNormalizedOp = getNormalizedOp;
        this.preprocessLeafs = preprocessLeafs;
        this.getTree = getTree;
        //this.leafPreprocessor = leafPreprocessor;
        this.getGraph = getGraph;
        //this.metaGraphToGraph = metaGraphToGraph;        
        this.index = index;
        this.graphIsoToContainmentMapping = graphIsoToContainmentMapping;
        this.containmentMappingToSolutionContribution = containmentMappingToSolutionContribution;
        
        this.nodeMapperFactory = nodeMapperFactory;
        this.treeMappingFactory = treeMappingFactory;        
    }

    
    public A get(Object key) {
    	TC treeContext = keyToTreeContext.get(key);
    	A result = getNormalizedOp.apply(treeContext);
    	return result;
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
        
    	// Remove any prior binding of the key
    	remove(key);
    	
    	TC treeContextA = treePreprocessor.apply(viewOp);
    	keyToTreeContext.put(key, treeContextA);
    	
    	Tree<A> tree = getTree.apply(treeContextA);

        long leafNodeId = 0;
    	
    	Map<A, LC> leafContextMap = preprocessLeafs.apply(treeContextA);
    	for(Entry<A, LC> leafContextEntry : leafContextMap.entrySet()) {
    		A op = leafContextEntry.getKey();
            TreeNode<A> node = new TreeNodeImpl<>(tree, op);

            LC leafContext = leafContextEntry.getValue();
            //Y leafPreprocessed = leafPreprocessor.apply(op, preprocessedA);
            
            //X preprocessed = treePreprocessor.apply(op);
            G graph = getGraph.apply(leafContext);
            
            //G graph = getGraph.apply(preprocessed);//opToGraph.apply(op);
            
//            System.out.println();
//            System.out.println("Graph for " + key);
//            System.out.println(op);
//            System.out.println(graph);
            
            if(graph != null) {
                Entry<K, Long> e = new SimpleEntry<>(key, leafNodeId);

                LeafInfo<Entry<K, Long>, A, TC, LC, G> leafInfo = new LeafInfo<>(e, treeContextA, leafContext, graph, node);
                
                keyToNodeIndexToInfo.put(key, leafNodeId, leafInfo);
                //System.out.println("Insert: " + e);
//                System.out.println("Graph: " + graph);
                //System.out.println("Graph of size: " + graph);

                index.put(e, graph);

                //index.printTree();
                leafNodeId++;
            }
    		
    	}
    	
    	
        //CA contextA = analyzer.apply(normViewOp);
        
        //Tree<A> tree = TreeImpl.create(normViewOp, parentToChildren);//OpUtils.createTree((Op)normViewOp);

        // Convert the leaf nodes to graphs

        //Stream<>getLeafs.apply(viewOp, preprocessedA);
        
        //TreeUtils.inOrderSearch(tree.getRoot(), tree::getChildren)
//        TreeUtils.leafStream(tree).forEach(op -> {
//        	//System.out.println("Processing op: " + op);
//            TreeNode<A> node = new TreeNodeImpl<>(tree, op);
//
//            Y leafPreprocessed = leafPreprocessor.apply(op, preprocessedA);
//            
//            //X preprocessed = treePreprocessor.apply(op);
//            G graph = getGraph.apply(leafPreprocessed);
//            
//            //G graph = getGraph.apply(preprocessed);//opToGraph.apply(op);
//            
////            System.out.println();
////            System.out.println("Graph for " + key);
////            System.out.println(op);
////            System.out.println(graph);
//            
//            if(graph != null) {
//                Entry<K, Long> e = new SimpleEntry<>(key, leafNodeId[0]);
//
//                LeafInfo<Entry<K, Long>, A, X, Y, G> leafInfo = new LeafInfo<>(e, preprocessedA, leafPreprocessed, graph, node);
//                
//                keyToNodeIndexToInfo.put(key, leafNodeId[0], leafInfo);
//                //System.out.println("Insert: " + e);
////                System.out.println("Graph: " + graph);
//                //System.out.println("Graph of size: " + graph);
//
//                index.put(e, graph);
//
//                //index.printTree();
//                leafNodeId[0]++;
//            }
//        });

    }


    // Tree<A> tree
    public Table<K, A, ProblemNeighborhoodAware<BiMap<L, L>, ?>> lookupLeaf(TC userContext, Entry<A, LC> userLeaf, BiMap<V, V> baseMatching) {
        
    	// FIXME Make identities configurable
    	Table<K, A, ProblemNeighborhoodAware<BiMap<L, L>, ?>> result = TreeMapper.createTable(true, true);

        // FIXME the user processing may require different preprocessing efforts than that of the view, so enhance to different preprocessors
        
        //Map<A, Y> leafMap = preprocessLeafs.apply(userContext);
        
        //Y preprocessed = leafPreprocessor.apply(userLeaf, null);
        //G graph = opToGraph.apply(leaf);
        LC preprocessed = userLeaf.getValue();//leafMap.get(userLeaf);
        G graph = getGraph.apply(preprocessed);
        if(graph == null) {
        	// TODO Maybe we should raise an exception here
        	logger.warn("Graph was null for node: " + userLeaf);
        	//throw new RuntimeException("Graph was null for node: " + leaf);
        } else {
	        Multimap<Entry<K, Long>, BiMap<L, L>> matches = index.lookup(graph, false);
	
	        for(Entry<Entry<K, Long>, BiMap<L, L>> e : matches.entries()) {
	            Entry<K, Long> f = e.getKey();
	            K viewKey = f.getKey();
	            Long leafIndex = f.getValue();
	
	            LeafInfo<Entry<K, Long>, A, TC, LC, G> leafInfo = keyToNodeIndexToInfo.get(viewKey, leafIndex); 
	            
	            TreeNode<A> viewTreeNode = leafInfo.getNode();
	            A leafNode = viewTreeNode.getNode();
	            BiMap<L, L> rawMatching = e.getValue();
	
	            // Filter solutions
	            //BiMap<N, N> matching = matchingCleaner.apply(rawMatching);
	            
	            // NOTE We are NOT filtering out non-variable node here, because we want to pass the complete matching for a leaf on to the
	            // nodeMapper
	            
	            // However, for non-leaf nodes, the matching needs to be converted to the containmentMapping
	            
	            ProblemNeighborhoodAware<BiMap<L, L>, ?> problems = new ProblemStaticSolutions<>(Collections.singleton(rawMatching));
	
	            result.put(viewKey, leafNode, problems);
	        }	
        }
        return result;
    }

    /* (non-Javadoc)
	 * @see org.aksw.jena_sparql_api.query_containment.index.QueryContainmentIndex#match(A)
	 */
    @Override
    //TreeMapping<A, A, BiMap<V, V>, R>
	public Stream<Entry<K, TM>> match(A userOp) {
        TreeMapper<K, TC, TC, LC, LC, A, A, BiMap<L, L>, BiMap<V, V>, BiMap<V, V>, R, TM> treeMapper = new TreeMapper<K, TC, TC, LC, LC, A, A, BiMap<L, L>, BiMap<V, V>, BiMap<V, V>, R, TM>(
            key -> keyToNodeIndexToInfo.row(key).values().iterator().next().getMetaGraph(),
            getTree,
            
            getTree,
            preprocessLeafs,
            //(preprocessed) -> preprocessed.getNormalizedOpTree(), 
            this::lookupLeaf,
            nodeMapperFactory,
            
            graphIsoToContainmentMapping,
            
            (m, c) -> MapUtils.mergeCompatible(m, c, HashBiMap::create),
            (m1, m2) -> MapUtils.mergeCompatible(m1, m2, HashBiMap::create),
            Objects::isNull,
            
            treeMappingFactory,
            
            true,
            true
        );

        //A normUserOp = normalizer.apply(userOp);
        TC preprocessedUserOp = treePreprocessor.apply(userOp);
        //A normUserOp = getNormalizedOp.apply(normUserContext);
        
        //Tree<A> userTree = TreeImpl.create(normUserOp, parentToChildren);//OpUtils.createTree((Op)normViewOp);
        //Tree<A> userTree = getTree.apply(preprocessedUserOp);
        
        
        BiMap<V, V> baseMatching = HashBiMap.create();                
        Stream<Entry<K, TM>> result = treeMapper.createMappings(baseMatching, preprocessedUserOp);

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

