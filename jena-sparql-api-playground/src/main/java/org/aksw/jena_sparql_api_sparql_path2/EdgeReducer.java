package org.aksw.jena_sparql_api_sparql_path2;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.aksw.commons.collections.multimaps.BiHashMultimap;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.engine.binding.Binding;
import org.jgrapht.DirectedGraph;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class EdgeReducer {

    public static BiHashMultimap<Node, Node> loadJoinSummary(QueryExecutionFactory qef) {
        BiHashMultimap<Node, Node> result = new BiHashMultimap<>();

        QueryExecution qe = qef.createQueryExecution("PREFIX o: <http://example.org/ontology/> SELECT ?x ?y { ?s o:sourcePredicate ?x ; o:targetPredicate ?y }");
        ResultSet rs = qe.execSelect();
        while(rs.hasNext()) {
            Binding binding = rs.nextBinding();

            Node x = binding.get(Vars.x);
            Node y = binding.get(Vars.y);

            result.put(x, y);
        }

        return result;
    }


    public static Map<Node, Long> loadPredicateSummary(QueryExecutionFactory qef) {
        Map<Node, Long> result = new HashMap<>();

        QueryExecution qe = qef.createQueryExecution("PREFIX o: <http://example.org/ontology/> SELECT ?x ?y { ?s a o:PredicateSummary ; o:predicate ?x ; o:freqTotal ?y }");
        ResultSet rs = qe.execSelect();
        while(rs.hasNext()) {
            Binding binding = rs.nextBinding();

            Node x = binding.get(Vars.x);
            Node y = binding.get(Vars.y);

            result.put(x, ((Number)y.getLiteralValue()).longValue());
        }

        return result;
    }





    /**
     * Based on the nfa, a list of all predicates and a predicate join summary,
     * determine the set of predicates referenced by the nfa
     *
     * The set of all predicates is implicitly used by transitionsToPredicates, as this function must resolve negated predicate sets
     *
     * BiHashMultimap<P, P> joinSummary
     *
     * @param nfa
     * @param predicates
     * @param joinSummary
     */
    public static <S, T, P> Set<P> getReferencedPredicates(Nfa<S, T> nfa, Predicate<Entry<P, P>> joins, Predicate<T> isEpsilon, Function<Set<T>, Set<P>> transitionsToPredicates) {
        Set<T> result = new HashSet<>();
        DirectedGraph<S, T> graph = nfa.getGraph();


        boolean change = true;
        Set<S> visited = new HashSet<>();

        Set<S> states = nfa.getStartStates();
        Multimap<S, P> priorTransitions = HashMultimap.create();


        while(change) {
            Set<S> nextStates = new HashSet<S>();
            Multimap<S, P> nextTransitions = HashMultimap.create();

            Set<T> transitions = JGraphTUtils.resolveTransitions(graph, states, isEpsilon);

            for(T t : transitions) {
                S state = graph.getEdgeTarget(t);
                Set<P> nextPredicates = transitionsToPredicates.apply(Collections.singleton(t));

                Collection<P> priorPredicates = priorTransitions.get(state);


                Set<P> reachablePredicates = nextPredicates.stream().filter(np ->
                    priorPredicates.stream().anyMatch(p ->
                        joins.test(new SimpleEntry<>(np, p)))
                    ).collect(Collectors.toSet());

                //predicates.stream().forEach(p -> priorTransitions.put(state, p));


            }



            change = visited.addAll(states);

            states = nextStates;



            //Set<P> predicates = transitionsToPredicates.apply(transitions);


        }
        return null;



    }
}
