package org.aksw.jena_sparql_api.http.repository.api;

import java.util.concurrent.TimeUnit;

public interface WriteProgress {
    /** Abort the process; no effect if already completed */
    void abort();

    boolean isAborted();
    boolean isFinished();

    /** Block until the process is complete */
    void awaitFinished(Long amount, TimeUnit timeUnit);

    /** Obtain the number of bytes written */
    long getBytesWritten();
}
