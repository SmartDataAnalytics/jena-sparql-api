package org.aksw.jena_sparql_api.dataset.file;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.util.Objects;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.concurrent.util.Synchronized;
import org.aksw.jena_sparql_api.io.common.Reference;
import org.aksw.jena_sparql_api.io.common.ReferenceImpl;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.shared.Lock;
import org.apache.jena.shared.LockMRPlusSW;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.Transactional;

/**
 * Base class for file-backed transactions:
 * The basic process is as follows:
 * Beginning a transaction attempts to first acquire a process and a thread lock on the file.
 * Then the content is read and cached:
 *
 * Upon completion of the transaction the content is written back and the process and thread lock are released.
 *
 * @author raven
 *
 */
public abstract class FileSyncBase
    implements Transactional, AutoCloseable
{
    public static class State {
        Reference<FileChannel> channelRef;
        // FileLock lock;
        Lock lock;
        ReadWrite transactionMode;
        Object generation;
    }

    protected Path path;

    protected FileTime cacheFileTime = null;

//    protected OpenOption[] openOptions;
    protected Supplier<Reference<FileChannel>> rootFileChannelSupp;


    protected Reference<FileChannel> openActual() {
        try {
            Path parentPath = path.getParent();
            if (parentPath != null) {
                Files.createDirectories(parentPath);
            }

            Reference<FileChannel> r = FileLockUtils.open(path, false, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
            return r;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    protected Reference<FileChannel> rootFileChannelRef = null;
    protected ThreadLocal<State> localState = new ThreadLocal<>();

    protected Lock transactionLock = new LockMRPlusSW();


    protected LockPolicy lockPolicy;

//    public FileSyncBase(Path path) {
//        this(path, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
//    }

//    public FileSyncBase(Path path, OpenOption ... openOptions) {
//        super();
//        this.path = path;
//        this.openOptions = openOptions;
//    }
    public FileSyncBase(
            LockPolicy lockPolicy,
            Path path
            ) throws Exception { //Supplier<Reference<FileChannel>> rootFileChannelSupp) throws Exception {
        super();
        this.path = path;
//        this.openOptions = openOptions;
        this.lockPolicy = lockPolicy;
        this.rootFileChannelSupp = () -> openActual(); //rootFileChannelSupp;

        if(LockPolicy.LIFETIME.equals(lockPolicy)) {
            // Initialize the root lock immediately
            // and close the local lock the method returns
            acquireLocalFileChannelRef().close();
        }
    }

    /**s
     * Override this method to read the content of the channel
     * into some object
     *
     * @param fc
     */
    protected abstract void deserializeFrom(FileChannel fc);

    /**
     * Override this method to write some object into the file channel
     *
     * @param fc
     */
    protected abstract void serializeTo(FileChannel fc);

    /**
     * Indicate whether the state/object loaded via loadFrom was modified.
     * If there was no modification than storeTo will not be called when committing a transaction
     *
     * @return
     */
    protected abstract Object getLoadedObjectVersion();

    @Override
    public void begin(TxnType type) {
        ReadWrite readWrite = TxnType.convert(type);
        begin(readWrite);
    }

    @Override
    public void begin(ReadWrite readWrite) {
        try {
            prepareBegin(readWrite);
            boolean readLockRequested = readWrite.equals(ReadWrite.READ);

//            System.out.println(Thread.currentThread() + " wants to enter");
            transactionLock.enterCriticalSection(readLockRequested);
//            System.out.println(Thread.currentThread() + " entered");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected Reference<FileChannel> acquireLocalFileChannelRef() throws Exception {

        // If the file has just been opened, we need to load the data
        // once we obtained the file lock
        boolean needsDataLoading = false;

        Reference<FileChannel> localFileChannelRef;
        synchronized (this) {
            if (rootFileChannelRef == null) {
                // if .isAlive returns false, it means that the close action
                // is running
                //Reference<FileChannel> ref = FileLockUtils.open(path, readLockRequested, openOptions);
                Reference<FileChannel> ref = rootFileChannelSupp.get();

                FileChannel fileChannel = ref.get();
                rootFileChannelRef = ReferenceImpl.create(fileChannel,
                        () -> {
                            ref.close();
                            rootFileChannelRef = null;
                        }, null);

                // After closing the rootFileChannelRef the open-state of the file channel
                // depends on the local reference
                localFileChannelRef = rootFileChannelRef.acquire(null);

                if (LockPolicy.TRANSACTION.equals(lockPolicy)) {
                    rootFileChannelRef.close();
                }
                needsDataLoading = true;


                // If the file has not changed since the last loading
                // we do not have to read it again
                // We still need to acquire a file lock though

            } else {
                // Acquire may wait for close to finish
                localFileChannelRef = rootFileChannelRef.acquire(null);
            }
        }

        if(needsDataLoading) {
            FileTime fileTime = Files.getLastModifiedTime(path);

            boolean unchanged = fileTime.equals(cacheFileTime);

            if (!unchanged) {
                // The input stream is intentionally not closed;
                // as it would close the file cannel.
                // The locks depend on the file channel, so the channel
                // needs to remain open for the time of transaction
                // Lang lang = Lang.TRIG;
                // InputStream in = Channels.newInputStream(localFc);
                // RDFDataMgr.read(getW(), in, lang);
                localFileChannelRef.get().position(0);
                deserializeFrom(localFileChannelRef.get());

                cacheFileTime = Files.getLastModifiedTime(path);
            }
        }

        return localFileChannelRef;
    }

    protected void prepareBegin(ReadWrite readWrite) throws Exception {
//        System.out.println("begin " + Thread.currentThread());
//        boolean readLockRequested = readWrite.equals(ReadWrite.READ);


        // Check that the thread is not already in a transaction
        State state = localState.get();
        if(state != null) {
            throw new RuntimeException("Thread is already in a transaction");
        }


        // Open the file if it has not been opened by another transaction before
        Reference<FileChannel> localFcRef = acquireLocalFileChannelRef();


        state = new State();
        state.channelRef = localFcRef;
        state.transactionMode = readWrite;
        state.generation = getLoadedObjectVersion();


        localState.set(state);
    }

    protected State local() {
        State result = localState.get();
        Objects.requireNonNull(result);
        return result;
    }

    @Override
    public boolean promote(Promote mode) {
        return false;
    }

    @Override
    public void commit() {
        if(!isInTransaction()) {
            throw new JenaTransactionException("commit called outside of transaction");
        }
//        System.out.println("commit " + Thread.currentThread());

        State state = local();
        Object version = getLoadedObjectVersion();

        boolean isDirty = !Objects.equals(state.generation, version);

        if (isDirty) {
            FileChannel fc = state.channelRef.get();
            try {
                fc.position(0);
                fc.truncate(0);
                serializeTo(fc);
                fc.force(true);

                cacheFileTime = Files.getLastModifiedTime(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void deleteFile() throws IOException {
        Files.delete(path);
    }

    @Override
    public void abort() {
//        System.out.println("abort " + Thread.currentThread());

        end();
    }

    @Override
    public ReadWrite transactionMode() {
        ReadWrite result = local().transactionMode;
        return result;
    }

    @Override
    public TxnType transactionType() {
        return null;
    }

    @Override
    public boolean isInTransaction() {
        boolean result = localState.get() != null;
        return result;
    }

    @Override
    public void end() {
//        System.out.println("end " + Thread.currentThread());

        if(isInTransaction()) {
            try {
                State state = local();
                synchronized(this) {
                    state.channelRef.close();
                }
                localState.remove();
                transactionLock.leaveCriticalSection();
    //            System.out.println(Thread.currentThread() + " left");

            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
//        System.out.println("done end " + Thread.currentThread());
    }

    @Override
    public void close() throws Exception {
        Synchronized.on(this, () -> rootFileChannelRef != null, rootFileChannelRef::close);
    }
}