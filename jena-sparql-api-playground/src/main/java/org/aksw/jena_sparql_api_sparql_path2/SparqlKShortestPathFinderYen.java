package org.aksw.jena_sparql_api_sparql_path2;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.Path;

public class SparqlKShortestPathFinderYen
    implements SparqlKShortestPathFinder
{
    protected QueryExecutionFactory qef;

    public SparqlKShortestPathFinderYen(QueryExecutionFactory qef) {
        this.qef = qef;
    }

    public static <S, V, E> TripletPath<V, Directed<E>> convertPath(TripletPath<? extends Entry<S, V>, Directed<E>> path) {
        return null;
    }

    @Override
    public Iterator<TripletPath<Node, Directed<Node>>> findPaths(Node start, Node end, Path path, Long k) {
        Nfa<Integer, LabeledEdge<Integer, PredicateClass>> nfa = PathCompiler.compileToNfa(path);

        Function<Pair<ValueSet<Node>>, LookupService<Node, Set<Triplet<Node, Node>>>> createTripletLookupService =
                pc -> PathExecutionUtils.createLookupService(qef, pc);


        List<TripletPath<Entry<Integer, Node>, Directed<Node>>> kPaths =
                YensKShortestPaths.findPaths(
                      nfa,
                      x -> x.getLabel() == null, //LabeledEdgeImpl::isEpsilon,
                      e -> e.getLabel(),
                      createTripletLookupService,
                      start,
                      end,
                      k == null ? Integer.MAX_VALUE : k.intValue());

        Iterator<TripletPath<Node, Directed<Node>>> result =
                kPaths.stream().map(SparqlKShortestPathFinderYen::convertPath).iterator();

        return result;
    }

}
