package org.aksw.jena_sparql_api.iso.index;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.aksw.combinatorics.solvers.ProblemNeighborhoodAware;
import org.aksw.commons.collections.utils.StreamUtils;
import org.aksw.jena_sparql_api.deprecated.iso.index.ProblemNodeMappingGraph;
import org.aksw.jena_sparql_api.deprecated.iso.index.ProblemVarMappingCompound;
import org.aksw.jena_sparql_api.jgrapht.transform.QueryToJenaGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.util.graph.GraphUtils;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;

import com.google.common.collect.BiMap;
import com.google.common.collect.Multimap;

public class SubGraphIsomorphismIndexJGraphT<K, V, E, G extends Graph<V, E>>
    extends SubGraphIsomorphismIndexBase<K, G, V>
{
    //protected Comparator<V> createVertexComparator(BiMap<V, V> baseIso);
    //protected Comparator<E> createEdgeComparator(BiMap<V, V> baseIso);
    protected Function<BiMap<V, V>, Comparator<V>> createVertexComparator;
    protected Function<BiMap<V, V>, Comparator<E>> createEdgeComparator;

    public SubGraphIsomorphismIndexJGraphT(
            Supplier<K> keySupplier,
            Function<? super G, Collection<?>> extractGraphTags,
            SetOps<G, V> graphOps,
            Function<BiMap<V, V>, Comparator<V>> createVertexComparator,
            Function<BiMap<V, V>, Comparator<E>> createEdgeComparator) {
        super(keySupplier, graphOps, extractGraphTags);
        this.createVertexComparator = createVertexComparator;
        this.createEdgeComparator = createEdgeComparator;
    }

    // TODO Move to util class
    public static SubGraphIsomorphismIndexJGraphT<Node, Node, Triple, DirectedGraph<Node, Triple>> create() {
        int i[] = {0};
        Supplier<Node> idSupplier = () -> NodeFactory.createURI("http://index.node/id" + i[0]++);

        SubGraphIsomorphismIndexJGraphT<Node, Node, Triple, DirectedGraph<Node, Triple>> result =
                new SubGraphIsomorphismIndexJGraphT<>(
                        idSupplier,
                        SubGraphIsomorphismIndexJGraphT::extractGraphTags,
                        SetOpsJGraphTRdfJena.INSTANCE,
                        QueryToJenaGraph::createNodeComparator,
                        QueryToJenaGraph::createEdgeComparator);
        return result;
    }

//
//    public static SubGraphIsomorphismIndex<Node, org.apache.jena.graph.Graph, Node> createForJenaGraph() {
//        int i[] = {0};
//        Supplier<Node> idSupplier = () -> NodeFactory.createURI("http://index.node/id" + i[0]++);
//
//        SubGraphIsomorphismIndex<Node, org.apache.jena.graph.Graph, Node> result =
//                new SubGraphIsomorphismIndexJGraphT<>(
//                        idSupplier,
//                        SubGraphIsomorphismIndexJGraphT::extractGraphTags2,
//                        SetOpsGraphJena.INSTANCE,
//                        QueryToJenaGraph::createNodeComparator,
//                        QueryToJenaGraph::createEdgeComparator);
//        return result;
//    }


    protected ProblemNeighborhoodAware<BiMap<V, V>, V> toProblem(BiMap<V, V> baseIso, G viewGraph, G insertGraph) {
        ProblemNeighborhoodAware<BiMap<V, V>, V> result = new ProblemNodeMappingGraph<V, E, G, V>(
                baseIso, viewGraph, insertGraph,
                createVertexComparator, createEdgeComparator);

        return result;
    }

    protected ProblemNeighborhoodAware<BiMap<V, V>, V> toProblem(InsertPosition<?, G, V> pos) {
        BiMap<V, V> baseIso = pos.getIso();
        G residualQueryGraph = pos.getResidualQueryGraph();

        // TODO This looks wrong, why an empty graph here?!
        G residualViewGraph = setOps.createNew(); //new GraphVarImpl();//pos.getNode().getValue(); //new GraphIsoMapImpl(pos.getNode().getValue(), pos.getNode().getTransIso()); //pos.getNode().getValue();

        //QueryToJenaGraph::createNodeComparator, QueryToJenaGraph::createEdgeComparator);
        ProblemNeighborhoodAware<BiMap<V, V>, V> result =
                toProblem(baseIso, residualViewGraph, residualQueryGraph);

        return result;
    }

    protected ProblemNeighborhoodAware<BiMap<V, V>, V> createCompound(Collection<? extends InsertPosition<?, G, V>> poss) {
        List<ProblemNeighborhoodAware<BiMap<V, V>, V>> problems = poss.stream()
                .map(this::toProblem)
                .collect(Collectors.toList());

        ProblemVarMappingCompound<BiMap<V, V>, V> result = new ProblemVarMappingCompound<>(problems);
        return result;
    }

    public Map<K, Iterable<BiMap<V, V>>> lookupStream(G queryGraph, boolean exactMatch) {
        Multimap<K, InsertPosition<K, G, V>> matches = lookup(queryGraph, exactMatch);

        Map<K, Iterable<BiMap<V, V>>> result =
            matches.asMap().entrySet().stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                        e -> {
                            ProblemNeighborhoodAware<BiMap<V, V>, V> c = createCompound(e.getValue());
                            return () -> c.generateSolutions().iterator();
                        }));

        return result;
    }

    public Map<K, ProblemNeighborhoodAware<BiMap<V, V>, V>> lookupStream2(G queryGraph, boolean exactMatch) {
        Multimap<K, InsertPosition<K, G, V>> matches = lookup(queryGraph, exactMatch);

        Map<K, ProblemNeighborhoodAware<BiMap<V, V>, V>> result =
            matches.asMap().entrySet().stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                        e -> createCompound(e.getValue())
                        ));

        return result;
//                    Map<K, ProblemNeighborhoodAware<BiMap<Var, Var>, Var>> result = matches.asMap().entrySet().stream()
//                            .collect(Collectors.toMap(e -> e.getKey(), e -> SparqlViewMatcherQfpcIso.createCompound(e.getValue())));
//
//
//        BiMap<Node, Node> baseIso = pos.getIso();
//
//
//        System.out.println("RAW SOLUTIONS for " + pos.getNode().getKey());
//        rawProblem.generateSolutions().forEach(s -> {
//            System.out.println("  Raw Solution: " + s);
//        });
//
//        ProblemNeighborhoodAware<BiMap<Var, Var>, Var> result = new ProblemVarWrapper(rawProblem);
//
//
//        return result;
    }

    public Iterable<BiMap<V, V>> match(BiMap<V, V> baseIso, G viewGraph, G insertGraph) {
        ProblemNeighborhoodAware<BiMap<V, V>, V> problem = toProblem(baseIso, viewGraph, insertGraph);
        Iterable<BiMap<V, V>> result = () -> problem.generateSolutions().iterator();

        return result;
    }


    public static Set<Node> extractGraphTags(Graph<Node, Triple> graph) {
        Set<Node> result = graph.edgeSet().stream()
            .flatMap(t -> Arrays.asList(t.getSubject(), t.getPredicate(), t.getObject()).stream())
            .filter(n -> n.isURI() || n.isLiteral())
            .collect(Collectors.toSet());

        return result;
    }

//    @Override
    protected Collection<?> extractGraphTags2(org.apache.jena.graph.Graph graph) {
        // TODO: All nodes does not include predicates
        Set<Node> result = StreamUtils.stream(GraphUtils.allNodes(graph))
                .filter(n -> n.isURI() || n.isLiteral())
                .collect(Collectors.toSet());

        return result;
    }

}
