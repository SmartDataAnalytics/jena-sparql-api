package org.aksw.jena_sparql_api.rx.entity.model;

import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.binding.Binding;

/**
 * A 'result row' of a partitioned construct query:
 * The key of the partition is a binding,
 * the graph is an RDF graph
 * and roots are a set of nodes in the graph that act
 * as the requested starting points for traversal.
 *
 * @author raven
 *
 */
public class GraphPartitionWithEntities {
    protected Binding binding;
    protected Graph graph;
    protected Set<Node> roots;

    public GraphPartitionWithEntities(Binding binding, Graph graph, Set<Node> roots) {
        super();
        this.binding = binding;
        this.graph = graph;
        this.roots = roots;
    }

    public Binding getBinding() {
        return binding;
    }

    public Graph getGraph() {
        return graph;
    }

    public Set<Node> getRoots() {
        return roots;
    }
}
