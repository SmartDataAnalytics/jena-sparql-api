package org.aksw.jena_sparql_api.iso.index;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.combinatorics.solvers.ProblemNeighborhoodAware;
import org.aksw.jena_sparql_api.deprecated.iso.index.ProblemNodeMappingGraph;
import org.aksw.jena_sparql_api.jgrapht.transform.QueryToJenaGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.util.graph.GraphUtils;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;

import com.google.common.collect.BiMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Streams;

class IsoMatcherImpl<V, E, G extends Graph<V, E>>
    implements IsoMatcher<G, V>
{
    protected Function<BiMap<V, V>, Comparator<V>> createVertexComparator;
    protected Function<BiMap<V, V>, Comparator<E>> createEdgeComparator;

    public IsoMatcherImpl(
            Function<BiMap<V, V>, Comparator<V>> createVertexComparator,
            Function<BiMap<V, V>, Comparator<E>> createEdgeComparator) {
        super();
        this.createVertexComparator = createVertexComparator;
        this.createEdgeComparator = createEdgeComparator;
    }

    public ProblemNeighborhoodAware<BiMap<V, V>, V> toProblem(BiMap<V, V> baseIso, G viewGraph, G insertGraph) {
        ProblemNeighborhoodAware<BiMap<V, V>, V> result = new ProblemNodeMappingGraph<V, E, G, V>(
                baseIso, viewGraph, insertGraph,
                createVertexComparator, createEdgeComparator);

        //Stream<BiMap<V, V>> result = tmp.generateSolutions();

        return result;
    }


    @Override
    public Iterable<BiMap<V, V>> match(BiMap<V, V> baseIso, G viewGraph, G insertGraph) {
        ProblemNeighborhoodAware<BiMap<V, V>, V> problem = toProblem(baseIso, viewGraph, insertGraph);
        Iterable<BiMap<V, V>> result = () -> problem.generateSolutions().iterator();

        return result;
    }

}

public class SubGraphIsomorphismIndexJGraphT<K, V, E, G extends Graph<V, E>>
    extends SubGraphIsomorphismIndexImpl<K, G, V>
{
    //protected Comparator<V> createVertexComparator(BiMap<V, V> baseIso);
    //protected Comparator<E> createEdgeComparator(BiMap<V, V> baseIso);

    public SubGraphIsomorphismIndexJGraphT(
            Supplier<K> keySupplier,
            Function<? super G, Collection<?>> extractGraphTags,
            SetOps<G, V> graphOps,
            IsoMatcher<G, V> isoMatcher) {
        super(keySupplier, graphOps, extractGraphTags, isoMatcher);
//        this.createVertexComparator = createVertexComparator;
//        this.createEdgeComparator = createEdgeComparator;
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
                        new IsoMatcherImpl<>(QueryToJenaGraph::createNodeComparator, QueryToJenaGraph::createEdgeComparator));
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





    public static Set<Node> extractGraphTags(Graph<Node, Triple> graph) {
        Set<Node> result = graph.edgeSet().stream()
            .flatMap(t -> Arrays.asList(t.getSubject(), t.getPredicate(), t.getObject()).stream())
            .filter(n -> n.isURI() || n.isLiteral())
            .collect(Collectors.toSet());

        return result;
    }

//    @Override
    protected static Collection<?> extractGraphTags2(org.apache.jena.graph.Graph graph) {
        // TODO: All nodes does not include predicates
        Set<Node> result = Streams.stream(GraphUtils.allNodes(graph))
                .filter(n -> n.isURI() || n.isLiteral())
                .collect(Collectors.toSet());

        return result;
    }

}
