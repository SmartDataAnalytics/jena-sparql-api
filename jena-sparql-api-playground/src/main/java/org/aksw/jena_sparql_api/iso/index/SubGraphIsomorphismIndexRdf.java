package org.aksw.jena_sparql_api.iso.index;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.aksw.combinatorics.solvers.ProblemNeighborhoodAware;
import org.aksw.jena_sparql_api.jgrapht.SparqlViewMatcherQfpcIso;
import org.aksw.jena_sparql_api.jgrapht.transform.GraphIsoMapImpl;
import org.aksw.jena_sparql_api.jgrapht.transform.GraphVarImpl;
import org.aksw.jena_sparql_api.jgrapht.transform.QueryToJenaGraph;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.compose.Difference;
import org.apache.jena.graph.compose.Intersection;
import org.apache.jena.sparql.core.Var;

import com.google.common.collect.BiMap;
import com.google.common.collect.Multimap;

public class SubGraphIsomorphismIndexRdf<K>
    extends SubGraphIsomorphismIndexBase<K, Graph, Node>
{
    public static SubGraphIsomorphismIndexRdf<Node> create() {
        int i[] = {0};
        Supplier<Node> idSupplier = () -> NodeFactory.createURI("http://index.node/id" + i[0]++);
        SubGraphIsomorphismIndexRdf<Node> result = new SubGraphIsomorphismIndexRdf<>(idSupplier);
        return result;
    }

    public Map<K, ProblemNeighborhoodAware<BiMap<Var, Var>, Var>> lookupStream(Graph queryGraph, boolean exactMatch) {
        Multimap<K, InsertPosition<K, Graph, Node>> matches = lookup(queryGraph, exactMatch);

        Map<K, ProblemNeighborhoodAware<BiMap<Var, Var>, Var>> result = matches.asMap().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> SparqlViewMatcherQfpcIso.createCompound(e.getValue())));

        return result;
    }

    public SubGraphIsomorphismIndexRdf(Supplier<K> keySupplier) {
        super(keySupplier);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Graph createSet() {
        return new GraphVarImpl();
    }

    @Override
    public Graph applyIso(Graph set, BiMap<Node, Node> iso) {
        Graph result = new GraphIsoMapImpl(set, iso);
        return result;
    }

    @Override
    public int size(Graph set) {
        int result = set.size();
        return result;
    }

    @Override
    public Graph difference(Graph baseSet, Graph removalSet) {
        Graph result = new Difference(baseSet, removalSet);
        return result;
    }

    @Override
    public Graph intersection(Graph a, Graph b) {
        Graph result = new Intersection(a, b);
        return result;
    }

    @Override
    public Iterable<BiMap<Node, Node>> match(BiMap<Node, Node> baseIso, Graph viewGraph, Graph insertGraph) {
        Iterable<BiMap<Node, Node>> result = QueryToJenaGraph.match(baseIso, viewGraph, insertGraph).collect(Collectors.toSet());
        return result;
    }

}
