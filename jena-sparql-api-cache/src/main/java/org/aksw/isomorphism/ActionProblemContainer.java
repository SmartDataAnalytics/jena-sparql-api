package org.aksw.isomorphism;

import java.util.function.BinaryOperator;

import org.aksw.state_space_search.core.Action;
import org.aksw.state_space_search.core.State;

public class ActionProblemContainer<S>
    implements Action<S>
{
    protected ProblemContainer<S> problemContainer;
    protected S partialSolution;
    protected BinaryOperator<S> solutionCombiner;
    protected double cost;

    public ActionProblemContainer(S partialSolution, ProblemContainer<S> problemContainer, BinaryOperator<S> solutionCombiner) {
        super();
        //this.cost = pick.getPicked().getEstimatedCost();
        this.partialSolution = partialSolution;
        this.problemContainer = problemContainer;
        this.solutionCombiner = solutionCombiner;
    }

    @Override
    public double getCost() {
        //return cost;
        return 1.0;
    }

    @Override
    public State<S> apply() {
        //ProblemContainerImpl<S> remaining = pick.getRemaining();
        State<S> result = new StateProblemContainer<S>(partialSolution, problemContainer, solutionCombiner);
        return result;
    }


//    public static <S> ActionProblemContainer<S> create(S partialSolution, ProblemContainer<S> problemContainer) {
//        ActionProblemContainer<S> result = new ActionProblemContainer<S>(problemContainer, partialSolution, solu);
//    }
}
