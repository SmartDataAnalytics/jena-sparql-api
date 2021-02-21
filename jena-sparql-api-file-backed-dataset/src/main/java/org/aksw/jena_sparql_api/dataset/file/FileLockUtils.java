package org.aksw.jena_sparql_api.dataset.file;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.aksw.jena_sparql_api.io.common.Reference;
import org.aksw.jena_sparql_api.io.common.ReferenceImpl;
import org.apache.jena.dboe.base.file.ProcessFileLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Similar to {@link ProcessFileLock} */
public class FileLockUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileLockUtils.class);

    public static class State {
        public FileChannel fileChannel;
        public FileLock processLock;
        //public java.util.concurrent.locks.Lock threadLock;
        public Semaphore threadLock;
    }

    private static final Map<Path, State> pathToState = new HashMap<>();

    /**
     * Request an exclusive file channel. The reference to the channel can be shared
     * among several threads, but the channel itself exists only once.
     *
     * Do not directly close the FileChannel!
     * Always close the reference as this also releases locks.
     *
     * @param path
     * @param readLockRequested
     * @param openOptions
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static Reference<FileChannel> open(
            Path path,
            boolean readLockRequested,
            OpenOption... openOptions) throws IOException, InterruptedException {
        Path norm = path.normalize();

//        System.out.println("pathToState " + Thread.currentThread() + " " + pathToState);

        State state;
        synchronized(pathToState) {
            state = pathToState.get(norm);
            if(state == null) {
                logger.info("Requesting process lock for: " + path);
                state = new State();
                state.fileChannel = FileChannel.open(path, openOptions);
                state.processLock =  state.fileChannel.lock(0, Long.MAX_VALUE, readLockRequested);
                //state.threadLock = new ReentrantLock(true);
                logger.info("Acquired process lock for: " + path);
                state.threadLock = new Semaphore(1);
                pathToState.put(norm, state);
            } else {
                logger.warn("There is already a process lock on: " + path);
            }
        }


        // If we (process-level) locked a file channel, it seems we cannot have multiple channels on
        // the file within the same JVM(?)

        // We use a semaphore here because we can hand out the file channel only once
        // and another thread my trigger the closing
        // If we used a lock, then the implicit restriction is,
        // that the same thread that acquired it would also have to do the release,
        // otherwise it would result in an
        // IllegalMonitorStateException
        // state.threadLock.lock();
        logger.info("Requesting thread lock for: " + path);
        state.threadLock.acquire();
        logger.info("Acquired thread lock for: " + path);

        State s = state;
        Reference<FileChannel> result = ReferenceImpl.create(
                s.fileChannel, () -> {
                    synchronized(pathToState) {
                        logger.info("Released locks for: " + path);
                        s.processLock.close();
                        s.fileChannel.close();
                        pathToState.remove(norm);
                        s.threadLock.release();
                    }
                }, null);


        return result;
    }
}