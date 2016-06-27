package org.aksw.isomorphism;

import java.util.Collection;

/**
 * Problem capable of exposing its neighborhood.
 * Problem instances that share the same neighborhood are considered related.
 *
 * @author raven
 *
 */
public interface ProblemWithNeighbourhood<S, T>
    extends Problem<S>
{
    Collection<T> exposeSourceNeighbourhood();
    Collection<T> exposeTargetNeighbourhood();
}
