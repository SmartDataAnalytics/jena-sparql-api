package org.aksw.isomorphism;

import java.util.Collection;

/**
 * Problem capable of exposing its neighborhood.
 * Problem instances that share the same neighborhood are considered related.
 *
 * @author raven
 *
 */
public interface ProblemNeighbourhoodAware<S, T>
    extends Problem<S>
{
    Collection<T> getSourceNeighbourhood();
    //Collection<T> exposeTargetNeighbourhood();
}
