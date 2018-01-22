package org.aksw.jena_sparql_api.query_containment.index;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collections.tagmap.TagMapSetTrie;
import org.aksw.commons.collections.tagmap.ValidationUtils;
import org.aksw.commons.graph.index.core.SubgraphIsomorphismIndex;
import org.aksw.commons.graph.index.jena.SubgraphIsomorphismIndexJena;
import org.aksw.commons.graph.index.jena.transform.QueryToJenaGraph;
import org.aksw.commons.jena.graph.GraphVarImpl;
import org.aksw.jena_sparql_api.algebra.transform.ExprTransformVariableOrder;
import org.aksw.jena_sparql_api.utils.CnfUtils;
import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.expr.E_StrContains;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.sparql.util.NodeUtils;
import org.jgrapht.DirectedGraph;

import com.codepoetics.protonpack.Indexed;
import com.codepoetics.protonpack.StreamUtils;
import com.codepoetics.protonpack.functions.TriFunction;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;


public class ExpressionMapper {
	
	public static Set<Expr> clauseHandler(Set<Expr> residualClause, Expr viewExpr, Expr userExpr) {
		Set<Expr> result = residualClause;
		
		// Handle regex
		if(userExpr instanceof E_StrContains) {
			E_StrContains aExpr = (E_StrContains)viewExpr;
			E_StrContains bExpr = (E_StrContains)userExpr;
	
			Expr a = aExpr.getArg2();
			Expr b = bExpr.getArg2();
	
			String aStr = getString(a);
			String bStr = getString(b);
			
			
			if(aStr != null && bStr != null && bStr.contains(aStr)) {
				result.add(new E_StrContains(bExpr.getArg1(), b));
			} else {
				result = null;
			}
		}

		return result;
	}
	
	public static Multimap<BiMap<Node, Node>, Set<Set<Expr>>> computeResidualExpressions(BiMap<Node, Node> baseIso, Expr view, Expr user) {
		view = ExprTransformer.transform(new ExprTransformVariableOrder(), view);
		user = ExprTransformer.transform(new ExprTransformVariableOrder(), user);

//		System.out.println("Normalization: " + view + " vs " + user);
		
		Set<Set<Expr>> viewCnf = CnfUtils.toSetCnf(view);
		Set<Set<Expr>> userCnf = CnfUtils.toSetCnf(user);
		
		Multimap<BiMap<Node, Node>, Set<Set<Expr>>> result = computeResidualExpressions(baseIso, viewCnf, userCnf);
		return result;
	}
	
	public static Multimap<BiMap<Node, Node>, Set<Set<Expr>>> computeResidualExpressions(BiMap<Node, Node> baseIso, Set<Set<Expr>> viewCnf, Set<Set<Expr>> userCnf) {

//		System.out.println("viewNf: " + viewDnf);
//		System.out.println("userNf: " + userDnf);
		//if(viewDnf.size() >= 1 || userDnf.)
		
		
		SubgraphIsomorphismIndex<Long, DirectedGraph<Node, Triple>, Node> sii = createIndex();
		//Predicate<Expr> isVar = (e) -> e.isVariable();
        Supplier<Supplier<Node>> ssn = () -> { int[] x = {0}; return () -> NodeFactory.createBlankNode("_" + x[0]++); };
        BiFunction<Expr, BiMap<Node, Node>, Expr> applyExprIso = (e, iso) -> e.applyNodeTransform(new NodeTransformRenameMap(iso));
        
		
		Multimap<BiMap<Node, Node>, Set<Set<Expr>>> result = computeResidualCnf(
				Node::isVariable,
				applyExprIso,
				sii,
				ssn,
				ExpressionMapper::clauseHandler,
				
				ExpressionMapper::toOpGraph,
				OpGraph::getJGraphTGraph,
				OpGraph::getNodeToExpr,
				
				
				baseIso,
				viewCnf,
				userCnf);
		return result;
	}
	
	public static String getString(Expr expr) {
		String result = null;
		
		if(expr.isConstant()) {
			NodeValue nv = expr.getConstant();
			
			if(nv.isString()) {
				result = nv.getString();
			}
		}

		return result;
	}
	
	public static OpGraph toOpGraph(Set<Expr> conjunction, BiMap<Node, Expr> nodeToExpr, Supplier<Node> nodeSupplier) {
		Node clauseNode = nodeSupplier.get();
		
        //BiMap<Node, Expr> nodeToExpr = HashBiMap.create(); //new HashMap<>();
		Graph graph = new GraphVarImpl();
        QueryToJenaGraph.clauseToGraph(clauseNode, graph, nodeToExpr, conjunction, nodeSupplier);

        OpGraph result = new OpGraph(graph, nodeToExpr, HashBiMap.create());
        return result;
	}

