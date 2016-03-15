package org.aksw.jena_sparql_api_sparql_path2;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedGraphUnion;

public class JoinSummaryUtils {

	public static List<NestedPath<Node, DefaultEdge>> findJoinSummaryPaths(
	        Nfa<Integer, LabeledEdge<Integer, PredicateClass>> nfa,
	        Set<Integer> states,
	        DirectedGraph<Node, DefaultEdge> joinGraph,
	        Node augStart,
	        Node augEnd,
	        Long k) {
	    List<NestedPath<Node, DefaultEdge>> reachabilityPaths = NfaExecutionUtils.findPathsInJoinSummary(
	            nfa,
	            LabeledEdgeImpl::isEpsilon,
	            states,
	            joinGraph,
	            augStart,
	            1l,
	            (trans, node) -> { // function for dealing with a predicate without known preceeding predicate
	                Set<Directed<Node>> r = new HashSet<>();
	
	                PredicateClass pc = trans.getLabel();
	                for(int i = 0; i < 2; ++i) {
	                    boolean transReverse = i == 1;
	                    ValueSet<Node> preds = pc.get(i);
	
	                    Set<DefaultEdge> edges = transReverse
	                            ? joinGraph.incomingEdgesOf(node)
	                            : joinGraph.outgoingEdgesOf(node)
	                            ;
	
	                    edges.stream()
	                        .map(edge -> transReverse ? joinGraph.getEdgeSource(edge) : joinGraph.getEdgeTarget(edge))
	                        .filter(p -> preds.contains(p))
	                        .map(p -> new Directed<>(p, transReverse))
	                        .forEach(r::add);
	                }
	
	                return r;
	            },
	            (trans, diPred) -> { // for the nfa transition and a set data nodes, return matching triplets per node
	
	                // TODO: if the diPred is any, we assume that every predicate of the transition may match
	                // This is ugly if the transition allows any predicate
	
	
	                //Set<Triplet<Node, DefaultEdge>> r;
	                Node pred = diPred == null ? null : diPred.getValue();
	                PredicateClass pc = trans.getLabel();
	
	                Set<Directed<Node>> r = new HashSet<>();
	
	                boolean predReverse = diPred.isReverse();
	                // Check the transition - if it is opposite to the current predicate,
	                // we cannot consult the join summary - so we return a pseudo-triplet indicating that it will join with any further predicate
	                for(int i = 0; i < 2; ++i) {
	                    boolean transReverse = i == 1;
	                    ValueSet<Node> transPolPreds = pc.get(i); // polarity set
	                    Set<Node> transPreds = transPolPreds.getValues();
	
	                    // If transition and path predicate face in the same direction, we can consult the join summary
	                    // If the point in opposing directions, we return the ANY token
	                    if(predReverse == transReverse) {
	                        if(pred.equals(Node.ANY)) {
	                            if(transPolPreds.isPositive()) {
	                                transPreds.stream()
	                                    .map(p -> new Directed<>(p, transReverse))
	                                    .forEach(r::add);
	                            } else {
	                                r.add(new Directed<>(Node.ANY, transReverse));
	                            }
	                        } else {
	                            Set<DefaultEdge> edges = transReverse
	                                    ? joinGraph.incomingEdgesOf(pred)
	                                    : joinGraph.outgoingEdgesOf(pred)
	                                    ;
	
	                            edges.stream()
	                                .map(edge -> transReverse ? joinGraph.getEdgeSource(edge) : joinGraph.getEdgeTarget(edge))
	                                .filter(p -> transPolPreds.contains(p))
	                                .map(p -> new Directed<>(p, transReverse))
	                                .forEach(r::add);
	                        }
	                    } else {
	                        if(!transPolPreds.isEmpty()) {
	                            r.add(new Directed<>(Node.ANY, transReverse));
	                        }
	                    }
	                }
	
	                return r;
	            },
	            nestedPath -> {
	                Node current = nestedPath.getCurrent();
	                boolean r = current.equals(Node.ANY) || nestedPath.getCurrent().equals(augEnd);
	                return r;
	            });
	
	    reachabilityPaths.forEach(o -> System.out.println("REACHPATH: " + o.asSimplePath()));
	    return reachabilityPaths;
	}

	/**
	 * Checks whether there exists a path connecting start and end nodes via the nfa
	 *
	 * @param nfa
	 * @param augStart
	 * @param augEnd
	 * @param joinGraph
	 */
	public static boolean existsJoinSummaryPath(
	        Nfa<Integer, LabeledEdge<Integer, PredicateClass>> nfa,
	        Set<Integer> states,
	        DirectedGraph<Node, DefaultEdge> joinGraph,
	        Node augStart,
	        Node augEnd) {
	
	    List<NestedPath<Node, DefaultEdge>> paths = findJoinSummaryPaths(
	        nfa,
	        states,
	        joinGraph,
	        augStart,
	        augEnd,
	        1l);
	
	    boolean result = !paths.isEmpty();
	    return result;
	}

	/**
	 * Given a predicate and a direction,
	 * determine whether a path exists for this predicate
	 *
	 * @param nfa
	 * @param state the current set of states in the nfa
	 * @param endAugJoinGraph the end-augmented join graph
	 * @param augEnd the end node of the end-augmented join graph
	 * @param joinGraph
	 */
	public static boolean existsReachability(
	        Nfa<Integer, LabeledEdge<Integer, PredicateClass>> nfa,
	        Set<Integer> states,
	        DirectedGraph<Node, DefaultEdge> endAugJoinGraph, // joinGraph that was augmented with targets
	        Node augEnd,
	        Node predicate,
	        boolean reverse) {
	
	    DirectedGraph<Node, DefaultEdge> augJoinGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
	
	    // TODO Dynamically allocate a start and end vertex that is not part of the rawJoinGraph
	    Node augStart = NodeFactory.createURI("http://start.org");
	    //Node augEnd = NodeFactory.createURI("http://end.org");
	    JGraphTUtils.addSuperVertex(augJoinGraph, augStart, predicate, reverse);
	
	    DirectedGraph<Node, DefaultEdge> joinGraph = new DirectedGraphUnion<Node, DefaultEdge>(augJoinGraph, endAugJoinGraph);
	
	    boolean result = existsJoinSummaryPath(
	            nfa,
	            states,
	            joinGraph,
	            augStart,
	            augEnd);
	
	    return result;
	}

}
