package org.aksw.jena_sparql_api.delay.extra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 10:35 AM
 */
public abstract class DelayerBase
    implements Delayer
{
    private static Logger logger = LoggerFactory.getLogger(DelayerBase.class);

    public abstract long getDelay();
    public abstract long getLastRequestTime();
    public abstract void setLastRequestTime(long lastRequestTime);

    @Override
    public synchronized void doDelay() {
        long now = System.currentTimeMillis();

        long remainingDelay = getDelay() - (now - getLastRequestTime());

        if (remainingDelay > 0l) {
            logger.debug("Delaying by " + remainingDelay + "ms.");
            try {
                Thread.sleep(remainingDelay);
            } catch (Exception e) {

            }
        }

        setLastRequestTime(System.currentTimeMillis());
    }

}
