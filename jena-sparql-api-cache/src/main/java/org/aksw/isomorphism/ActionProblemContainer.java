package org.aksw.isomorphism;

import org.aksw.state_space_search.core.Action;
import org.aksw.state_space_search.core.State;

public class ActionProblemContainer<S>
    implements Action<S>
{
    protected Problem<S> problem;
    protected S partialSolution;

    public ActionProblemContainer(Problem<S> problem, S partialSolution) {
        super();
        this.problem = problem;
        this.partialSolution = partialSolution;
    }

    @Override
    public double getCost() {
        double result = problem.getEstimatedCost();
        return result;
    }

    @Override
    public State<S> apply() {

    }

}
