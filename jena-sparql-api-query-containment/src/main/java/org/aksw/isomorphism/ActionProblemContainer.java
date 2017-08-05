package org.aksw.isomorphism;

import java.util.function.BinaryOperator;
import java.util.function.Predicate;

import org.aksw.combinatorics.algos.StateProblemContainer;
import org.aksw.combinatorics.solvers.collections.ProblemContainer;
import org.aksw.state_space_search.core.Action;
import org.aksw.state_space_search.core.State;

public class ActionProblemContainer<S>
    implements Action<S>
{
    protected ProblemContainer<S> problemContainer;
    protected S partialSolution;
    protected Predicate<S> isUnsolveable;
    protected BinaryOperator<S> solutionCombiner;
    protected double cost;

    public ActionProblemContainer(S partialSolution, Predicate<S> isUnsolveable, ProblemContainer<S> problemContainer, BinaryOperator<S> solutionCombiner) {
        super();
        //this.cost = pick.getPicked().getEstimatedCost();
        this.partialSolution = partialSolution;
        this.isUnsolveable = isUnsolveable;
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
        State<S> result = new StateProblemContainer<S>(partialSolution, isUnsolveable, problemContainer, solutionCombiner);
        return result;
    }

    @Override
    public String toString() {
        return "ActionProblemContainer [problemContainer=" + problemContainer
                + ", partialSolution=" + partialSolution + ", solutionCombiner="
                + solutionCombiner + ", cost=" + cost + "]";
    }


//    public static <S> ActionProblemContainer<S> create(S partialSolution, ProblemContainer<S> problemContainer) {
//        ActionProblemContainer<S> result = new ActionProblemContainer<S>(problemContainer, partialSolution, solu);
//    }


}
