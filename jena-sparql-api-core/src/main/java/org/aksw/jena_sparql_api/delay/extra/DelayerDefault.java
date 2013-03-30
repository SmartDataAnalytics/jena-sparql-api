package org.aksw.jena_sparql_api.delay.extra;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 10:45 AM
 */
public class DelayerDefault
    extends DelayerBase
{
    private long delay = 1000;
    private long lastRequestTime = 0;

    public DelayerDefault() {
    }

    public DelayerDefault(long delay) {
        this.delay = delay;
    }


    @Override
    public long getDelay() {
        return delay;
    }

    @Override
    public long getLastRequestTime() {
        return lastRequestTime;
    }

    @Override
    public void setLastRequestTime(long lastRequestTime) {
        this.lastRequestTime = lastRequestTime;
    }
}
