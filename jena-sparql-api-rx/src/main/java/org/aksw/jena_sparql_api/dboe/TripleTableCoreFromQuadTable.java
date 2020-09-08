package org.aksw.jena_sparql_api.dboe;

import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;

public class TripleTableCoreFromQuadTable
    implements TripleTableCore
{
    protected QuadTableCore quadTable;
    protected Node graphNode;

    public TripleTableCoreFromQuadTable(QuadTableCore quadTable, Node graphNode) {
        super();
        this.quadTable = quadTable;
        this.graphNode = graphNode;
    }

    @Override
    public void clear() {
        quadTable.deleteGraph(graphNode);
    }

    @Override
    public void add(Triple triple) {
        quadTable.add(Quad.create(graphNode, triple));
    }

    @Override
    public void delete(Triple triple) {
        quadTable.delete(Quad.create(graphNode, triple));
    }

    @Override
    public Stream<Triple> find(Node s, Node p, Node o) {
        return quadTable.find(graphNode, s, p, o)
                .map(Quad::asTriple);
    }
}