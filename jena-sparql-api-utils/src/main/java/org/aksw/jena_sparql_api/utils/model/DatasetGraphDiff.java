package org.aksw.jena_sparql_api.utils.model;

import static org.apache.jena.query.ReadWrite.WRITE;
import static org.apache.jena.system.Txn.executeWrite;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.aksw.commons.collections.sets.SetIterator;
import org.apache.jena.ext.com.google.common.collect.Iterators;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.compose.Delta;
import org.apache.jena.graph.compose.Difference;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphBase;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.GraphView;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Transactional;


/**
 * A DatasetGraph that tracks insertions / deletions to a base graph in separate
 * DatasetGraphs.
 *
 * Similar to {@link Delta} which however is for Graphs.
 *
 * Does not support transactions.
 *
 *
 * @author raven
 *
 */
public class DatasetGraphDiff
    extends DatasetGraphBase
{
    protected DatasetGraph base;

    protected DatasetGraph added;
    protected DatasetGraph removed;

//    protected boolean allowEmptyGraphs;

    protected TransactionalSet<Node> removedGraphs;
    protected TransactionalSet<Node> addedGraphs;

    protected GraphView defaultGraphViewCache = GraphView.createDefaultGraph(this);
    protected Map<Node, GraphView> namedGraphViewCache = Collections.synchronizedMap(new HashMap<>());

    public DatasetGraphDiff() {
        this(DatasetGraphFactory.createTxnMem());
    }

    public DatasetGraphDiff(DatasetGraph base) {
        super();
        this.base = base;
        this.added = DatasetGraphFactory.createTxnMem();
        this.removed = DatasetGraphFactory.createTxnMem();

//        this.allowEmptyGraphs = allowEmptyGraphs;

        this.removedGraphs = new TransactionalSetImpl<>();
        this.addedGraphs = new TransactionalSetImpl<>();
    }


    public DatasetGraph getBase() {
        return base;
    }

    public DatasetGraph getAdded() {
        return added;
    }

    public DatasetGraph getRemoved() {
        return removed;
    }

    public Set<Node> getRemovedGraphs() {
        return removedGraphs;
    }

    public Set<Node> getAddedGraphs() {
        return addedGraphs;
    }

    @Override
    public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
        Iterator<Quad> itAdded = added.find(g, s, p, o);

        Iterator<Quad> result = base.find(g, s, p, o);

        result = Iterators.filter(result, quad -> !removed.contains(quad));
        result = Iterators.concat(result, itAdded);

        return result;
    }

    @Override
    public Iterator<Quad> findNG(Node g, Node s, Node p, Node o) {

        Iterator<Quad> itAdded = added.findNG(g, s, p, o);

        Iterator<Quad> result = base.findNG(g, s, p, o);
        result = Iterators.filter(result, quad -> !removed.contains(quad));
        result = Iterators.concat(result, itAdded);

        return result;
    }

    public void add(Quad quad) {
        removed.delete(quad);
        added.add(quad);
    }

    @Override
    public void delete(Quad quad) {
        added.delete(quad);
        removed.add(quad);
    }

    @Override
    public GraphView getDefaultGraph() {
        return defaultGraphViewCache;
    }

    @Override
    public GraphView getGraph(Node graphNode) {
        GraphView result = namedGraphViewCache.computeIfAbsent(graphNode,
                n -> GraphView.createNamedGraph(this, n));

        return result;
    }


    @Override
    public Iterator<Node> listGraphNodes() {
        Iterator<Node> result = base.listGraphNodes();
        // TODO Add flag to treat empty graphs as effectively removed
        result = Iterators.filter(result, node -> !removedGraphs.contains(node));

        Set<Node> effectiveAddedGraphs = new LinkedHashSet<Node>(addedGraphs);
        added.listGraphNodes().forEachRemaining(effectiveAddedGraphs::add);

        result = Iterators.concat(result, effectiveAddedGraphs.iterator());


        return result;
    }

    @Override
    public void addGraph(Node graphNode, Graph graph) {
        removedGraphs.remove(graphNode);

        if (base.containsGraph(graphNode)) {
            Graph existing = base.getGraph(graphNode);

            added.addGraph(graphNode, new Difference(graph, existing));
            removed.addGraph(graphNode, new Difference(existing, graph));

        } else {
            addedGraphs.add(graphNode);

            removed.removeGraph(graphNode);
            added.addGraph(graphNode, graph);
        }
    }

    @Override
    public void removeGraph(Node graphNode) {
        addedGraphs.remove(graphNode);

        if (base.containsGraph(graphNode)) {
            removedGraphs.add(graphNode);

            added.removeGraph(graphNode);

        } else {
            added.removeGraph(graphNode);

            // Sanity check: If the graph did not exist is base then
            // the set of removals must be empty
            // if (!core.containsGraph(graphNode)) { assert removed.isEmpty(); }
            // removed.removeGraph(graphNode);
        }

        added.removeGraph(graphNode);
        removed.removeGraph(graphNode);
    }

    public void materialize() {
        mutate(this, DatasetGraphDiff::_materialize, this);
    }

    protected void _materialize() {
        removed.find().forEachRemaining(base::delete);
        added.find().forEachRemaining(base::add);

        added.clear();
        addedGraphs.clear();
        removed.clear();
        removedGraphs.clear();
    }

    @Override
    public boolean supportsTransactions() {
        return true;
    }

    @Override
    public boolean supportsTransactionAbort() {
        return true;
    }

    @Override
    public void abort() {
        addedGraphs.abort();
        removedGraphs.abort();
        base.abort();
        added.abort();
        removed.abort();
    }

    @Override
    public void begin(ReadWrite mode) {
        base.begin(mode);
        added.begin(mode);
        removed.begin(mode);
        addedGraphs.begin(mode);
        removedGraphs.begin(mode);
    }

    @Override
    public void commit() {
        removedGraphs.commit();
        addedGraphs.commit();
        removed.commit();
        added.commit();
        base.commit();
    }

    @Override
    public void end() {
        removedGraphs.end();
        addedGraphs.end();
        removed.end();
        added.end();
        base.end();
    }

    @Override
    public boolean isInTransaction() {
        return base.isInTransaction();
    }

    @Override
    public void begin(TxnType type) {
        base.begin(type);
        added.begin(type);
        removed.begin(type);
        addedGraphs.begin(type);
        removedGraphs.begin(type);
    }

    @Override
    public boolean promote(Promote mode) {
        added.promote(mode);
        removed.promote(mode);
        addedGraphs.promote(mode);
        removedGraphs.promote(mode);
        boolean result = base.promote(mode);
        return result;
    }

    @Override
    public ReadWrite transactionMode() {
        ReadWrite result = base.transactionMode();
        return result;
    }

    @Override
    public TxnType transactionType() {
        TxnType result = base.transactionType();
        return result;
    }


    public static <T> void mutate(Transactional target, final Consumer<T> mutator, final T payload) {
        if (target.isInTransaction()) {
            if (!target.transactionMode().equals(WRITE)) {
                TxnType mode = target.transactionType();
                switch (mode) {
                case WRITE:
                    break;
                case READ:
                    throw new JenaTransactionException("Tried to write inside a READ transaction!");
                case READ_COMMITTED_PROMOTE:
                case READ_PROMOTE:
                    throw new RuntimeException("promotion not implemented");
//                    boolean readCommitted = (mode == TxnType.READ_COMMITTED_PROMOTE);
//                    promote(readCommitted);
                    //break;
                }
            }

            mutator.accept(payload);
        } else executeWrite(target, () -> {
            mutator.accept(payload);
        });
    }

}
