package org.aksw.jena_sparql_api_sparql_path2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
    protected NfaFrontier<S, V, V, E> frontier;

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

        this.frontier = new NfaFrontier<S, V, V, E>();
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
            frontier.add(state, node, rdfPath);
        }
    }


    public static <S, T, G, V, E> boolean collectPaths(Nfa<S, T> nfa, NfaFrontier<S, G, V, E> frontier, Predicate<T> isEpsilon, Function<NestedPath<V, E>, Boolean> pathCallback) {
        boolean isFinished = false;
        Set<S> currentStates = frontier.getCurrentStates();
        for(S state : currentStates) {

            boolean isFinal = isFinalState(nfa, state, isEpsilon);
            if(isFinal) {
                Multimap<G, NestedPath<V, E>> ps = frontier.getPaths(state);
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


    /**
     * The getMatchingTriples function takes as input all paths (by some grouping) for a certain nfa state,
     * and yields a set of triplets that connect to the endpoints of the current paths in that group
     *
     *
     * @param frontier
     * @param nfaGraph
     * @param isEpsilon
     * @param getMatchingTriplets
     * @param pathGrouper
     * @param earlyPathReject
     * @return
     */
    public static <S, T, G, V, E> NfaFrontier<S, G, V, E> advanceFrontier(
            NfaFrontier<S, G, V, E> frontier,
            DirectedGraph<S, T> nfaGraph,
            Predicate<T> isEpsilon,
            BiFunction<T, Multimap<G, NestedPath<V, E>>, Map<V, Set<Triplet<V, E>>>> getMatchingTriplets,
            Function<NestedPath<V, E>, G> pathGrouper,
            Predicate<NestedPath<V, E>> earlyPathReject // Function that can reject paths before they are added to the frontier, such as by consulting a join summary or performing a reachability test to the target
            ) {
        // Prepare the next frontier
        NfaFrontier<S, G, V, E> result = new NfaFrontier<S, G, V, E>();

        Set<S> currentStates = frontier.getCurrentStates();
        for(S state : currentStates) {
            Multimap<G, NestedPath<V, E>> ps = frontier.getPaths(state);
            Set<T> transitions = JGraphTUtils.resolveTransitions(nfaGraph, state, isEpsilon, false);

            for(T trans : transitions) {

                Map<V, Set<Triplet<V, E>>> vToTriplets = getMatchingTriplets.apply(trans, ps);
                Collection<NestedPath<V, E>> allPaths = ps.values();

                for(NestedPath<V, E> parentPath : allPaths) {
                    V node = parentPath.getCurrent();

                    Set<Triplet<V, E>> triplets = vToTriplets.getOrDefault(node, Collections.emptySet());

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

                        NestedPath<V, E> next = new NestedPath<V, E>(new ParentLink<V, E>(parentPath, p0), o);
                        G groupKey = pathGrouper.apply(next);

                        if(next.isCycleFree()) {
                            boolean reject = earlyPathReject.test(next);
                            if(!reject) {
                                S targetState = nfaGraph.getEdgeTarget(trans);
                                result.add(targetState, groupKey, next);
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
            P startVertex, // the start vertex
            Long k,
            BiFunction<T, Directed<P>, Set<Directed<P>>> transAndNodesToTriplets,  //Triplet<P, Q>>
            Function<NestedPath<P, Q>, Boolean> pathCallback) {


        // Group by the directed of the prior predicate (null if there is no prior predicate)
        Function<NestedPath<P, Q>, Directed<P>> pathGrouper = nestedPath ->
            nestedPath.getParentLink().map(pl ->
                new Directed<P>(nestedPath.getCurrent(), pl.getDiProperty().isReverse())
            ).orElse(null);

        List<NestedPath<P, Q>> result = new ArrayList<>();
        PathExecutionUtils.execNfa(
                nfa,
                states,
                isEpsilon,
                Collections.singleton(startVertex),
                pathGrouper,
                (trans, diPredToPaths) -> {
                    // there is a transition, an there is our initial predicate,
                    // and we now need to determine successor triplet of this predicate in regard to the transition
                    Map<P, Set<Triplet<P, Q>>> r = new HashMap<>();

                    Set<Directed<P>> diPreds = diPredToPaths.keySet();
                    for(Directed<P> diPred : diPreds) {
                        P pred = diPred == null ? null : diPred.getValue();
                        ///Set<Q> joinEdges = joinGraph.outgoingEdgesOf(pred);
                        //Set<Triplet<P, Q>> triplets = transAndNodesToTriplets.apply(trans, diPred);
                        Set<Directed<P>> nextDiPreds = transAndNodesToTriplets.apply(trans, diPred);

                        Set<Triplet<P, Q>> triplets = nextDiPreds.stream()
                                .map(dp -> Triplet.create(pred, (Q)null, dp.getValue(), dp.isReverse())) // TODO get rid of the null - maybe: joinGraph.getEdge(pred, ...)
                                .collect(Collectors.toSet());



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

