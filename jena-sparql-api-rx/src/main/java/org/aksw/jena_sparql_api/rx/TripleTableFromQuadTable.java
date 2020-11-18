package org.aksw.jena_sparql_api.rx;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.mem.QuadTable;
import org.apache.jena.sparql.core.mem.TripleTable;

/**
 * A {@link TripleTable} view on top of a {@link QuadTable}.
 *
 * Used to create a simple insert-order-preserving Dataset view with
 * proper prefix-mapping support using
 *
 * QuadTable quadTable = new QuadTableFromNestedMaps();
 * new DatasetGraphInMemory(quadTable, new TripleTableFromQuadTable(quadTable));
 *
 *
 * @author Claus Stadler, 2020-03-19
 *
 */
public class TripleTableFromQuadTable
    implements TripleTable
{
    protected Node targetGraph;
    protected QuadTable quadTable;

    public TripleTableFromQuadTable(QuadTable quadTable) {
        this(quadTable, Quad.defaultGraphIRI);
    }

    public TripleTableFromQuadTable(QuadTable quadTable, Node targetGraph) {
        super();
        this.quadTable = quadTable;
        this.targetGraph = targetGraph;
    }

    @Override
    public void clear() {
        List<Quad> removals = quadTable.find(targetGraph, null, null, null).collect(Collectors.toList());
        for(Quad q : removals) {
            quadTable.delete(q);
        }
    }

    @Override
    public void add(Triple t) {
        quadTable.add(Quad.create(targetGraph, t));
    }

    @Override
    public void delete(Triple t) {
        quadTable.delete(Quad.create(targetGraph, t));
    }

    @Override
    public void begin(ReadWrite readWrite) {
        quadTable.begin(readWrite);
    }

    @Override
    public void commit() {
        quadTable.commit();
    }

    @Override
    public void end() {
        quadTable.end();
    }

    @Override
    public Stream<Triple> find(Node s, Node p, Node o) {
        return quadTable.find(targetGraph, s, p, o)
                .map(Quad::asTriple);
    }
}