	public static SubgraphIsomorphismIndex<Long, DirectedGraph<Node, Triple>, Node> createIndex() {
		return createIndex(false);
	}
	
	
	public static <K> SubgraphIsomorphismIndex<K, DirectedGraph<Node, Triple>, Node> createIndex(boolean validate) {
		// Create an index entry for each view clause
		// Then, perform a lookup with each user clause whether there is an entry.
		// If there is none, the view cannot be used to answer the query
		
		SubgraphIsomorphismIndex<K, DirectedGraph<Node, Triple>, Node> siiTreeTags = SubgraphIsomorphismIndexJena.create();

        SubgraphIsomorphismIndex<K, DirectedGraph<Node, Triple>, Node> sii;

        if(validate) {
			//SubgraphIsomorphismIndex<Long, DirectedGraph<Node, Triple>, Node> siiFlat = SubgraphIsomorphismIndexJena.createFlat();
	        SubgraphIsomorphismIndex<K, DirectedGraph<Node, Triple>, Node> siiTagBased = SubgraphIsomorphismIndexJena.createTagBased(new TagMapSetTrie<>(NodeUtils::compareRDFTerms));
	
	        SubgraphIsomorphismIndex<K, DirectedGraph<Node, Triple>, Node> siiValidating = ValidationUtils.createValidatingProxy(SubgraphIsomorphismIndex.class, siiTreeTags, siiTagBased);
	        sii = siiValidating;
        } else {
        	sii = siiTreeTags;
        }
        
        return sii;
	}
	

	public static <T, K, U, M extends Map<K, U>> Collector<T, ?, M> toMap(
			Function<? super T, ? extends K> keyMapper,
			Function<? super T, ? extends U> valueMapper,
			Supplier<M> mapSupplier) {
		Collector<T, ?, M> result = Collectors.toMap(
				keyMapper,
				valueMapper,
				(u, v) -> { throw new RuntimeException("should not happen"); },
				mapSupplier);
		return result;
	}

	public static <T, K, U> Collector<T, ?, Map<K, U>> toLinkedHashMap(
			Function<? super T, ? extends K> keyMapper,
			Function<? super T, ? extends U> valueMapper) {
		Collector<T, ?, Map<K, U>> result = Collectors.toMap(
				keyMapper,
				valueMapper,
				(u, v) -> { throw new RuntimeException("should not happen"); },
				LinkedHashMap::new);
		return result;
	}
	
