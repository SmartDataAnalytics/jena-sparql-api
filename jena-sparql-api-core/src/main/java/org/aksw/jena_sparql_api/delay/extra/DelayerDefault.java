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
    private long delayInMs = 1000;
    private long lastRequestTime = 0;

    public DelayerDefault() {
    }

    public DelayerDefault(long delayInMs) {
        this.delayInMs = delayInMs;
    }

    public DelayerDefault(long delay, long lastRequestTime) {
        this.delayInMs = delay;
        this.lastRequestTime = lastRequestTime;
    }


    @Override
    public long getDelay() {
        return delayInMs;
    }

    @Override
    public long getLastRequestTime() {
        return lastRequestTime;
    }

    @Override
    public void setLastRequestTime(long lastRequestTime) {
        this.lastRequestTime = lastRequestTime;
    }
    
    public static DelayerDefault createFromNow(long delay) {
    	return new DelayerDefault(delay, System.currentTimeMillis());
    }
}
