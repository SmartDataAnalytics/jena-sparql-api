package org.aksw.jena_sparql_api_sparql_path2;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.Path;

public class SparqlKShortestPathFinderYen
    implements SparqlKShortestPathFinder
{
    protected QueryExecutionFactory qef;
    protected int resourceBatchSize;

    public SparqlKShortestPathFinderYen(QueryExecutionFactory qef, int resourceBatchSize) {
        this.qef = qef;
        this.resourceBatchSize = resourceBatchSize;
    }

    public static <S, V, E> TripletPath<V, Directed<E>> convertPath(TripletPath<? extends Entry<S, V>, Directed<E>> path) {
        List<Triplet<V, Directed<E>>> triplets = path.getTriplets().stream()
                .map(t -> new Triplet<>(t.getSubject().getValue(), t.getPredicate(), t.getObject().getValue()))
                .collect(Collectors.toList());

        TripletPath<V, Directed<E>> result = new TripletPath<>(
                path.getStart().getValue(),
                path.getEnd().getValue(),
                triplets);

        return result;
    }

    @Override
    public Iterator<TripletPath<Node, Directed<Node>>> findPaths(Node start, Node end, Path path, Long k) {
        Nfa<Integer, LabeledEdge<Integer, PredicateClass>> nfa = PathCompiler.compileToNfa(path);

        Function<Pair<ValueSet<Node>>, LookupService<Node, Set<Triplet<Node, Node>>>> createTripletLookupService =
                pc -> PathExecutionUtils.createLookupService(qef, pc).partition(resourceBatchSize);


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
