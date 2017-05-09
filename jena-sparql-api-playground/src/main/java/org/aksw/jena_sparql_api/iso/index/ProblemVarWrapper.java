package org.aksw.jena_sparql_api.iso.index;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.combinatorics.solvers.ProblemNeighborhoodAware;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class ProblemVarWrapper
    implements ProblemNeighborhoodAware<BiMap<Var, Var>, Var>
{
    protected ProblemNeighborhoodAware<BiMap<Node, Node>, Node> core;

    public ProblemVarWrapper(ProblemNeighborhoodAware<BiMap<Node, Node>, Node> core) {
        super();
        this.core = core;
    }


    public static BiMap<Node, Node> createNodeMap(BiMap<? extends Node, ? extends Node> map) {
        BiMap<Node, Node> result = map.entrySet().stream()
            .collect(Collectors.toMap(
                e -> e.getKey(),
                e -> e.getValue(),
                (u, v) -> { throw new RuntimeException("conflict for " + u + " " + v); },
                HashBiMap::create
            ));

        return result;
    }

    public static BiMap<Var, Var> createVarMap(BiMap<Node, Node> map) {
        BiMap<Var, Var> result = map.entrySet().stream()
            .filter(e -> e.getKey().isVariable() && e.getValue().isVariable())
            .collect(Collectors.toMap(
                e -> (Var)e.getKey(),
                e -> (Var)e.getValue(),
                (u, v) -> { throw new RuntimeException("conflict for " + u + " " + v); },
                HashBiMap::create
            ));

        return result;
    }


    @Override
    public Stream<BiMap<Var, Var>> generateSolutions() {
        Stream<BiMap<Var, Var>> result = core.generateSolutions().map(ProblemVarWrapper::createVarMap);
        return result;
    }

    @Override
    public Collection<? extends ProblemNeighborhoodAware<BiMap<Var, Var>, Var>> refine(
            BiMap<Var, Var> partialSolution) {

        BiMap<Node, Node> nodeMap = createNodeMap(partialSolution);
        Collection<? extends ProblemNeighborhoodAware<BiMap<Node, Node>, Node>> tmp = core.refine(nodeMap);

        Collection<? extends ProblemNeighborhoodAware<BiMap<Var, Var>, Var>> result =
                tmp.stream().map(entry -> new ProblemVarWrapper(entry)).collect(Collectors.toList());

        return result;
    }

    @Override
    public boolean isEmpty() {
        //generateSolutions().
        boolean result = core.isEmpty();
        return result;
    }

    @Override
    public long getEstimatedCost() {
        long result = core.getEstimatedCost();
        return result;
    }

    @Override
    public Collection<Var> getSourceNeighbourhood() {
        throw new UnsupportedOperationException();
    }

}
