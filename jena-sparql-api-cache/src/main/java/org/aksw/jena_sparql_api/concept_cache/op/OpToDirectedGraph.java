package org.aksw.jena_sparql_api.concept_cache.op;

import java.util.List;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph.CycleFoundException;
import org.jgrapht.graph.DefaultEdge;

import org.apache.jena.sparql.algebra.Op;

public class OpToDirectedGraph {



    public static DirectedAcyclicGraph<Op, DefaultEdge> convert(Op op) {
        DirectedAcyclicGraph<Op, DefaultEdge> result = new DirectedAcyclicGraph<Op, DefaultEdge>(DefaultEdge.class);

        result.addVertex(op);
        convert(op, result);

        return result;
    }

    // Note: op is assumed to already be a vertex in the graph
    public static void convert(Op op, DirectedAcyclicGraph<Op, DefaultEdge> result)
    {

        List<Op> children = OpUtils.getSubOps(op);
        for(Op child : children) {
            try {
                result.addVertex(child);
                result.addDagEdge(op, child);
            } catch (CycleFoundException e) {
                throw new RuntimeException(e);
            }

            convert(child, result);
        }
    }


}
