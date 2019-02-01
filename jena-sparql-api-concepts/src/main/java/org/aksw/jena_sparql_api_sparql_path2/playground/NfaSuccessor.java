package org.aksw.jena_sparql_api_sparql_path2.playground;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.aksw.jena_sparql_api.sparql_path2.Nfa;
import org.aksw.jena_sparql_api.sparql_path2.ValueSet;
import org.aksw.jena_sparql_api.utils.Pair;
import org.aksw.jena_sparql_api.utils.model.Directed;
import org.aksw.jena_sparql_api.utils.model.Triplet;

class NfaSuccessor<S, T, V, E>
    implements Function<Iterable<Entry<S, V>>, Map<Entry<S, V>, Set<Triplet<Entry<S, V>, Directed<E>>>>>
{
    protected Nfa<S, T> nfa;
    protected Predicate<T> isEpsilon;
    protected Function<T, ? extends Pair<ValueSet<V>>> transToVertexClass;
    protected Function<Pair<ValueSet<V>>, ? extends Function<? super Iterable<V>, Map<V, Set<Triplet<V,E>>>>> createTripletLookupService;

    public NfaSuccessor(
        Nfa<S, T> nfa,
        Predicate<T> isEpsilon,
        Function<T, ? extends Pair<ValueSet<V>>> transToVertexClass,
        Function<Pair<ValueSet<V>>, ? extends Function<? super Iterable<V>, Map<V, Set<Triplet<V,E>>>>> createTripletLookupService
    ) {
        this.nfa = nfa;
        this.isEpsilon = isEpsilon;
        this.transToVertexClass = transToVertexClass;
        this.createTripletLookupService = createTripletLookupService;
    }

    @Override
    public Map<Entry<S, V>, Set<Triplet<Entry<S, V>, Directed<E>>>> apply(Iterable<Entry<S, V>> nodes) {

        Map<Entry<S, V>, Set<Triplet<Entry<S, V>, Directed<E>>>> result = NfaDijkstra.getSuccessors(
                nfa,
                isEpsilon,
                transToVertexClass,
                createTripletLookupService,
                nodes);

        return result;
    }

}