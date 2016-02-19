package org.aksw.jena_sparql_api_sparql_path2;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.lookup.ListService;
import org.aksw.jena_sparql_api.lookup.ListServiceUtils;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.lookup.LookupServiceListService;
import org.aksw.jena_sparql_api.lookup.LookupServicePartition;
import org.aksw.jena_sparql_api.mapper.MappedConcept;
import org.aksw.jena_sparql_api.shape.ResourceShape;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.Path;
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
    protected Frontier<S, V, E> frontier;

    //protected Set<NestedRdfPath> accepted = new HashSet<NestedRdfPath>();
    protected Function<MyPath<V, E>, Boolean> pathCallback;

    /**
     * If an nfa is reversed only be reversing the edges of the automaton, the edge labels themselves
     * are not reversed. This flag is used to treat edge labels (i.e. property directions) reversed.
     */
    protected boolean reversePropertyDirection = false;


    // Nfa<S, LabeledEdge<V, Path>> nfa
    public NfaExecution(Nfa<S, T> nfa, QueryExecutionFactory qef, boolean reversePropertyDirection, Function<MyPath<V, E>, Boolean> pathCallback) {
        this.nfa = nfa;
        this.qef = qef;
        this.reversePropertyDirection = reversePropertyDirection;
        this.pathCallback = pathCallback;

        this.frontier = new Frontier<S, V, E>();
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


    public static <S, T, V, E> boolean collectPaths(Nfa<S, T> nfa, Frontier<S, V, E> frontier, Predicate<T> isEpsilon, Function<MyPath<V, E>, Boolean> pathCallback) {
        boolean isFinished = false;
        Set<S> currentStates = frontier.getCurrentStates();
        for(S state : currentStates) {

            boolean isFinal = isFinalState(nfa, state, isEpsilon);
            if(isFinal) {
                Multimap<V, NestedPath<V, E>> ps = frontier.getPaths(state);
                for(NestedPath<V, E> path : ps.values()) {
                    MyPath<V, E> rdfPath = path.asSimplePath();
                    isFinished = pathCallback.apply(rdfPath);
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


    public static <S, T, V, E> Frontier<S, V, E> advanceFrontier(
            Frontier<S, V, E> frontier,
            Nfa<S, T> nfa,
            //QueryExecutionFactory qef,
            boolean reversePropertyDirection,
            Predicate<T> isEpsilon,
            Function<DirectedProperty<T>, Function<Iterable<V>, Map<V, Graphlet<V, E>>>> transitionToNodesToGraphlets
            ) {
        // Prepare the next frontier
        Frontier<S, V, E> result = new Frontier<S, V, E>();

        Set<S> currentStates = frontier.getCurrentStates();
        for(S state : currentStates) {
            Multimap<V, NestedPath<V, E>> ps = frontier.getPaths(state);

            DirectedGraph<S, T> graph = nfa.getGraph();

            Set<T> transitions = JGraphTUtils.resolveTransitions(graph, state, isEpsilon);


            Set<V> nodes = ps.keySet();

            // TODO frontierToFilter
            // V nodes -> Triples
            //Concept filter = ConceptUtils.createFilterConcept(nodes);

            for(T transition : transitions) {

                DirectedProperty<T> tmp = new DirectedProperty<T>(transition, reversePropertyDirection);
                Function<Iterable<V>, Map<V, Graphlet<V, E>>> nodesToGraphlets = transitionToNodesToGraphlets.apply(tmp);
                Map<V, Graphlet<V, E>> nodeToGraphlet = nodesToGraphlets.apply(nodes);


                for(Entry<V, Graphlet<V, E>> entry : nodeToGraphlet.entrySet()) {
                    V node = entry.getKey();
                    Graphlet<V, E> g = entry.getValue();

                    //Node.ANY, Node.ANY, Node.ANY
                    //for(Triplet<V, E> t : g.find(null, null, null)) {
                    Iterator<Triplet<V, E>> it = g.find(null, null, null);
                    while(it.hasNext()) {
                        Triplet<V, E> t = it.next();
                        E p = t.getPredicate();
                        //P_Path0 p0;
                        DirectedProperty<E> p0;

                        //Node s;
                        V o;
                        if(t.getSubject().equals(node)) {
                            //p0 = new P_Link(p);
                            p0 = new DirectedProperty<E>(p, false);
                            //s = t.getSubject();
                            o = t.getObject();
                        } else if(t.getObject().equals(node)) {
                            p0 = new DirectedProperty<E>(p, true);
                            //t = TripleUtils.swap(t);
                            //s = t.getObject();
                            o = t.getSubject();
                        } else {
                            throw new RuntimeException("Should not happen");
                        }

                        Collection<NestedPath<V, E>> parentPaths = ps.get(node);
                        for(NestedPath<V, E> parentPath : parentPaths) {
                            NestedPath<V, E> next = new NestedPath<V, E>(Optional.of(new ParentLink<V, E>(parentPath, p0)), o);

                            if(next.isCycleFree()) {
                                S targetState = graph.getEdgeTarget(transition);
                                result.add(targetState, next);
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

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

}