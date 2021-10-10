package org.aksw.jena_sparql_api.rx.io.resultset;

import org.aksw.commons.util.lifecycle.LifeCycle;
import org.apache.jena.atlas.lib.Sink;

/**
 * A {@link Sink} extended with the start() and finish() life cycle methods.
 * Mainly intended for sinks that perform serialization:
 * <ul>
 * <li>start() may write a header</li>
 * <li>send() may write an item</li>
 * <li>finish() may write a footer</li>
 * <li>flush() can be used any time to flush <i>to the</i> and/or <i>the</i> undelying stream</li>
 * <li>close() frees any associated resources</li>
 * </ul>
 *
 *
 *
 * @author raven
 *
 * @param <T>
 */
public interface SinkStreaming<T>
    extends Sink<T>, LifeCycle, AutoCloseable
{
    void start();
    void finish();    
}