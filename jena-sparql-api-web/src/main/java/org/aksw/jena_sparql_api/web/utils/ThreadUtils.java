package org.aksw.jena_sparql_api.web.utils;

import javax.ws.rs.container.AsyncResponse;

public class ThreadUtils {

    public static void start(final AsyncResponse asyncResponse, final Runnable runnable) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch(Exception e) {
                    asyncResponse.cancel();
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}
