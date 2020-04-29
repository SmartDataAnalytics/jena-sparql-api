package org.aksw.jena_sparql_api.concurrent.util;

import java.util.function.Supplier;

public class Synchronized {
    /**
     * Helper that checks the condition before a synchronized block
     * and once it is entered.
     *
     * @param syncObj
     * @param condition
     * @param action
     * @throws Exception
     */
    public static void on(Object syncObj, Supplier<Boolean> condition, AutoCloseable action) throws Exception {
        if(condition.get()) {
            synchronized(syncObj) {
                if(condition.get()) {
                    action.close();
                }
            }
        }
    }
}
