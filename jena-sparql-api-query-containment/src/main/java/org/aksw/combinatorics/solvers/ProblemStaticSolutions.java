package org.aksw.combinatorics.solvers;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

public class ProblemStaticSolutions<S, T>
    implements ProblemNeighborhoodAware<S, T>
{
    protected Collection<S> solutions;
    protected Collection<T> neighborhood;

    public ProblemStaticSolutions(Collection<S> solutions) {
        this(solutions, null);
    }

    public ProblemStaticSolutions(Collection<S> solutions, Collection<T> neighborhood) {
        super();
        this.solutions = solutions;
    }

//    @Override
//    public int compareTo(Problem<S> o) {
//        return
//    }

    public boolean isEmpty() {
        return solutions.isEmpty();
    }

    @Override
    public long getEstimatedCost()
    {
        return solutions.size();
    }

    @Override
    public Stream<S> generateSolutions() {
        return solutions.stream();
    }

    @Override
    public Collection<ProblemNeighborhoodAware<S, T>> refine(S partialSolution) {
        return Collections.singleton(this);
    }

    @Override
    public Collection<T> getSourceNeighbourhood() {
        return neighborhood;
    }

	@Override
	public String toString() {
		return "ProblemStaticSolutions [solutions=" + solutions + ", neighborhood=" + neighborhood + "]";
	}
}
