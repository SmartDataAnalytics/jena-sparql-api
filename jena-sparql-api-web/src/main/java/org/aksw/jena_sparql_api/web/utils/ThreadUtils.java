package org.aksw.jena_sparql_api.web.utils;

import javax.ws.rs.container.AsyncResponse;

public class ThreadUtils {

    public static void start(AsyncResponse asyncResponse, Runnable runnable) {
        Runnable safeRunnable = new RunnableAsyncResponseSafe(asyncResponse, runnable);

        new Thread(safeRunnable).start();
    }
}
