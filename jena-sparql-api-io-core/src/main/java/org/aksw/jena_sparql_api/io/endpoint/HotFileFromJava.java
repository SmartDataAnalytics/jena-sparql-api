package org.aksw.jena_sparql_api.io.endpoint;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class HotFileFromJava
    implements HotFile
{
    protected ConcurrentFileEndpoint endpoint;
    protected AutoCloseable cancelAction;
    //protected Single<?> processSingle;

    public HotFileFromJava(ConcurrentFileEndpoint endpoint, AutoCloseable cancelAction) {
        super();
        this.endpoint = endpoint;
        this.cancelAction = cancelAction;
    }

    @Override
    public CompletableFuture<Path> future() {
        return endpoint.getIsDone();
    }

    @Override
    public void abort() throws Exception {
        cancelAction.close();
    }

    @Override
    public InputStream newInputStream() throws IOException {
        InputStream result = Channels.newInputStream(endpoint.newReadChannel());
        return result;
    }

    @Override
    public String toString() {
        return "HotFileFromJava [endpoint=" + endpoint + "]";
    }
}
