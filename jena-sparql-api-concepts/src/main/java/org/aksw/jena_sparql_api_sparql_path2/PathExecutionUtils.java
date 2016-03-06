package org.aksw.jena_sparql_api_sparql_path2;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.concepts.Concept;
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
import org.jgrapht.VertexFactory;
import org.jgrapht.graph.DefaultDirectedGraph;

public class PathExecutionUtils {

    public static Nfa<Integer, LabeledEdge<Integer, PredicateClass>> compileToNfa(Path path) {
        //Path path = PathParser.parse("!(<p>|(<p>|<p>))", PrefixMapping.Extended);

        path = PathVisitorTopDown.apply(path, new PathVisitorRewriteInvert());

        /*
         * Some ugly set up of graph related stuff
         */
        EdgeFactoryLabeledEdge<Integer, PredicateClass> edgeFactory = new EdgeFactoryLabeledEdge<Integer, PredicateClass>();
        EdgeLabelAccessor<LabeledEdge<Integer, PredicateClass>, PredicateClass> edgeLabelAccessor = new EdgeLabelAccessorImpl<Integer, LabeledEdge<Integer, PredicateClass>, PredicateClass>();
        DefaultDirectedGraph<Integer, LabeledEdge<Integer, PredicateClass>> graph = new DefaultDirectedGraph<Integer, LabeledEdge<Integer, PredicateClass>>(edgeFactory);
        VertexFactory<Integer> vertexFactory = new VertexFactoryInteger(graph);

        PathVisitorNfaCompilerImpl<Integer, LabeledEdge<Integer, PredicateClass>, PredicateClass> nfaCompiler = new PathVisitorNfaCompilerImpl<Integer, LabeledEdge<Integer, PredicateClass>, PredicateClass>(graph, vertexFactory, edgeLabelAccessor, x -> PathVisitorPredicateClass.transform(x));

        /*
         * The actual nfa conversion step
         */
        path.visit(nfaCompiler);
        Nfa<Integer, LabeledEdge<Integer, PredicateClass>> result = nfaCompiler.complete();

        return result;
    }

    public static void executePath(Path path, Node startNode, Node targetNode, QueryExecutionFactory qef, Function<NestedPath<Node, Node>, Boolean> pathCallback) {

        Nfa<Integer, LabeledEdge<Integer, PredicateClass>> nfa = compileToNfa(path);


        System.out.println("NFA");
        System.out.println(nfa);
        for(LabeledEdge<Integer, PredicateClass> edge : nfa.getGraph().edgeSet()) {
            System.out.println(edge);
        }

//        PartialNfa<Integer, Path> peek = nfaCompiler.peek();

        //QueryExecutionFactory qef = FluentQueryExecutionFactory.http("http://dbpedia.org/sparql", "http://dbpedia.org").config().selectOnly().end().create();

        //NfaExecution<Integer, LabeledEdge<Integer, Path>, Node, Node> exec = new NfaExecution<>(nfa, qef, false, p -> targetNode == null || p.getEnd().equals(targetNode) ? pathCallback.apply(p) : false);

        //Function<LabeledEdge<Integer, Path>, Path> edgeToPath = e -> e.getLabel();

        Frontier<Integer, Node, Node> frontier = new Frontier<>();
        Frontier.addAll(frontier, nfa.getStartStates(), startNode);


        Function<Directed<LabeledEdge<Integer, PredicateClass>>, Function<Iterable<Node>, Map<Node, Graphlet<Node, Node>>>> createLookupService = (Directed<LabeledEdge<Integer, PredicateClass>> diTransition) -> {
            //Path pathx = diTransition.getProperty().getLabel();
            PredicateClass pathx = diTransition.getValue().getLabel();
            boolean assumeReversed = diTransition.isReverse();

            ResourceShapeBuilder rsb = new ResourceShapeBuilder();
            PathVisitorResourceShapeBuilder.apply(rsb, pathx, assumeReversed);

            //PathVisitorResourceShapeBuilder visitor = new PathVisitorResourceShapeBuilder(assumeReversed);
            //pathx.visit(visitor);
            //ResourceShapeBuilder rsb = visitor.getResourceShapeBuilder();


            //MappedConcept<Graph> mc = ResourceShape.createMappedConcept(rsb.getResourceShape(), filter);
            MappedConcept<Graph> mc = ResourceShape.createMappedConcept(rsb.getResourceShape(), null);
            ListService<Concept, Node, Graph> ls = ListServiceUtils.createListServiceAcc(qef, mc, false);
            //Map<Node, Graph> nodeToGraph = ls.fetchData(null, null, null);

            LookupService<Node, Graph> lsls = LookupServiceListService.create(ls);
            lsls = LookupServicePartition.create(lsls, 100);

            Function<Iterable<Node>, Map<Node, Graphlet<Node, Node>>> s = lsls.andThen(map -> {
                Map<Node, Graphlet<Node, Node>> r = map.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> new GraphletGraph(e.getValue())));
                return r;
            });

            return s;
        };

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

            boolean abort = NfaExecution.collectPaths(nfa, frontier, LabeledEdgeImpl::isEpsilon, pathCallback);
            if(abort) {
                break;
            }

            Frontier<Integer, Node, Node> nextFrontier = NfaExecution.advanceFrontier(frontier, nfa, false, LabeledEdgeImpl::isEpsilon, createLookupService);
            //System.out.println("advancing...");
            frontier = nextFrontier;
        }

    }
}
