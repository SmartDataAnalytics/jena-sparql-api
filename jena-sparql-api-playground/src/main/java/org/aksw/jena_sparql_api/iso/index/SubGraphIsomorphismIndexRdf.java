package org.aksw.jena_sparql_api.iso.index;

public class SubGraphIsomorphismIndexRdf<K>
//    extends SubGraphIsomorphismIndexBase<K, Graph, Node>
{
//    public static SubGraphIsomorphismIndexRdf<Node> create() {
//        int i[] = {0};
//        Supplier<Node> idSupplier = () -> NodeFactory.createURI("http://index.node/id" + i[0]++);
//        SubGraphIsomorphismIndexRdf<Node> result = new SubGraphIsomorphismIndexRdf<>(idSupplier);
//        return result;
//    }
//
//    public Map<K, ProblemNeighborhoodAware<BiMap<Var, Var>, Var>> lookupStream(Graph queryGraph, boolean exactMatch) {
//        Multimap<K, InsertPosition<K, Graph, Node>> matches = lookup(queryGraph, exactMatch);
//
//        Map<K, ProblemNeighborhoodAware<BiMap<Var, Var>, Var>> result = matches.asMap().entrySet().stream()
//                .collect(Collectors.toMap(e -> e.getKey(), e -> SparqlViewMatcherQfpcIso.createCompound(e.getValue())));
//
//        return result;
//    }
//
//    public SubGraphIsomorphismIndexRdf(Supplier<K> keySupplier) {
//        super(keySupplier);
//        // TODO Auto-generated constructor stub
//    }
//
//    @Override
//    public Iterable<BiMap<Node, Node>> match(BiMap<Node, Node> baseIso, Graph viewGraph, Graph insertGraph) {
//        Iterable<BiMap<Node, Node>> result = QueryToJenaGraph.match(baseIso, viewGraph, insertGraph).collect(Collectors.toSet());
//        return result;
//    }
//
//    @Override
//    protected Collection<?> extractGraphTags(Graph graph) {
//        Set<Node> result = StreamUtils.stream(GraphUtils.allNodes(graph))
//                .filter(n -> n.isURI() || n.isLiteral())
//                .collect(Collectors.toSet());
//
//        return result;
//    }

}
