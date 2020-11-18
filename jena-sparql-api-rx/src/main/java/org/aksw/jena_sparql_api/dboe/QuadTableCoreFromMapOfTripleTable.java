package org.aksw.jena_sparql_api.dboe;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;

public class QuadTableCoreFromMapOfTripleTable
    implements QuadTableCore
{
    protected Map<Node, TripleTableCore> store;
    protected Supplier<? extends TripleTableCore> tripleTableSupplier;

    public QuadTableCoreFromMapOfTripleTable(Supplier<? extends TripleTableCore> tripleTableSupplier) {
        this(new LinkedHashMap<>(), tripleTableSupplier);
    }

    public QuadTableCoreFromMapOfTripleTable(Map<Node, TripleTableCore> store, Supplier<? extends TripleTableCore> tripleTableSupplier) {
        super();
        this.store = store;
        this.tripleTableSupplier = tripleTableSupplier;
    }

    @Override
    public void clear() {
        store.clear();
    }

    @Override
    public void add(Quad quad) {
        TripleTableCore tripleTable = store.computeIfAbsent(quad.getGraph(), g -> tripleTableSupplier.get());
        tripleTable.add(quad.asTriple());
    }

    @Override
    public void delete(Quad quad) {
        TripleTableCore tripleTable = store.get(quad.getGraph());
        if (tripleTable != null) {
            tripleTable.delete(quad.asTriple());
        }
    }

    @Override
    public Stream<Quad> find(Node g, Node s, Node p, Node o) {
        return find(store, g, s, p, o);
    }

    @Override
    public Stream<Node> listGraphNodes() {
        return store.keySet().stream();
    }

    public static Stream<Quad> find(
            Map<Node, TripleTableCore> store,
            Node g, Node s, Node p, Node o)
    {
        Stream<Quad> result = QuadTableCoreFromNestedMapsImpl.matchEntries(Stream.of(store), QuadTableCoreFromNestedMapsImpl::isWildcard, g)
                            .flatMap(e -> e.getValue().find(s, p, o).map(triple -> new Quad(e.getKey(), triple)));

        return result;
    }

}
