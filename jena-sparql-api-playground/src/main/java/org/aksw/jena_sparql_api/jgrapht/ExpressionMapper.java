package org.aksw.jena_sparql_api.jgrapht;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.aksw.commons.collections.tagmap.TagMapSetTrie;
import org.aksw.commons.collections.tagmap.ValidationUtils;
import org.aksw.commons.graph.index.core.SubgraphIsomorphismIndex;
import org.aksw.commons.graph.index.jena.SubgraphIsomorphismIndexJena;
import org.aksw.commons.graph.index.jena.transform.QueryToJenaGraph;
import org.aksw.commons.jena.graph.GraphVarImpl;
import org.aksw.jena_sparql_api.algebra.transform.ExprTransformVariableOrder;
import org.aksw.jena_sparql_api.query_containment.index.OpGraph;
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
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;


public class ExpressionMapper {
	public static Set<Set<Expr>> computeResidualExpressions(BiMap<Node, Node> baseIso, Expr view, Expr user) {
		view = ExprTransformer.transform(new ExprTransformVariableOrder(), view);
		user = ExprTransformer.transform(new ExprTransformVariableOrder(), user);

		System.out.println("Normalization: " + view + " vs " + user);
		
		Set<Set<Expr>> viewDnf = CnfUtils.toSetCnf(view);
		Set<Set<Expr>> userDnf = CnfUtils.toSetCnf(user);
		
		System.out.println("viewNf: " + viewDnf);
		System.out.println("userNf: " + userDnf);
		//if(viewDnf.size() >= 1 || userDnf.)
		
		Set<Set<Expr>> result = computeResidualExpressions(baseIso, viewDnf, userDnf);
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
	
	public static OpGraph toOpGraph(Set<Expr> conjunction, Supplier<Node> nodeSupplier) {
		Node clauseNode = nodeSupplier.get();
		
        BiMap<Node, Expr> nodeToExpr = HashBiMap.create(); //new HashMap<>();
		Graph graph = new GraphVarImpl();
        QueryToJenaGraph.clauseToGraph(clauseNode, graph, nodeToExpr, conjunction, nodeSupplier);

        OpGraph result = new OpGraph(graph, nodeToExpr);
        return result;
	}
	
	public static SubgraphIsomorphismIndex<Long, DirectedGraph<Node, Triple>, Node> createIndex() {
		// Create an index entry for each view clause
		// Then, perform a lookup with each user clause whether there is an entry.
		// If there is none, the view cannot be used to answer the query
		
		SubgraphIsomorphismIndex<Long, DirectedGraph<Node, Triple>, Node> siiTreeTags = SubgraphIsomorphismIndexJena.create();
        SubgraphIsomorphismIndex<Long, DirectedGraph<Node, Triple>, Node> siiFlat = SubgraphIsomorphismIndexJena.createFlat();
        SubgraphIsomorphismIndex<Long, DirectedGraph<Node, Triple>, Node> siiTagBased = SubgraphIsomorphismIndexJena.createTagBased(new TagMapSetTrie<>(NodeUtils::compareRDFTerms));

        SubgraphIsomorphismIndex<Long, DirectedGraph<Node, Triple>, Node> siiValidating = ValidationUtils.createValidatingProxy(SubgraphIsomorphismIndex.class, siiTreeTags, siiTagBased);
        SubgraphIsomorphismIndex<Long, DirectedGraph<Node, Triple>, Node> sii = siiTreeTags; //siiValidating;

        return sii;
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
	
	
	public static Set<Expr> commonConstraints(Set<Set<Expr>> dnf) {
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
	 * @param baseIso
	 * @param viewCnf
	 * @param userCnf
	 * @return
	 */
	public static Set<Set<Expr>> computeResidualExpressions(BiMap<Node, Node> baseIso, Set<Set<Expr>> viewCnf, Set<Set<Expr>> userCnf) {
		// Find all constraints that occur in all clauses
		
		Set<Expr> commonUserExprs = commonConstraints(userCnf);
		System.out.println(commonUserExprs);
		
		
		//CnfUtils.extractEquality(clause)
		
		
		SubgraphIsomorphismIndex<Long, DirectedGraph<Node, Triple>, Node> sii = createIndex();
        Supplier<Supplier<Node>> ssn = () -> { int[] x = {0}; return () -> NodeFactory.createBlankNode("_" + x[0]++); };

        
        //HashMap<Integer, Set<Expr>> idToViewClause = new LinkedHashMap<>();
        Map<Long, Set<Expr>> idToUserClause = StreamUtils.zipWithIndex(userCnf.stream())
        		.collect(toLinkedHashMap(Indexed::getIndex, Indexed::getValue));

        Map<Long, OpGraph> idToUserOpGraph = new HashMap<>();

        for(Entry<Long, Set<Expr>> userClauseI : idToUserClause.entrySet()) {
        	long id = userClauseI.getKey();
        	Set<Expr> userClause = userClauseI.getValue();
        	
        	OpGraph opGraph = toOpGraph(userClause, ssn.get());        	
        	DirectedGraph<Node, Triple> g = opGraph.getJGraphTGraph();
        	
        	sii.put(id, g);
        	idToUserOpGraph.put(id, opGraph);
        }

        Map<Long, Set<Expr>> idToViewClause = StreamUtils.zipWithIndex(viewCnf.stream())
        		.collect(toLinkedHashMap(Indexed::getIndex, Indexed::getValue));

        
        Set<Set<Expr>> residualCnf = new LinkedHashSet<>();
        Set<Long> coveredUserClauseIds = new LinkedHashSet<>();
        
        // For every view clause there has to be a more restrictive user clause
        
        // TODO Sort view clauses by size (we expect those to yield fewest isos)
        
        for(Set<Expr> viewClause : viewCnf) {
        	OpGraph viewOpGraph = toOpGraph(viewClause, ssn.get());
        	
        	
        	BiMap<Node, Expr> viewNodeToExpr = viewOpGraph.getNodeToExpr();
        	BiMap<Expr, Node> viewExprToNode = viewNodeToExpr.inverse();
        	
        	DirectedGraph<Node, Triple> g = viewOpGraph.getJGraphTGraph();

        	System.out.println("Lookup with clause: " + viewClause);
        	
        	// This returns candidate clauses of the query that may be more restrictive than those
        	// of the view
        	Multimap<Long, BiMap<Node, Node>> cands = sii.lookupX(g, false);

        	boolean foundMatch = false;
        	for(Entry<Long, BiMap<Node, Node>> e : cands.entries()) {
        		
        		long userId = e.getKey();
        		Set<Expr> userClause = idToUserClause.get(userId);
        		BiMap<Node, Node> userToView = e.getValue();
        		//BiMap<Node, Node> viewToUser = userToView.inverse(); 

        		System.out.println("  Candidate user clause: " + idToUserClause.get(userId));

        		
        		OpGraph userOpGraph = idToUserOpGraph.get(userId);
        		BiMap<Node, Expr> userNodeToExpr = userOpGraph.getNodeToExpr();
        		BiMap<Expr, Node> userExprToNode = userNodeToExpr.inverse();
        		
        		Set<Expr> residualClause = new HashSet<>();
        		for(Expr userExpr : userClause) {
        			
        			Node userNode = userExprToNode.get(userExpr);
        			Node viewNode = userToView.getOrDefault(userNode, userNode);
        			
        			if(viewNode == null) {
        				//residualClause.add(userExpr);
        				residualClause = null;
        			} else {
	        			
            			Expr viewExpr = viewNodeToExpr.get(viewNode);
	
	        			Expr renamedUserExpr = userExpr.applyNodeTransform(new NodeTransformRenameMap(userToView));

	        			if(Objects.equals(viewExpr, renamedUserExpr)) {
	        				residualClause.add(viewExpr);
	        			} else {
	        			
	        			// Handle regex
		        			if(renamedUserExpr instanceof E_StrContains) {
		        				E_StrContains aExpr = (E_StrContains)viewExpr;
		        				E_StrContains bExpr = (E_StrContains)renamedUserExpr;
		
		//        				if(Objects.equals(aExpr, bExpr)) {
		//        					r = Expr.
		//        				}
		        				
		        				Expr a = aExpr.getArg2();
		        				Expr b = bExpr.getArg2();
		
		        				String aStr = getString(a);
		        				String bStr = getString(b);
		        				
		        				
		        				if(aStr != null && bStr != null && bStr.contains(aStr)) {
		        					residualClause.add(new E_StrContains(bExpr.getArg1(), b));
		        				} else {
		        					residualClause = null;
		        				}
		        			} else {		        			
		        				residualClause = null;
		        			}
	        			}
	        			
	        			
	        			if(residualClause != null) {
		        			foundMatch = true;
	        			} else {
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
        }
        
        
        // TODO Add all query clauses not covered by the view 


        return residualCnf;
	}
	
	public static void main(String[] args) {
		Expr view = ExprUtils.parse("1 + ?y * ?x = ?h && contains(?i, 'foo') && (?a = 1 || ?a = 2 || ?a = 3)");
		Expr user = ExprUtils.parse("(?a * ?b) + 1 = ?z && contains(?j, 'foobar') && (?o = 1 || ?o = 2)");
		
		// Expected: [(contains(?j, 'foobar'), !?o = ?3)]
		
		
		Set<Set<Expr>> residualDnf = computeResidualExpressions(HashBiMap.create(), view, user);
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