	public static Set<Expr> commonConstraintsInDnf(Set<Set<Expr>> dnf) {
		Map<Expr, Integer> exprToCount = new HashMap<>();
		
		for(Set<Expr> clause : dnf) {
			for(Expr expr : clause) {
				exprToCount.compute(expr, (k, v) -> v == null ? 1 : v + 1);
			}
		}

		int n = dnf.size();
		
		Set<Expr> result = exprToCount.entrySet().stream()
				.filter(e -> e.getValue() == n)
				.map(Entry::getKey)
				.collect(Collectors.toSet());

		return result;
	}
	
	
	/**
	 * The residual expression is expressed over the variables of the view
	 * 
	 * @param baseIsoViewToUser
	 * @param viewCnf
	 * @param userCnf
	 * @return
	 */
	public static <E, N, T, O, G> Multimap<BiMap<N, N>, Set<Set<E>>> computeResidualCnf(
			// Basic Node and Expr handling
			Predicate<? super N> isVar,
			BiFunction<? super E, BiMap<N, N>, E> applyExprIso,
			
			// Indexing
			SubgraphIsomorphismIndex<Long, G, N> sii,
			Supplier<Supplier<N>> ssn,
			
			// Expression post-processing
			TriFunction<Set<E>, E,  E, Set<E>> clauseHandler,
					
			// Graph conversion
			TriFunction<Set<E>, BiMap<N, E>, Supplier<N>, O> toOpGraph,
			Function<O, G> opGraphToGraph,
			Function<O, BiMap<N, E>> opGraphToNodeToExpr,
			
			// Actual expressions
			BiMap<N, N> baseIsoViewToUser,
			Set<Set<E>> viewCnf,
			Set<Set<E>> userCnf)
	{        
        Supplier<N> userNodeSupplier = ssn.get();
        Supplier<N> viewNodeSupplier = ssn.get();
        BiMap<N, E> userNodeToExpr = HashBiMap.create();
        BiMap<N, E> viewNodeToExpr = HashBiMap.create();
        
        BiMap<N, N> baseIsoUserToView = baseIsoViewToUser.inverse();
        
        
        //HashMap<Integer, Set<Expr>> idToViewClause = new LinkedHashMap<>();
        Map<Long, Set<E>> idToUserClause = StreamUtils.zipWithIndex(userCnf.stream())
        		.collect(toLinkedHashMap(Indexed::getIndex, Indexed::getValue));
      //viewExpr.applyNodeTransform(new NodeTransformRenameMap(viewToUser));
        Map<Long, O> idToUserOpGraph = new HashMap<>();

        for(Entry<Long, Set<E>> userClauseI : idToUserClause.entrySet()) {
        	long id = userClauseI.getKey();
        	Set<E> userClause = userClauseI.getValue();
        	
        	O opGraph = toOpGraph.apply(userClause, userNodeToExpr, userNodeSupplier);        	
        	G g = opGraphToGraph.apply(opGraph);
        	//DirectedGraph<Node, Triple>
        	
        	
        	sii.put(id, g);
        	idToUserOpGraph.put(id, opGraph);
        }

//        Map<Long, Set<E>> idToViewClause = StreamUtils.zipWithIndex(viewCnf.stream())
//        		.collect(toLinkedHashMap(Indexed::getIndex, Indexed::getValue));

        
        
        // For every view clause there has to be a more restrictive user clause

        
        // TODO Sort view clauses by size (we expect those to yield fewest isos)
        List<Set<E>> viewClausesBySize = new ArrayList<>(viewCnf);
        Collections.sort(viewClausesBySize, (a, b) -> Integer.compare(a.size(), b.size()));
        
        List<List<Entry<Long, BiMap<N, N>>>> candList = new ArrayList<>();
        
        List<O> viewClauseIdToOpGraph = new ArrayList<>(viewClausesBySize.size());
        
        
        // For each view clause retrieve the set of candidate user clauses
        for(int i = 0; i < viewClausesBySize.size(); ++i) {

        	Set<E> viewClause = viewClausesBySize.get(i);
        	O viewOpGraph = toOpGraph.apply(viewClause, viewNodeToExpr, viewNodeSupplier);
        	viewClauseIdToOpGraph.add(viewOpGraph);
        	
        	//BiMap<Node, Expr> viewNodeToExpr = viewOpGraph.getNodeToExpr();
        	//BiMap<Expr, Node> viewExprToNode = viewNodeToExpr.inverse();
        	
        	G g = opGraphToGraph.apply(viewOpGraph);

        	//System.out.println("Lookup with clause: " + viewClause);
        	
        	// This returns candidate clauses of the query that may be more restrictive than those
        	// of the view
        	Multimap<Long, BiMap<N, N>> userClauseCands = sii.lookupX(g, false, baseIsoUserToView);

        	candList.add(new ArrayList<>(userClauseCands.entries()));
        }
        
        
        Stream<Entry<List<Entry<Long, BiMap<N, N>>>, BiMap<N, N>>> compats = Lists.cartesianProduct(candList).stream().map(items -> {
        	BiMap<N, N> total = HashBiMap.create();
        	for(Entry<Long, BiMap<N, N>> entry : items) {
        		BiMap<N, N> contrib = entry.getValue();
        		try {
        			total.putAll(contrib);
        		} catch(Exception e) {
        			// Mapping is incompatible
        			total = null;
        			break;
        		}
        	}
        	
        	
        	Entry<List<Entry<Long, BiMap<N, N>>>, BiMap<N, N>> r = total == null
        			? null
        			: new SimpleEntry<>(items, total);        	

        	return r;
        }).filter(x -> x != null);
        
        
        
        List<Entry<List<Entry<Long, BiMap<N, N>>>, BiMap<N, N>>> l = compats.collect(Collectors.toList());
        //System.out.println("Cartesian product of isos yeld " + l.size() + " compatible mappings");
        	
        	//StateCartesian<Set<Expr>, Long, BiMap<Node, Node>> cart = null;
        	
        	
        Multimap<BiMap<N, N>, Set<Set<E>>> result = ArrayListMultimap.create();

        //compats.forEach(compat -> {
        for(Entry<List<Entry<Long, BiMap<N, N>>>, BiMap<N, N>> compat : l) {
        	
        	BiMap<N, N> rawIso = compat.getValue();
        	
        	BiMap<N, N> totalIso = rawIso.inverse().entrySet().stream()
        			.filter(e -> isVar.test(e.getKey()) || isVar.test(e.getValue()))
        			.collect(toMap(Entry::getKey, Entry::getValue, HashBiMap::create));
        	
            Set<Set<E>> residualCnf = new LinkedHashSet<>();
            Set<Long> coveredUserClauseIds = new LinkedHashSet<>();

        	for(int i = 0; i < viewClausesBySize.size(); ++i) {
        		Set<E> viewClause = viewClausesBySize.get(i);
        		System.out.println("viewClause: " + viewClause);
	        		
	        	Entry<Long, BiMap<N, N>> e = compat.getKey().get(i);
	        	
	        	O viewOpGraph = viewClauseIdToOpGraph.get(i); //.getNodeToExpr();
	        	Map<? super N, ? extends E> xviewNodeToExpr = opGraphToNodeToExpr.apply(viewOpGraph);
	        	
	        	//boolean foundMatch = false;
	//        	for(Entry<Long, BiMap<Node, Node>> e : cands.entries()) {
	        		
	        	//for(Entry<Long, BiMap<Node, Node>> e : userCands) {
        		long userId = e.getKey();
        		Set<E> userClause = idToUserClause.get(userId);
        		BiMap<N, N> userToView = e.getValue();
        		BiMap<N, N> viewToUser = userToView.inverse(); 

        		System.out.println("  Candidate user clause: " + idToUserClause.get(userId));

        		
        		O userOpGraph = idToUserOpGraph.get(userId);
        		BiMap<N, E> xuserNodeToExpr = opGraphToNodeToExpr.apply(userOpGraph); //.getNodeToExpr();
        		BiMap<E, N> userExprToNode = xuserNodeToExpr.inverse();
        		
        		Set<E> residualClause = new HashSet<>();
        		for(E userExpr : userClause) {
        			
        			
        			N userNode = userExprToNode.get(userExpr);
        			N viewNode = userToView.getOrDefault(userNode, userNode);
        			
        			if(viewNode == null) {
        				//residualClause.add(userExpr);
        				residualClause = null;
        			} else {
	        			
        				
        				
            			E viewExpr = xviewNodeToExpr.get(viewNode);
	
	        			E renamedViewExpr = applyExprIso.apply(viewExpr, viewToUser);

	        			if(Objects.equals(renamedViewExpr, userExpr)) {
	        				residualClause.add(viewExpr);
	        			} else {
	        				residualClause = clauseHandler.apply(residualClause, renamedViewExpr, userExpr);	        			
	        			}
	        			
	        			
	        			if(residualClause == null) {
	        				break;
	        			}
	        			
        			}
        			
        			if(residualClause == null) {
        				residualCnf = null;
        				break;
        			}        			
        		}

        		if(residualClause != null) {
	        		coveredUserClauseIds.add(userId);
	    			
	    			// If the residualClause equals the view clause, we can omit it from the final cnf
	    			// as the query uses the exact same constraints as the view
	    			if(!residualClause.equals(viewClause)) {
	    				residualCnf.add(residualClause);
	    			}
        		} else {
        			break;
        		}
        		// If for a user clause there is no corresponding view clause, we cannot use
        		// the view at all
//        		if(!foundMatch) {
//        			residualCnf = null;
//        			break;
//        		}
        		
    			//residualCnf.add(residualClause);
        	}
        	
        	if(residualCnf != null) {
        		
        		// FIXME What to do if these expressions use variables that have not been mapped???
        		// Possible resolutions
        		// (a) reject the match
        		// (b) return the non-covered user clauses separately from the resdual expressions
        		// Probably (b) is best
        		
                // Add all query clauses not covered by the view 
            	for(Entry<Long, Set<E>> e : idToUserClause.entrySet()) {
            		Long userId = e.getKey();
            		if(coveredUserClauseIds.contains(userId)) {
            			continue;
            		}
            		
            		Set<E> nonCoveredClause = e.getValue();
            		residualCnf.add(nonCoveredClause);
            	}
	
        		
        		
        		result.put(totalIso, residualCnf);
//        		result = residualCnf;
        		//break;
        	}        	        	
        }
        
        return result;
	}
	

