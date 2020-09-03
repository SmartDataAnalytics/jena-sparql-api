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
 * This is somewhat a mix of QuadTable and StorageRDF but distilled to the essential functions
 * Using the Quad.defaultGraph is permitted at this stage
 *
 * @author raven
 *
 */
interface QuadTableCore {
    void clear();
    void add(Quad quad);
    void delete(Quad quad);
    Stream<Quad> find(Node g, Node s, Node p, Node o);

    Stream<Node> listGraphNodes();

    default boolean contains(Quad quad) {
        boolean result = find(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject())
            .findAny().isPresent();
        return result;
    }

}

/**
 * A diff based StorageRDF similar to the Delta graph
 *
 * @author raven
 *
 */
class QuadTableCoreDiff
    implements QuadTableCore
{
    protected QuadTableCore master;
    protected QuadTableCore additions;
    protected QuadTableCore deletions;

    public void clearDiff() {
        additions.clear();
        deletions.clear();
    }

    public void applyDiff() {
        applyDiff(master, additions, deletions);
        clearDiff();
    }

    public static void applyDiff(QuadTableCore target, QuadTableCore additions, QuadTableCore deletions) {
        deletions.find(Node.ANY, Node.ANY, Node.ANY, Node.ANY).forEach(target::delete);
        additions.find(Node.ANY, Node.ANY, Node.ANY, Node.ANY).forEach(target::add);
    }

    public QuadTableCoreDiff(QuadTableCore master, QuadTableCore additions, QuadTableCore deletions) {
        this.master = master;
        this.additions = additions;
        this.deletions = deletions;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Invocation of .clear() on a diff is not supported; you may use .clearDiff()");
    }

    @Override
    public void add(Quad quad) {
        additions.add(quad);
        deletions.delete(quad);
    }

    @Override
    public void delete(Quad quad) {
        deletions.add(quad);
        additions.delete(quad);
    }

    @Override
    public Stream<Quad> find(Node g, Node s, Node p, Node o) {
        return Stream.concat(
                master.find(g, s, p, o).filter(q -> !deletions.contains(q)),
                additions.find(g, s, p, o).filter(q -> !master.contains(q)));
    }

    @Override
    public Stream<Node> listGraphNodes() {
        return
            Stream.concat(master.listGraphNodes(), additions.listGraphNodes()).distinct()
                .filter(g -> {
                    boolean r = true; // may become false
                    boolean hasDeletionsInG = deletions.find(g, Node.ANY, Node.ANY, Node.ANY).findAny().isPresent();
                    if (hasDeletionsInG) {
                        // For graph g test if there is any triple in master+additions that is not in deletions
                        r = Stream.concat(
                                master.find(g, Node.ANY, Node.ANY, Node.ANY),
                                additions.find(g, Node.ANY, Node.ANY, Node.ANY))
                            .filter(q -> !deletions.contains(q))
                            .findAny().isPresent();
                    }
                    return r;
                });
    }

}

/**
 * A simple implementation of a QuadTableCore using nested maps
 *
 * @author raven
 *
 */
class QuadTableCoreImpl
    implements QuadTableCore
{
    public static interface MapSupplier {
        <K, V> Map<K, V> newMap();
    }

    protected Map<Node, Map<Node, Map<Node, Map<Node, Quad>>>> store;
    protected MapSupplier mapSupplier;

    public QuadTableCoreImpl() {
        this(LinkedHashMap::new);
    }

    public QuadTableCoreImpl(MapSupplier mapSupplier) {
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
                            match(Stream.of(store), QuadTableCoreImpl::isWildcard, g),
                            QuadTableCoreImpl::isWildcard, s),
                        QuadTableCoreImpl::isWildcard, p),
                    QuadTableCoreImpl::isWildcard, o);

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


/**
 * A simple QuadTable implementation for use case insert order sensitivity
 * is more important than performance.
 * This implementation uses nested LinkedHashMaps and therefore does not preserve insert order but it is sensitive to it
 *
 * Beginning and committing a WRITE transaction performs a full copy of the data and
 * should therefore be used sparsely.
 *
 * @author Claus Stadler, Oct 30, 2018
 *
 */
public class QuadTableFromNestedMaps
    implements QuadTable
{
    public static class TxnState {
        ReadWrite mode;
        QuadTableCoreDiff diff;
    }

    protected AtomicReference<QuadTableCore> master = new AtomicReference<>(newQuadStore());
    protected ThreadLocal<TxnState> local = ThreadLocal.withInitial(() -> null);

    protected AtomicReference<QuadTableCore> master() {
        return master;
    }

    protected ThreadLocal<TxnState> local() {
        return local;
    }


    public QuadTableFromNestedMaps() {
        super();
    }

    protected QuadTableCore newQuadStore() {
         return new QuadTableCoreImpl();
    }

    @Override
    public void clear() {
        TxnState txnState = local.get();
        txnState.diff.master.find(Node.ANY, Node.ANY, Node.ANY, Node.ANY)
            .forEach(txnState.diff.deletions::add);
    }

    @Override
    public void add(Quad quad) {
        local().get().diff.add(quad);
    }

    @Override
    public void delete(Quad quad) {
        local().get().diff.delete(quad);
    }

    @Override
    public void begin(ReadWrite readWrite) {
        // Ignore multiple begin's on the same thread
        // The purpose is to allow wrapping this class with a TripleTable view
        // using new TripleTableFromQuadTable(new QuadTableFromNestedMaps())
        TxnState txnState = local().get();
        if (txnState == null) {
            txnState = new TxnState();
            txnState.mode = readWrite;
            txnState.diff = new QuadTableCoreDiff(master.get(), newQuadStore(), newQuadStore());
            local.set(txnState);
        } else {
            ReadWrite current = txnState.mode;
            if (current != readWrite) {
                throw new IllegalStateException("Requested begin of txn with " + readWrite + " however a prior begin with " + current + " was not ended");
            }
        }
    }

    @Override
    public void commit() {
        TxnState txnState = local().get();

        if (txnState != null) {
            txnState.diff.applyDiff();
        }

        end();
    }

    @Override
    public void end() {
        local().remove();
    }

    @Override
    public Stream<Quad> find(Node g, Node s, Node p, Node o) {
        Stream<Quad> result = local().get().diff.find(g, s, p, o);
        return result;
    }

    @Override
    public Stream<Node> listGraphNodes() {
        // QuadTableCore allows to yield quads in the default graph - but the contract of QuadTable forbids it!
        return local().get().diff.listGraphNodes()
                .filter(node -> !Quad.isDefaultGraph(node));
    }

}
