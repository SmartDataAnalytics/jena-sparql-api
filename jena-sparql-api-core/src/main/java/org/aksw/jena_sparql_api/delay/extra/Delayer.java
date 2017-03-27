package org.aksw.jena_sparql_api.delay.extra;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 10:57 AM
 */
public interface Delayer {
    void doDelay() throws InterruptedException;
}