	public static void main(String[] args) {
		Expr view = ExprUtils.parse("1 + ?y * ?x = ?h && contains(?i, 'foo') && (?a = 1 || ?a = 2 || ?a = 3)");
		Expr user = ExprUtils.parse("(?a * ?b) + 1 = ?z && contains(?j, 'foobar') && (?o = 1 || ?o = 2) && ?a = 4");
		
		// TODO Should the result include identity mappings? Probably yes for the sake of
		// future compatibility checking 
		//Expr view = ExprUtils.parse("?i = ?y");
		//Expr user = ExprUtils.parse("?x = ?y && ?a = ?b");
		
		// Expected: [(contains(?j, 'foobar'), !?o = ?3)]
		
		
		Multimap<BiMap<Node, Node>, Set<Set<Expr>>> residualDnf = computeResidualExpressions(HashBiMap.create(), view, user);
		System.out.println("Residual DNF: " + residualDnf);
	}
}




//TreeContainmentIndexImpl.queryToOpGraph,
//DirectedGraph<Node, >
//Graph viewGraph = new GraphVarImpl();
//BiMap<Node, Expr> viewNodeToExpr = HashBiMap.create();//new HashMap<>();
//
//
//System.out.println("View: " + viewDnf);
//System.out.println("User: " + user);
//
//
//QueryToJenaGraph.dnfToGraph(viewGraph, viewNodeToExpr, viewDnf, ssn.get());
//
//
////userNodeToExpr.entrySet().forEach(x -> System.out.println("" + x));
////Map<Expr, Node> userExprToNode = userNodeToExpr.entrySet().stream().collect(
////		Collectors.toMap(Entry::getValue, Entry::getKey, (u, v) -> { throw new RuntimeException("should not happen"); }, IdentityHashMap::new));
//System.out.println(userGraph);
//BiMap<Expr, Node> userExprToNode = userNodeToExpr.inverse();        
//System.out.println(viewGraph);
//DirectedGraph<Node, Triple> userJGraph = new PseudoGraphJenaGraph(userGraph);
//
//
//DirectedGraph<Node, Triple> viewJGraph = new PseudoGraphJenaGraph(viewGraph);       
//IsoMatcher<DirectedGraph<Node, Triple>, Node> isoMatcher = new IsoMatcherImpl<>(SubgraphIsomorphismIndexJena::createNodeComparator, SubgraphIsomorphismIndexJena::createEdgeComparator);
//Iterable<BiMap<Node, Node>> matches = isoMatcher.match(baseIso, viewJGraph, userJGraph);
//
//System.out.println("Begin of matches:");
//matches.forEach(viewToUser -> {
//	System.out.println("Match: " + viewToUser);
//	
//	BiMap<Node, Node> userToView = viewToUser.inverse();
//	// Perform post processing of the matches
//	
//	// Perform bottom up traversal of the view expression?
//	
//	for(Set<Expr> userClause : user) {
//		Set<Expr> residualClause = new HashSet<>();
//		for(Expr userExpr : userClause) {
//			
//			Node userNode = userExprToNode.get(userExpr);
//			Node viewNode = userToView.get(userNode);
//			
//			if(viewNode == null) {
//				residualClause.add(userExpr);
//			} else {
//    			
//				
//    			Expr viewExpr = viewNodeToExpr.get(userNode);
//
//    			Expr renamedViewExpr = viewExpr.applyNodeTransform(new NodeTransformRenameMap(viewToUser));
//
//    			// Handle regex
//    			if(userExpr instanceof E_StrContains) {
//    				E_StrContains aExpr = (E_StrContains)renamedViewExpr;
//    				E_StrContains bExpr = (E_StrContains)userExpr;
//
////        				if(Objects.equals(aExpr, bExpr)) {
////        					r = Expr.
////        				}
//    				
//    				Expr a = aExpr.getArg2();
//    				Expr b = bExpr.getArg2();
//
//    				String aStr = getString(a);
//    				String bStr = getString(b);
//    				
//    				
//    				if(aStr != null && bStr != null && bStr.contains(aStr)) {
//    					residualClause.add(new E_StrContains(bExpr.getArg1(), b));
//    				} else {
//    					residualClause = null;
//    					break;
//    				}
//    			}
//			}
////			Stream<Expr> nodeStream = BottomUpTreeTraversals.postOrder(viewExpr, org.aksw.jena_sparql_api.utils.ExprUtils::getSubExprs);
////			
////			nodeStream.forEach();
//		}
//		System.out.println("Residual clause: " + residualClause);
//	}
//	
//	//BottomUpTreeTraversals
//	
//});
//System.out.println("End of matches");
//

