package org.aksw.jena_sparql_api_sparql_path2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.jgrapht.DirectedGraph;

import com.google.common.collect.Multimap;


/**
 *
 * @author raven
 *
 * @param <S> NFA State type
 * @param <T> NFA Transition type
 * @param <V> Data Vertex Type (e.g. jena's Node or RDFNode)
 * @param <E> Data Property Edge Type (e.g. jena's Node or Property)
 */
public class NfaExecution<S, T, V, E> {
    protected Nfa<S, T> nfa;
    protected QueryExecutionFactory qef;

    /**
     * The frontier keeps track of the current paths being traced
     */
    protected NfaFrontier<S, V, E> frontier;

    //protected Set<NestedRdfPath> accepted = new HashSet<NestedRdfPath>();
    protected Function<TripletPath<V, E>, Boolean> pathCallback;

    /**
     * If an nfa is reversed only be reversing the edges of the automaton, the edge labels themselves
     * are not reversed. This flag is used to treat edge labels (i.e. property directions) reversed.
     */
    protected boolean reversePropertyDirection = false;


    // Nfa<S, LabeledEdge<V, Path>> nfa
    public NfaExecution(Nfa<S, T> nfa, QueryExecutionFactory qef, boolean reversePropertyDirection, Function<TripletPath<V, E>, Boolean> pathCallback) {
        this.nfa = nfa;
        this.qef = qef;
        this.reversePropertyDirection = reversePropertyDirection;
        this.pathCallback = pathCallback;

        this.frontier = new NfaFrontier<S, V, E>();
    }

    /**
     * Adds a node to the frontier under the given states
     *
     * @param states
     * @param node
     */
    public void add(Set<S> states, V node) {
        for(S state : states) {
            NestedPath<V, E> rdfPath = new NestedPath<V, E>(node);
            frontier.add(state, rdfPath);
        }
    }


    public static <S, T, V, E> boolean collectPaths(Nfa<S, T> nfa, NfaFrontier<S, V, E> frontier, Predicate<T> isEpsilon, Function<NestedPath<V, E>, Boolean> pathCallback) {
        boolean isFinished = false;
        Set<S> currentStates = frontier.getCurrentStates();
        for(S state : currentStates) {

            boolean isFinal = isFinalState(nfa, state, isEpsilon);
            if(isFinal) {
                Multimap<V, NestedPath<V, E>> ps = frontier.getPaths(state);
                for(NestedPath<V, E> path : ps.values()) {
                    //MyPath<V, E> rdfPath = path.asSimplePath();
                    isFinished = pathCallback.apply(path);
                    if(isFinished) {
                        break;
                    }
                }
            }

            if(isFinished) {
                break;
            }
        }

        return isFinished;
    }


    public static <S, D, T extends LabeledEdge<S, ? extends Directed<? extends ValueSet<D>>>> Pair<ValueSet<D>> extractNextPropertyClasses(DirectedGraph<S, T> nfaGraph, Predicate<T> isEpsilon, Set<S> states, boolean reverse) {
        Set<T> transitions = JGraphTUtils.resolveTransitions(nfaGraph, states, isEpsilon, false);

        ValueSet<D> fwd = ValueSet.createEmpty();
        ValueSet<D> bwd = ValueSet.createEmpty();


        for(T transition : transitions) {
            Directed<? extends ValueSet<D>> label = transition.getLabel();
            boolean isReverse = label.isReverse();

            // invert direction if reverse is true
            isReverse = reverse ? !isReverse : isReverse;


            ValueSet<D> valueSet = label.getValue();

            if(isReverse) {
                bwd = bwd.union(valueSet);
            } else {
                fwd = fwd.union(valueSet);
            }
        }

        Pair<ValueSet<D>> result = new Pair<>(fwd, bwd);
        return result;
    }

    /**
     * advances the state of the execution. returns false to indicate finished execution
     * @return
     *
     * TODO: We should detect dead states, as to prevent potential cycling in them indefinitely
     */
//    public boolean advance() {
//        boolean isFinished = collectPaths(nfa, frontier, pathCallback);
//        boolean result;
//
//        if(isFinished) {
//            result = false;
//        } else {
//            frontier = advanceFrontier(frontier, nfa, qef, reversePropertyDirection);
//            result = !frontier.isEmpty();
//        }
//
//        return result;
//    }

    //Function<T, Path> transitionToPath,


