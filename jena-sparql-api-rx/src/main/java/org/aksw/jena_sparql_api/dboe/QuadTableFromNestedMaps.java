package org.aksw.jena_sparql_api.dboe;

import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.core.Transactional.Promote;
import org.apache.jena.sparql.core.mem.QuadTable;


/**
 * A simple QuadTable implementation for use cases where insert order sensitivity
 * is more important than performance.
 * This implementation uses nested LinkedHashMaps and therefore does not preserve insert order but it is sensitive to it
 *
 * @author Claus Stadler, Oct 30, 2018
 *
 */
public class QuadTableFromNestedMaps
    implements QuadTable, Transactional
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
         return new QuadTableCoreFromNestedMapsImpl();
    }

    @Override
    public void clear() {
        TxnState txnState = local().get();
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
            local().set(txnState);
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

    @Override
    public void begin(TxnType type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean promote(Promote mode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ReadWrite transactionMode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TxnType transactionType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isInTransaction() {
        TxnState txnState = local().get();
        boolean result = txnState != null;
        return result;
    }

    @Override
    public void abort() {
        throw new UnsupportedOperationException();
    }

}
