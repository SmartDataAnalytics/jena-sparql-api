package org.aksw.jena_sparql_api.core;

import java.util.concurrent.TimeUnit;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 11/19/11
 *         Time: 11:22 PM
 */
public class Time
{
    private long time;
    private TimeUnit timeUnit;

    public Time(long time, TimeUnit timeUnit) {
        this.time = time;
        this.timeUnit = timeUnit;
    }

    public long getTime() {
        return time;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
}