    public static <S, T, V, E> NfaFrontier<S, V, E> advanceFrontier(
            NfaFrontier<S, V, E> frontier,
            DirectedGraph<S, T> nfaGraph,
            Predicate<T> isEpsilon,
            BiFunction<T, Set<V>, Map<V, Set<Triplet<V, E>>>> getMatchingTriplets, // This is essentially the successor function
            Predicate<NestedPath<V, E>> earlyPathReject // Function that can reject paths before they are added to the frontier, such as by consulting a join summary or performing a reachability test to the target
            ) {
        // Prepare the next frontier
        NfaFrontier<S, V, E> result = new NfaFrontier<S, V, E>();

        Set<S> currentStates = frontier.getCurrentStates();
        for(S state : currentStates) {
            Multimap<V, NestedPath<V, E>> ps = frontier.getPaths(state);

            //DirectedGraph<S, T> graph = nfa.getGraph();

            Set<T> transitions = JGraphTUtils.resolveTransitions(nfaGraph, state, isEpsilon, false);


            Set<V> nodes = ps.keySet();

            for(T trans : transitions) {

                //Directed<T> diTrans = new Directed<T>(transition, reversePropertyDirection);
                Map<V, Set<Triplet<V, E>>> vToTriplets = getMatchingTriplets.apply(trans, nodes);

                for(Entry<V, Set<Triplet<V, E>>> entry : vToTriplets.entrySet()) {
                    V node = entry.getKey();

                    Set<Triplet<V, E>> triplets = entry.getValue();
                    for(Triplet<V, E> t : triplets) {
                        E p = t.getPredicate();
                        Directed<E> p0;

                        V o;
                        if(t.getSubject().equals(node)) {
                            p0 = new Directed<E>(p, false);
                            o = t.getObject();
                        } else if(t.getObject().equals(node)) {
                            p0 = new Directed<E>(p, true);
                            o = t.getSubject();
                        } else {
                            throw new RuntimeException("Should not happen");
                        }

                        Collection<NestedPath<V, E>> parentPaths = ps.get(node);
                        for(NestedPath<V, E> parentPath : parentPaths) {
                            NestedPath<V, E> next = new NestedPath<V, E>(new ParentLink<V, E>(parentPath, p0), o);


                            if(next.isCycleFree()) {
                                boolean reject = earlyPathReject.test(next);
                                if(!reject) {
                                    S targetState = nfaGraph.getEdgeTarget(trans);
                                    result.add(targetState, next);
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;
    }


    /**
     * Convenience method.
     *
     * Returns true if the given path can reach any of the target vertices
     * under a given edge.
     *
     * TODO We need to create a join summary excerpt
     *
     */
//    public static boolean <V, E> isReachableUnder(Graph<V, E> graph, NestedPath<V, E> path, E underEdge, Set<V> targets) {
//        JGraphTUtils.getAllPaths(graph, start, end)
//    }
//
//    public static <V, E> isReachable(Set<V> startVertices, Set<V> targetVertices) {
//    //    JGraphTUtils.
//    }


    /**
     * Tests if a state is final. This includes if there is a transitive
     * connection via epsilon edges to a final state.
     *
     * @param state
     * @return
     */
    public static <S, T> boolean isFinalState(Nfa<S, T> nfa, S state, Predicate<T> isEpsilon) {
        DirectedGraph<S, T> graph = nfa.getGraph();
        Set<S> endStates = nfa.getEndStates();
        Set<S> reachableStates = JGraphTUtils.transitiveGet(graph, state, 1, x -> isEpsilon.test(x));
        boolean result = reachableStates.stream().anyMatch(s -> endStates.contains(s));
        return result;
    }


    /**
     * Given
     * - an nfa and
     * - join graph, determine for a given
     * - predicate (pointing either forwards or backwards) in a certain set of     //nestedPath in a certain set of
     * - nfa states of whether it can reach the
     * - set of predicates leading to the target states.
     *
     * Execution works as follows:
     * It is assumed that the given predicate is reachable, so no further checks are performed.
     *
     *
     * BiFunction<Set<V>, Directed<T>, Map<V, Set<Triplet<V, E>>>> getMatchingTriplets
     */

    public static <S, T, P, Q> boolean isTargetReachable(Nfa<S, T> nfa, Predicate<T> isEpsilon, Set<S> states, BiPredicate<Directed<T>, Q> matcher, DirectedGraph<P, Q> joinGraph, Directed<P> diPredicate, Pair<Set<P>> targetPreds) {
        // Return true if there is at least 1 path
        return false;
    }


    /**
     * matcher(Directed<T>, joinGraph, vertex)
     *
     * @param nfa
     * @param isEpsilon
     * @param states
     * @param matcher
     * @param joinGraph
     * @param diPredicate
     * @param targetPreds
     * @return
     */
//T extends Pair<ValueSet<V>>
    public static <S, T, P, Q> List<NestedPath<P, Q>> findPathsInJoinSummary(
            Nfa<S, T> nfa,
            Predicate<T> isEpsilon,
            Set<S> states,
            DirectedGraph<P, Q> joinGraph,
            //P predicate,
            P startVertex, // the start vertex
            //Pair<Set<P>> targetPreds,
            BiFunction<T, P, Set<Triplet<P, Q>>> transAndNodesToTriplets,
            Function<NestedPath<P, Q>, Boolean> pathCallback,
            Long k) {


        List<NestedPath<P, Q>> result = new ArrayList<>();
        PathExecutionUtils.execNfa(
                nfa,
                states,
                isEpsilon,
//                Collections.<P>emptySet(),
                Collections.singleton(startVertex),
                (trans, preds) -> {
                    // there is a transition, an there is our initial predicate,
                    // and we now need to determine successor triplet of this predicate in regard to the transition

                    Map<P, Set<Triplet<P, Q>>> r = new HashMap<>();

                    for(P pred : preds) {
                        ///Set<Q> joinEdges = joinGraph.outgoingEdgesOf(pred);
                        Set<Triplet<P, Q>> triplets = transAndNodesToTriplets.apply(trans, pred);
                        // Check which join edges are accepted by the transition
//                        Set<Triplet<P, Q>> triplets = joinEdges.stream()
//                                .filter(e -> matcher.test(diTrans, e))
//                                .map(e -> new Triplet<>(joinGraph.getEdgeSource(e), e, joinGraph.getEdgeTarget(e)))
//                                .collect(Collectors.toSet());

                        r.put(pred, triplets);
                    }

                    return r;
                }, nestedPath -> {
                    boolean accept = pathCallback.apply(nestedPath);
                    if(accept) {
                        result.add(nestedPath);
                    }

                    boolean abort = k != null && result.size() >= k;
                    return abort;
                });



//        JGraphTUtils.getAllPaths(graph, starts, ends)
//
        return result;
    }
}

//
@FunctionalInterface
interface NfaDataGraphMatcher<T, V, E> {
    boolean matches(Directed<T> trans, DirectedGraph<V, E> graph, V vertex);
}

