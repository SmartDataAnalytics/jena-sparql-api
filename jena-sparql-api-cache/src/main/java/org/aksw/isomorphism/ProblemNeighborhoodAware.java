package org.aksw.isomorphism;

import java.util.Collection;

/**
 * Problem capable of exposing its neighborhood.
 * Problem instances that share the same neighborhood are considered related.
 *
 * @author raven
 *
 */
public interface ProblemNeighborhoodAware<S, T>
    extends GenericProblem<S, ProblemNeighborhoodAware<S, T>>
{
    Collection<T> getSourceNeighbourhood();
    //Collection<T> exposeTargetNeighbourhood();
}
