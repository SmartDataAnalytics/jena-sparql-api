package org.aksw.jena_sparql_api.sparql_path2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.aksw.jena_sparql_api.utils.model.Triplet;
import org.aksw.jena_sparql_api.utils.model.TripletPath;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class BidirectionalSearch<S, G, V, E> {
//    protected NfaExecution<V> forwards;
//    protected NfaExecution<V> backwards;
    protected NfaFrontier<S, G, V, E> fwdFrontier;
    protected NfaFrontier<S, G, V, E> bwdFrontier;

    protected Set<NestedPath<V, E>> accepted = new HashSet<>();
    protected Function<TripletPath<V, E>, Boolean> pathCallback;

//    public BidirectionalSearch(NfaExecution<V> forwards, NfaExecution<V> backwards) {
//        this.forwards = forwards;
//        this.backwards = backwards;
//    }


    public static <S, V, E> Set<TripletPath<V, E>> intersect(NfaFrontier<S, V, V, E> fwd, NfaFrontier<S, V, V, E> bwd) {
        Set<TripletPath<V, E>> result = new HashSet<>();

        // Get the sets of states where the frontiers meet
        Set<S> fwdStates = fwd.getCurrentStates();
        Set<S> bwdStates = bwd.getCurrentStates();
        Set<S> commonStates = Sets.intersection(fwdStates, bwdStates);

        for(S state : commonStates) {
            Multimap<V, NestedPath<V, E>> fwdNodeToPaths = fwd.getPaths(state);
            Multimap<V, NestedPath<V, E>> bwdNodeToPaths = bwd.getPaths(state);

            Set<V> fwdNodes = fwdNodeToPaths.keySet();
            Set<V> bwdNodes = bwdNodeToPaths.keySet();
            Set<V> commonNodes = Sets.union(fwdNodes, bwdNodes);

            for(V node : commonNodes) {
                Collection<NestedPath<V, E>> fwdPaths = fwdNodeToPaths.get(node);
                Collection<NestedPath<V, E>> bwdPaths = bwdNodeToPaths.get(node);
                for(NestedPath<V, E> fwdPath : fwdPaths) {
                    for(NestedPath<V, E> bwdPath : bwdPaths) {
                        TripletPath<V, E> fwdPart = fwdPath.asSimplePath();
                        TripletPath<V, E> bwdPart = bwdPath.asSimplePath().reverse();

                        V start = fwdPart.getStart();
                        V end = fwdPart.getEnd();
                        List<Triplet<V, E>> triples = new ArrayList<>();
                        triples.addAll(fwdPart.getTriplets());
                        triples.addAll(bwdPart.getTriplets());

                        TripletPath<V, E> path = new TripletPath<>(start, end, triples);
                        result.add(path);
                    }
                }
            }
        }

        return result;
    }
}

