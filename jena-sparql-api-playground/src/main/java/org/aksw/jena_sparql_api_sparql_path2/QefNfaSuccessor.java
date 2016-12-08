package org.aksw.jena_sparql_api_sparql_path2;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.sparql_path2.Nfa;

public class QefNfaSuccessor<S, T, V>
//	implements Function<Pa>
{
//    public static <S, T, E> Multimap<Pair<S, V>, Pair<V, E>> createLookupServiceSuccessor(QueryExecutionFactory qef, Nfa<S, T> nfa) {
//
//    }

    protected Nfa<S, T> nfa;
    protected QueryExecutionFactory qef;

    public Map<V, S> apply(Set<? extends Entry<S, V>> stateToVertex) {
        return null;
    }

}
