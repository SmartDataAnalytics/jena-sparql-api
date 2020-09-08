package org.aksw.jena_sparql_api.dboe;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;

/**
 * A simple implementation of a QuadTableCore using nested maps
 *
 * @author raven
 *
 */
public class QuadTableCoreFromNestedMapsImpl
    implements QuadTableCore
{
    public static interface MapSupplier {
        <K, V> Map<K, V> newMap();
    }

    public static interface TripleMapSupplier {
        <K, V> Map<K, V> newMap();
    }

    protected Map<Node, Map<Node, Map<Node, Map<Node, Quad>>>> store;
    protected MapSupplier mapSupplier;

    public QuadTableCoreFromNestedMapsImpl() {
        this(LinkedHashMap::new);
    }

    public QuadTableCoreFromNestedMapsImpl(MapSupplier mapSupplier) {
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
    public void add(Quad quad) {
        add(store, quad, mapSupplier);
    }

    @Override
    public void delete(Quad quad) {
        delete(store, quad);
    }

    @Override
    public boolean contains(Quad quad) {
        boolean result = contains(store, quad);
        return result;
    }

    @Override
    public Stream<Quad> find(Node g, Node s, Node p, Node o) {
        return find(store, g, s, p, o);
    }

    @Override
    public Stream<Node> listGraphNodes() {
        return store.keySet().stream();
    }


    /*
     * Static helper functions
     ***************************/

    public static boolean isWildcard(Node n) {
        return n == null || Node.ANY.equals(n);
    }

    /**
     * Create a stream of matching values from a stream of maps and a key that may be a wildcard
     *
     * @param <K> The map's key type
     * @param <V> The map's value type
     * @param in A stream of input maps
     * @param isAny Predicate whether a key is concrete
     * @param k A key
     * @return
     */
    public static <K, V> Stream<V> match(Stream<Map<K, V>> in, Predicate<? super K> isAny, K k) {
        boolean any = isAny.test(k);
        Stream<V> result = any
                ? in.flatMap(m -> m.values().stream())
                : in.flatMap(m -> m.containsKey(k) ? Stream.of(m.get(k)) : Stream.empty());

        return result;
    }

    /**
     * Create a stream of matching values from a stream of maps and a key that may be a wildcard
     *
     * @param <K> The map's key type
     * @param <V> The map's value type
     * @param in A stream of input maps
     * @param isAny Predicate whether a key is concrete
     * @param k A key
     * @return
     */
    public static <K, V> Stream<Entry<K, V>> matchEntries(Stream<Map<K, V>> in, Predicate<? super K> isAny, K k) {
        boolean any = isAny.test(k);
        Stream<Entry<K, V>> result = any
                ? in.flatMap(m -> m.entrySet().stream())
                : in.flatMap(m -> m.containsKey(k) ? Stream.of(new SimpleEntry<>(k, m.get(k))) : Stream.empty());

        return result;
    }

    /**
     * Stream all quads from nested maps
     *
     * @param store
     * @param quad
     */
    public static Stream<Quad> find(
            Map<Node, Map<Node, Map<Node, Map<Node, Quad>>>> store,
            Node g, Node s, Node p, Node o)
    {
        Stream<Quad> result =
                match(
                    match(
                        match(
                            match(Stream.of(store), QuadTableCoreFromNestedMapsImpl::isWildcard, g),
                            QuadTableCoreFromNestedMapsImpl::isWildcard, s),
                        QuadTableCoreFromNestedMapsImpl::isWildcard, p),
                    QuadTableCoreFromNestedMapsImpl::isWildcard, o);

        return result;
    }

    public static Map<Node, Map<Node, Map<Node, Map<Node, Quad>>>> copy(
        Stream<Quad> stream,
        MapSupplier mapSupplier
    ) {
        Map<Node, Map<Node, Map<Node, Map<Node, Quad>>>> result = mapSupplier.newMap();
        stream.forEach(quad -> add(result, quad, mapSupplier));

        return result;
    }

    /**
     * Add a quad to nested maps
     *
     * @param store
     * @param quad
     */
    public static void add(
        Map<Node, Map<Node, Map<Node, Map<Node, Quad>>>> store,
        Quad quad,
        MapSupplier mapSupplier
    ) {
        store
            .computeIfAbsent(quad.getGraph(), g -> mapSupplier.newMap())
            .computeIfAbsent(quad.getSubject(), s -> mapSupplier.newMap())
            .computeIfAbsent(quad.getPredicate(), p -> mapSupplier.newMap())
            .computeIfAbsent(quad.getObject(), o -> quad);
    }

    public static boolean contains(
            Map<Node, Map<Node, Map<Node, Map<Node, Quad>>>> store,
            Quad quad
        ) {
            boolean result = store
                .getOrDefault(quad.getGraph(), Collections.emptyMap())
                .getOrDefault(quad.getSubject(), Collections.emptyMap())
                .getOrDefault(quad.getPredicate(), Collections.emptyMap())
                .containsKey(quad.getObject());

            return result;
        }

    /**
     * Delete a quad from nested maps
     *
     * @param store
     * @param quad
     */
    public static void delete(
        Map<Node, Map<Node, Map<Node, Map<Node, Quad>>>> store,
        Quad quad)
    {
        Map<Node, Map<Node, Map<Node, Quad>>> sm = store.getOrDefault(quad.getGraph(), Collections.emptyMap());
        Map<Node, Map<Node, Quad>> pm = sm.getOrDefault(quad.getSubject(), Collections.emptyMap());
        Map<Node, Quad> om = pm.getOrDefault(quad.getPredicate(), Collections.emptyMap());

        if(om.containsKey(quad.getObject())) {
            om.remove(quad.getObject());
            if(om.isEmpty()) { pm.remove(quad.getPredicate()); }
            if(pm.isEmpty()) { sm.remove(quad.getSubject()); }
            if(sm.isEmpty()) { store.remove(quad.getGraph()); }
        }
    }

}