package org.aksw.jena_sparql_api.dboe;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.dboe.QuadTableCoreFromNestedMapsImpl.MapSupplier;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public class TripleTableCoreFromNestedMapsImpl
    implements TripleTableCore
{
    protected Map<Node, Map<Node, Map<Node, Triple>>> store;
    protected MapSupplier mapSupplier;

    public TripleTableCoreFromNestedMapsImpl() {
        this(LinkedHashMap::new);
    }

    public TripleTableCoreFromNestedMapsImpl(MapSupplier mapSupplier) {
        super();
        this.mapSupplier = mapSupplier;

        // Initialize
        store = mapSupplier.newMap();
    }

    @Override
    public void clear() {
        store.clear();
    }

    @Override
    public void add(Triple triple) {
        add(store, triple, mapSupplier);
    }

    @Override
    public void delete(Triple triple) {
        delete(store, triple);
    }

    @Override
    public Stream<Triple> find(Node s, Node p, Node o) {
        return find(store, s, p, o);
    }


    /**
     * Stream all triples from nested maps
     *
     * @param store
     * @param triple
     */
    public static <T> Stream<T> find(
            Map<Node, Map<Node, Map<Node, T>>> store,
            Node s, Node p, Node o)
    {
        Stream<T> result =
                    QuadTableCoreFromNestedMapsImpl.match(
                            QuadTableCoreFromNestedMapsImpl.match(
                                    QuadTableCoreFromNestedMapsImpl.match(Stream.of(store), QuadTableCoreFromNestedMapsImpl::isWildcard, s),
                                    QuadTableCoreFromNestedMapsImpl::isWildcard, p),
                            QuadTableCoreFromNestedMapsImpl::isWildcard, o);

        return result;
    }

    /**
     * Add a triple to nested maps
     *
     * @param store
     * @param triple
     */
    public static void add(
        Map<Node, Map<Node, Map<Node, Triple>>> store,
        Triple triple,
        MapSupplier mapSupplier
    ) {
        store
            .computeIfAbsent(triple.getSubject(), s -> mapSupplier.newMap())
            .computeIfAbsent(triple.getPredicate(), p -> mapSupplier.newMap())
            .computeIfAbsent(triple.getObject(), o -> triple);
    }

    public static boolean contains(
            Map<Node, Map<Node, Map<Node, Map<Node, Triple>>>> store,
            Triple triple
        ) {
            boolean result = store
                .getOrDefault(triple.getSubject(), Collections.emptyMap())
                .getOrDefault(triple.getPredicate(), Collections.emptyMap())
                .containsKey(triple.getObject());

            return result;
        }

    /**
     * Delete a triple from nested maps
     *
     * @param store
     * @param triple
     */
    public static void delete(
        Map<Node, Map<Node, Map<Node, Triple>>> store,
        Triple triple)
    {
        Map<Node, Map<Node, Triple>> pm = store.getOrDefault(triple.getSubject(), Collections.emptyMap());
        Map<Node, Triple> om = pm.getOrDefault(triple.getPredicate(), Collections.emptyMap());

        if(om.containsKey(triple.getObject())) {
            om.remove(triple.getObject());
            if(om.isEmpty()) { pm.remove(triple.getPredicate()); }
            if(pm.isEmpty()) { store.remove(triple.getSubject()); }
        }
    }



//  public static Map<Node, Map<Node, Map<Node, Map<Node, Triple>>>> copy(
//  Stream<Triple> stream,
//  MapSupplier mapSupplier
//) {
//  Map<Node, Map<Node, Map<Node, Map<Node, Triple>>>> result = mapSupplier.newMap();
//  stream.forEach(triple -> add(result, triple, mapSupplier));
//
//  return result;
//}

}
