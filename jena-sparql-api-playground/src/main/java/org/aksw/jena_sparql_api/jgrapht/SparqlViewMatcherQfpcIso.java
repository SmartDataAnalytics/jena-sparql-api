package org.aksw.jena_sparql_api.jgrapht;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.aksw.combinatorics.solvers.ProblemNeighborhoodAware;
import org.aksw.jena_sparql_api.algebra.utils.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.concept_cache.dirty.QfpcMatch;
import org.aksw.jena_sparql_api.concept_cache.dirty.SparqlViewMatcherQfpc;
import org.aksw.jena_sparql_api.deprecated.iso.index.ProblemNodeMappingGraph;
import org.aksw.jena_sparql_api.deprecated.iso.index.ProblemUnion;
import org.aksw.jena_sparql_api.deprecated.iso.index.ProblemVarWrapper;
import org.aksw.jena_sparql_api.deprecated.iso.index.SubGraphIsomorphismIndex;
import org.aksw.jena_sparql_api.deprecated.iso.index.SubGraphIsomorphismIndexBase;
import org.aksw.jena_sparql_api.iso.index.InsertPosition;
import org.aksw.jena_sparql_api.jgrapht.transform.GraphVar;
import org.aksw.jena_sparql_api.jgrapht.transform.GraphVarImpl;
import org.aksw.jena_sparql_api.jgrapht.transform.QueryToGraphVisitor;
import org.aksw.jena_sparql_api.jgrapht.transform.QueryToJenaGraph;
import org.aksw.jena_sparql_api.jgrapht.wrapper.PseudoGraphJenaGraph;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.SimpleDirectedGraph;

import com.google.common.collect.BiMap;

public class SparqlViewMatcherQfpcIso<K>
    implements SparqlViewMatcherQfpc<K>
{
    protected SubGraphIsomorphismIndex<K> graphIndex;
    protected Map<K, QuadFilterPatternCanonical> keyToCq;

    public SparqlViewMatcherQfpcIso() {
        this.graphIndex = new SubGraphIsomorphismIndexBase<>(null);//keySupplier);
        this.keyToCq = new HashMap<>();
    }

    public static Graph queryToGraph(QuadFilterPatternCanonical queryQfpc) {
        Supplier<Supplier<Node>> ssn = () -> { int[] x = {0}; return () -> NodeFactory.createBlankNode("_" + x[0]++); };

        QueryToGraphVisitor visitor = new ExtendedQueryToGraphVisitor(ssn.get());
//        OpExtConjunctiveQuery op = new OpExtConjunctiveQuery(cq);
//        op.visit(visitor);
        queryQfpc.getQuads();
        GraphVar result = visitor.getGraph();

        return result;
    }
//
//    public static Map<Var, Var> createVarMap(Map<Node, Node> map) {
//        Map<Var, Var> result = map.entrySet().stream()
//            .filter(e -> e.getKey().isVariable() && e.getValue().isVariable())
//            .collect(Collectors.toMap(
//                e -> (Var)e.getValue(),
//                e -> (Var)e.getKey()));
//
//        return result;
//    }

    public static ProblemNeighborhoodAware<BiMap<Var, Var>, Var> toProblem(InsertPosition<?, Graph, Node> pos) {
        // TODO This is making the hacky index structure clean
        Graph residualQueryGraph = pos.getResidualQueryGraph();
        Graph residualViewGraph = new GraphVarImpl();//pos.getNode().getValue(); //new GraphIsoMapImpl(pos.getNode().getValue(), pos.getNode().getTransIso()); //pos.getNode().getValue();
        BiMap<Node, Node> baseIso = pos.getIso();

        DirectedGraph<Node, Triple> viewGraphGraphView = new PseudoGraphJenaGraph(residualViewGraph);
        DirectedGraph<Node, Triple> queryGraphGraphView = new PseudoGraphJenaGraph(residualQueryGraph);

        // Create a copy of the wrapped graph, as we will have to re-compute a lot of data with the view
        DirectedGraph<Node, Triple> viewGraph = new SimpleDirectedGraph<>(Triple.class);
        DirectedGraph<Node, Triple> queryGraph = new SimpleDirectedGraph<>(Triple.class);

        Graphs.addGraph(viewGraph, viewGraphGraphView);
        Graphs.addGraph(queryGraph, queryGraphGraphView);

        ProblemNodeMappingGraph<Node, Triple, DirectedGraph<Node, Triple>, Node> rawProblem = new ProblemNodeMappingGraph<>(
                baseIso, viewGraph, queryGraph,
                QueryToJenaGraph::createNodeComparator, QueryToJenaGraph::createEdgeComparator);


        System.out.println("RAW SOLUTIONS for " + pos.getNode().getKey());
        rawProblem.generateSolutions().forEach(s -> {
            System.out.println("  Raw Solution: " + s);
        });

        ProblemNeighborhoodAware<BiMap<Var, Var>, Var> result = new ProblemVarWrapper(rawProblem);

        return result;
    }

    public static ProblemNeighborhoodAware<BiMap<Var, Var>, Var> createCompound(Collection<? extends InsertPosition<?, Graph, Node>> poss) {
        List<ProblemNeighborhoodAware<BiMap<Var, Var>, Var>> problems = poss.stream().map(SparqlViewMatcherQfpcIso::toProblem).collect(Collectors.toList());

        ProblemUnion<BiMap<Var, Var>, Var> result = new ProblemUnion<>(problems);
        return result;
    }

    @Override
    public Map<K, QfpcMatch> lookup(QuadFilterPatternCanonical queryQfpc) {
//        Graph queryGraph = queryToGraph(queryQfpc);
//        Multimap<K, InsertPosition<K>> matches = graphIndex.lookup(queryGraph);
//
//        Map<K, ProblemNeighborhoodAware<BiMap<Var, Var>, Var>> result = matches.asMap().entrySet().stream().collect(Collectors.toMap(
//            e -> e.getKey(),
//            e -> SparqlViewMatcherQfpcIso.createCompound(e.getValue())));
//
//        ProblemNeighborhoodAware<BiMap<Var, Var>, Var> problems;
//
//
//
//
//        //Multimap<K, QfpcMatch> result = LinkedHashMultimap.create();//new LinkedHashMap<>();
//
//        Map<K, QfpcMatch> result = new HashMap<>();
//
//        matches.entries().stream().forEach(e -> {
//            K key = e.getKey();
//            Map<Var, Var> varMap = createVarMap(e.getValue());
//
//            QuadFilterPatternCanonical cand = keyToCq.get(key);
//
//            NodeTransform rename = new NodeTransformRenameMap(varMap);
//
//            QuadFilterPatternCanonical candRename = cand.applyNodeTransform(rename);
//
//            boolean isSubsumed = candRename.isSubsumedBy(queryQfpc);
//
//            if(isSubsumed) {
//                //QuadFilterPatternCanonical diffPattern = candRename.diff(queryQfpc);
//                QuadFilterPatternCanonical diffPattern = queryQfpc.diff(candRename);
//
//                QfpcMatch cacheHit = new QfpcMatch(candRename, diffPattern, varMap);
//
//                result.put(key, cacheHit);
//            }
//        });
//
//        // for now only return the first result for each key
//

        return null;
    }

    @Override
    public void put(K key, QuadFilterPatternCanonical queryQfpc) {
        Graph graph = queryToGraph(queryQfpc);

        graphIndex.put(key, graph);
    }

    @Override
    public void removeKey(Object key) {
        graphIndex.removeKey(key);
        keyToCq.remove(key);
    }

}
