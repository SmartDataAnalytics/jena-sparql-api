package org.aksw.jena_sparql_api.utils.model;

import static org.apache.jena.query.ReadWrite.WRITE;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.shared.Lock;
import org.apache.jena.shared.LockMRPlusSW;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.system.Txn;

/**
 * A wrapper for a collection that provides transactions using MRSW locking.
 *
 * Also uses copy-on-write; i.e. the thread with the write transaction creates a full
 * copy of the underlying collection - which is far from optimal.
 *
 * @author raven
 *
 * @param <T>
 * @param <C>
 */
public class TransactionalCollectionImpl<T, C extends Collection<T>>
    extends AbstractCollection<T>
    implements TransactionalCollection<T>
{
    private Lock transactionLock = new LockMRPlusSW();
//    private final ReentrantLock systemLock = new ReentrantLock(true);

    protected AtomicReference<C> master = new AtomicReference<>();

    private final AtomicLong generation = new AtomicLong(0) ;

    protected ThreadLocal<TxnState<T, C>> txnState = ThreadLocal.withInitial(() -> null);

    public TransactionalCollectionImpl(C baseCollection, Function<? super C, ? extends C> cloner) {
        super();
        this.master.set(baseCollection);
        this.cloner = cloner;
    }

    public static class TxnState<T, C extends Collection<T>> {
        public TxnState(TxnType txnType, ReadWrite txnMode, C local, long version) {
            super();
            this.txnType = txnType;
            this.txnMode = txnMode;
            this.local = local;
            this.version = version;
        }
        TxnType txnType;
        ReadWrite txnMode;
        C local;
        long version;
    }

    protected Function<? super C, ? extends C> cloner;


    @Override
    public void commit() {
        TxnState<T, C> state = txnState.get();
        master.set(state.local);
    }

    @Override
    public void abort() {
        end();
    }

    @Override
    public void end() {
        txnState.remove();
        transactionLock.leaveCriticalSection();
    }

    @Override
    public void begin(ReadWrite readWrite) {
        begin(TxnType.convert(readWrite));
    }

    @Override
    public void begin(TxnType txnType) {
        if (isInTransaction()) {
            throw new JenaTransactionException("Transactions cannot be nested!");
        }

        ReadWrite txnMode = TxnType.initial(txnType);
        _begin(txnType, txnMode);
    }

    private void _begin(TxnType txnType, ReadWrite txnMode) {
        transactionLock.enterCriticalSection(txnMode.equals(ReadWrite.READ)); // get the dataset write lock, if needed.

        C clone;
        if (txnMode.equals(ReadWrite.WRITE)) {
            clone = cloner.apply(master.get());
        } else {
            clone = master.get();
        }

        long version = generation.get();

        txnState.set(new TxnState<T, C>(txnType, txnMode, clone, version));

//        withLock(systemLock, () ->{
//            version.set(generation.get());
//        }) ;
    }

//    public static void withLock(java.util.concurrent.locks.Lock lock, Runnable action) {
//        lock.lock();
//        try { action.run(); }
//        finally {
//            lock.unlock();
//        }
//    }


    @Override
    public boolean promote(Promote promoteMode) {
        if (!isInTransaction())
            throw new JenaTransactionException("Tried to promote outside a transaction!");
        if ( transactionMode().equals(ReadWrite.WRITE) )
            return true;

        if ( transactionType() == TxnType.READ )
            return false;

        boolean readCommitted = (promoteMode == Promote.READ_COMMITTED);

        try {
            _promote(readCommitted);
            return true;
        } catch (JenaTransactionException ex) {
            return false ;
        }
    }

    private void _promote(boolean readCommited) {
        // Outside lock.
        if ( ! readCommited && txnState.get().version != generation.get() )  {
            // This tests for any commited writers since this transaction started.
            // This does not catch the case of a currently active writer
            // that has not gone to commit or abort yet.
            // The final test is after we obtain the transactionLock.
            throw new JenaTransactionException("Dataset changed - can't promote") ;
        }

        // Blocking on other writers.
        transactionLock.enterCriticalSection(Lock.WRITE);

        TxnState<T, C> local = txnState.get();
        // Check again now we are inside the lock.
        if ( ! readCommited && local.version != generation.get() )  {
                // Can't promote - release the lock.
                transactionLock.leaveCriticalSection();
                throw new JenaTransactionException("Concurrent writer changed the dataset : can't promote") ;
            }
        // We have the lock and we have promoted!
        local.txnMode = WRITE;
        _begin(transactionType(), ReadWrite.WRITE) ;
    }

    @Override
    public ReadWrite transactionMode() {
        return txnState.get().txnMode;
    }

    @Override
    public TxnType transactionType() {
        return txnState.get().txnType;
    }

    @Override
    public boolean isInTransaction() {
        return txnState.get() != null;
    }

    private <X> X access(Function<C, X> source) {
        return isInTransaction()
                ? source.apply(txnState.get().local)
                : Txn.calculateRead(this, () -> source.apply(txnState.get().local));
    }

    protected <X, R> R mutate(Function<C, R> action) {
        @SuppressWarnings("unchecked")
        R[] result = (R[])new Object[]{null};

        if (isInTransaction()) {
            if (!transactionMode().equals(WRITE)) {
                TxnType mode = transactionType();
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

            result[0] = action.apply(txnState.get().local);
        } else Txn.executeWrite(this, () -> {
//            System.out.println(version.get());
            result[0] = action.apply(txnState.get().local);
        });
        return result[0];
    }


    @Override
    public boolean add(T e) {
        return mutate(c -> c.add(e));
    }

    @Override
    public Iterator<T> iterator() {
        return access(C::iterator);
    }

    @Override
    public boolean contains(Object o) {
        return access(c -> c.contains(o));
    }

    @Override
    public int size() {
        return access(C::size);
    }
}