package org.aksw.jena_sparql_api.sparql_path2;

import java.util.Set;
import java.util.function.Function;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.jgrapht.wrapper.LabeledEdge;
import org.aksw.jena_sparql_api.jgrapht.wrapper.LabeledEdgeImpl;
import org.aksw.jena_sparql_api.lookup.MapService;
import org.aksw.jena_sparql_api.lookup.MapServiceUtils;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.lookup.LookupServiceFilterKey;
import org.aksw.jena_sparql_api.lookup.LookupServiceListService;
import org.aksw.jena_sparql_api.mapper.MappedConcept;
import org.aksw.jena_sparql_api.shape.ResourceShape;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.aksw.jena_sparql_api.utils.Pair;
import org.aksw.jena_sparql_api.utils.model.Triplet;
import org.aksw.jena_sparql_api.utils.model.TripletImpl;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.Path;

public class PathExecutionUtils {

    /**
     * A function that creates a lookup service for a given qef and predicate class
     *
     */
    public static <S, T> LookupService<Node, Set<Triplet<Node, Node>>> createLookupService(QueryExecutionFactory qef, Pair<ValueSet<Node>> predicateClass) {
        ResourceShapeBuilder rsb = new ResourceShapeBuilder();
        PathVisitorResourceShapeBuilder.apply(rsb, predicateClass, false);

        //MappedConcept<Graph> mc = ResourceShape.createMappedConcept(rsb.getResourceShape(), filter);
        MappedConcept<Graph> mc = ResourceShape.createMappedConcept(rsb.getResourceShape(), null, false);
        MapService<Concept, Node, Graph> ls = MapServiceUtils.createListServiceAcc(qef, mc, false);
        //Map<Node, Graph> nodeToGraph = ls.fetchData(null, null, null);

        // TODO Add a default fluent API
        LookupService<Node, Graph> lsls = LookupServiceListService.create(ls);
        lsls = new LookupServiceFilterKey<Node, Graph>(lsls, k -> k.isURI());

        //lsls.partition(100);
        //lsls = LookupServicePartition.create(lsls, 100);

        LookupService<Node, Set<Triplet<Node, Node>>> s = lsls.mapValues((k, e) -> {
//            Map<Node, Set<Triplet<Node, Node>>> r =
//              map.entrySet().stream()
//              .collect(Collectors.toMap(Entry::getKey, e -> graphToTriplets(e.getValue())));
            Set<Triplet<Node, Node>> r = graphToTriplets(e);
            return r;
        });

        return s;
    };


    public static Set<Triplet<Node, Node>> graphToTriplets(Graph graph) {
        Set<Triplet<Node, Node>> result = graph
            .find(Node.ANY, Node.ANY, Node.ANY)
            .mapWith(t -> (Triplet<Node, Node>)new TripletImpl<Node, Node>(t.getSubject(), t.getPredicate(), t.getObject()))
            .toSet();
        return result;
    }

    public static void executePath(Path path, Node startNode, Node targetNode, QueryExecutionFactory qef, Function<NestedPath<Node, Node>, Boolean> pathCallback) {

        Nfa<Integer, LabeledEdge<Integer, PredicateClass>> nfa = PathCompiler.compileToNfa(path);


        System.out.println("NFA");
        System.out.println(nfa);
        for(LabeledEdge<Integer, PredicateClass> edge : nfa.getGraph().edgeSet()) {
            System.out.println(edge);
        }

//        PartialNfa<Integer, Path> peek = nfaCompiler.peek();

        //QueryExecutionFactory qef = FluentQueryExecutionFactory.http("http://dbpedia.org/sparql", "http://dbpedia.org").config().selectOnly().end().create();

        //NfaExecution<Integer, LabeledEdge<Integer, Path>, Node, Node> exec = new NfaExecution<>(nfa, qef, false, p -> targetNode == null || p.getEnd().equals(targetNode) ? pathCallback.apply(p) : false);

        //Function<LabeledEdge<Integer, Path>, Path> edgeToPath = e -> e.getLabel();

        NfaFrontier<Integer, Node, Node, Node> frontier = new NfaFrontier<>();
        Function<NestedPath<Node, Node>, Node> nodeGrouper = nestedPath -> nestedPath.getCurrent();

        NfaFrontier.addAll(frontier, nfa.getStartStates(), nodeGrouper, startNode);





        // TODO: How to wrap a LookupService, such that we can chain transformations?
        // Ok, java8 supports this natively
        // In essence, a lookupservice is a Function<K, Map<K, V>>
//
//        Function<Collection<Node>, Map<Node, Graphlet<Node, Node>>> nodeToGraphlets = (LabeledEdge<Integer, Path> transition, boolean assumeReversed) -> {
//
//            //Path path = transitionToPath.apply(transition);
//
//
//
//            //Map<Node, Graph> nodeToGraph = lsls.apply(nodes);
//        };

        while(!frontier.isEmpty()) {

            boolean abort = NfaExecutionUtils.collectPaths(nfa, frontier, LabeledEdgeImpl::isEpsilon, pathCallback);
            if(abort) {
                break;
            }

            if(true) {
                throw new RuntimeException("Adjust the code");
            }
            //NfaFrontier<Integer, Node, Node, Node> nextFrontier = null; //
//            NfaFrontier<Integer, Node, Node, Node> nextFrontier = NfaExecutionUtils.advanceFrontier(
//                    frontier,
//                    nfa,
//                    LabeledEdgeImpl::isEpsilon,
//                    createLookupService, // getTriplets
//                    nestedPath -> nestedPath.getCurrent(),
//                    false
//                    );
//            //System.out.println("advancing...");
//            frontier = nextFrontier;
        }

    }


    /**
     * Generic nfa execution
     *
     * BiFunction<Set<V>, T, Map<V, Set<Triplet<V, E>>>> getMatchingTriplets
     *
     * This function resolves a single transition of the nfa to a set of triplets
     * The Directed<T> is used to execute the automaton in reverse direction
     *
     *
     * @param nfa
     * @param vertices
     */
//    public static <S, T, V, E> void execNfa(
//            Nfa<S, T> nfa,
//            Predicate<T> isEpsilon,
//            Set<V> startVertices,
//            BiFunction<T, NestedPath<V, E>, Map<V, Set<Triplet<V, E>>>> getMatchingTriplets,
//            Function<NestedPath<V, E>, Boolean> pathCallback) {
//        execNfa(
//                nfa,
//                nfa.getStartStates(),
//                isEpsilon,
//                startVertices,
//                //getMatchingTriplets,
//                (trans, vToNestedPaths) ->
//                    vToNestedPaths.values().stream()
//                    .map(nestedPath -> getMatchingTriplets.apply(trans, nestedPath))
//                    .collect(Collectors.toSet()),
//                pathCallback);
//    }


}
