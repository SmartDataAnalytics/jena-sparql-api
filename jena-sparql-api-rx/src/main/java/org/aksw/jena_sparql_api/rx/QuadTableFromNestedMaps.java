package org.aksw.jena_sparql_api.rx;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.mem.QuadTable;

/**
 * A simple QuadTable implementation where preservation of insert order
 * is more important than performance.
 *
 * @author Claus Stadler, Oct 30, 2018
 *
 */
public class QuadTableFromNestedMaps
    implements QuadTable
{
    protected AtomicReference<Map<Node, Map<Node, Map<Node, Map<Node, Quad>>>>> master = new AtomicReference<>(newMap());
    protected ThreadLocal<Map<Node, Map<Node, Map<Node, Map<Node, Quad>>>>> local = ThreadLocal.withInitial(() -> null);

    protected ThreadLocal<Boolean> isInTxn = ThreadLocal.withInitial(() -> false);

    protected AtomicReference<Map<Node, Map<Node, Map<Node, Map<Node, Quad>>>>> master() {
        return master;
    }

    protected ThreadLocal<Map<Node, Map<Node, Map<Node, Map<Node, Quad>>>>> local() {
        return local;
    }


    public QuadTableFromNestedMaps() {
        super();
    }

    protected <K, V> Map<K, V> newMap() {
        return new LinkedHashMap<>();
    }

    @Override
    public void clear() {
        local().set(newMap());
    }

    @Override
    public void add(Quad quad) {
        local().get()
            .computeIfAbsent(quad.getGraph(), g -> newMap())
            .computeIfAbsent(quad.getSubject(), s -> newMap())
            .computeIfAbsent(quad.getPredicate(), p -> newMap())
            .computeIfAbsent(quad.getObject(), o -> quad);
    }

    @Override
    public void delete(Quad quad) {
        Map<Node, Map<Node, Map<Node, Quad>>> sm = local().get().getOrDefault(quad.getGraph(), Collections.emptyMap());
        Map<Node, Map<Node, Quad>> pm = sm.getOrDefault(quad.getSubject(), Collections.emptyMap());
        Map<Node, Quad> om = pm.getOrDefault(quad.getPredicate(), Collections.emptyMap());

        if(om.containsKey(quad.getObject())) {
            om.remove(quad.getObject());
            if(om.isEmpty()) { pm.remove(quad.getPredicate()); }
            if(pm.isEmpty()) { sm.remove(quad.getSubject()); }
            if(sm.isEmpty()) { local().get().remove(quad.getGraph()); }
        }
    }

    @Override
    public void begin(ReadWrite readWrite) {
        // Ignore multiple begin's on the same thread
        // The purpose is to allow wrapping this class with a TripleTable view
        // using new TripleTableFromQuadTable(new QuadTableFromNestedMaps())
        if (!isInTxn.get()) {
            isInTxn.set(true);
            local().set(master().get());
        }
    }

    @Override
    public void commit() {
        if (isInTxn.get()) {
            master().set(local().get());
        }

        end();
    }

    @Override
    public void end() {
        local().remove();
        isInTxn.remove();
    }

    @Override
    public Stream<Quad> find(Node g, Node s, Node p, Node o) {
        Stream<Quad> result =
                match(
                    match(
                        match(
                            match(Stream.of(local().get()), QuadTableFromNestedMaps::isWildcard, g),
                            QuadTableFromNestedMaps::isWildcard, s),
                        QuadTableFromNestedMaps::isWildcard, p),
                    QuadTableFromNestedMaps::isWildcard, o);

        return result;
    }

    @Override
    public Stream<Node> listGraphNodes() {
        return local().get().keySet().stream()
                .filter(node -> !Quad.isDefaultGraph(node));
    }


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
}
