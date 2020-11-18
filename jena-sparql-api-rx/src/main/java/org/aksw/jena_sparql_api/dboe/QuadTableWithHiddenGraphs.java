package org.aksw.jena_sparql_api.dboe;

import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;

/**
 * A forwarding QuadTable that hides graphs based on a predicate.
 * Typically used to hide the default graph
 * Attempting to insert into a hidden graph raises an Exception
 *
 * @author raven
 *
 */
public class QuadTableWithHiddenGraphs
    implements QuadTableCore
{
    protected QuadTableCore quadTable;
    protected Predicate<Node> hiddenGraph;

    public QuadTableWithHiddenGraphs(QuadTableCore quadTable, Predicate<Node> hiddenGraph) {
        super();
        this.quadTable = quadTable;
        this.hiddenGraph = hiddenGraph;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(Quad quad) {
        if (hiddenGraph.test(quad.getGraph())) {
            throw new IllegalArgumentException("Insert rejected by filter");
        }

        quadTable.add(quad);
    }

    @Override
    public void delete(Quad quad) {
        if (!hiddenGraph.test(quad.getGraph())) {
            quadTable.delete(quad);
        }
    }

    @Override
    public Stream<Quad> find(Node g, Node s, Node p, Node o) {
        Stream<Quad> result = hiddenGraph.test(g)
                ? Stream.empty()
                : quadTable.find(g, s, p, o).filter(quad -> !hiddenGraph.test(quad.getGraph()));

        return result;
    }

    @Override
    public Stream<Node> listGraphNodes() {
        return quadTable.listGraphNodes()
            .filter(node -> !hiddenGraph.test(node));
    }

}