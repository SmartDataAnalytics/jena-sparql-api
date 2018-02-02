package org.aksw.jena_sparql_api.concept_cache.op;

import java.util.List;

import org.aksw.jena_sparql_api.algebra.utils.OpUtils;
import org.apache.jena.sparql.algebra.Op;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

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
            result.addVertex(child);
            result.addEdge(op, child);

            convert(child, result);
        }
    }


}
