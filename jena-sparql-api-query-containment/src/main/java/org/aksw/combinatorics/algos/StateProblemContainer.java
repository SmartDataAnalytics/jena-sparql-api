package org.aksw.combinatorics.algos;

import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.aksw.combinatorics.solvers.GenericProblem;
import org.aksw.combinatorics.solvers.Problem;
import org.aksw.combinatorics.solvers.collections.ProblemContainer;
import org.aksw.combinatorics.solvers.collections.ProblemContainerPick;
import org.aksw.isomorphism.ActionProblemContainer;
import org.aksw.state_space_search.core.Action;
import org.aksw.state_space_search.core.State;

/**
 * Class that wraps a {@link Problem} with the {@link State} interface.
 *
 * @author raven
 *
 * @param <S>
 */
public class StateProblemContainer<S>
    implements State<S>
{
    protected ProblemContainer<S> problemContainer;
    protected Predicate<S> isUnsolveable;

    protected S baseSolution;
    protected BinaryOperator<S> solutionCombiner;

    public StateProblemContainer(S baseSolution, Predicate<S> isUnsolveable, ProblemContainer<S> problemContainer, BinaryOperator<S> solutionCombiner) {
        super();
        this.baseSolution = baseSolution;
        this.problemContainer = problemContainer;
        this.solutionCombiner = solutionCombiner;
    }

    @Override
    public boolean isFinal() {
        boolean result = problemContainer.isEmpty() || isUnsolveable.test(baseSolution);
        return result;
    }

    @Override
    public S getSolution() {
        return baseSolution;
    }

    /**
     * Actions are created by means of first solving the cheapest open problem
     * generating an action for each obtained solution.
     * (assumes a non-final state)
     *
     *
     */
    @Override
    public Stream<Action<S>> getActions() {
        ProblemContainerPick<S> pick = problemContainer.pick();

        GenericProblem<S, ?> picked = pick.getPicked();
        ProblemContainer<S> remaining = pick.getRemaining();

        Stream<Action<S>> result = picked
            .generateSolutions()
            .map(solutionContribution -> {
                S partialSolution = solutionCombiner.apply(baseSolution, solutionContribution);

                Action<S> r;
                //Stream<Action<S>> r;
                // If the partial solution is null, then indicate the
                // absence of a solution by returning a stream that yields
                // null as a 'solution'
                if (partialSolution == null) {
                    //r = Collections.<S> singleton(null).stream();
                    r = null;
                } else {
                    // This step is optional: it refines problems
                    // based on the current partial solution
                    // Depending on your setting, this can give a
                    // performance boost or penalty
                    //ProblemContainerImpl<S> openProblems = remaining;
                    ProblemContainer<S> openProblems = remaining.refine(partialSolution);
                    r = new ActionProblemContainer<S>(partialSolution, isUnsolveable, openProblems, solutionCombiner);
//
//                    if (openProblems.isEmpty()) {
//                        r = Collections.<S> emptySet().stream();
//                    } else {
//                        r =
//                        //ProblemSolver<S> nextState = new ProblemSolver<S>(openProblems, baseSolution, solutionCombiner);
//                        //r = nextState.streamSolutions();
//                    }
                }

//                Stream<Action<S>> s = r.map(x -> ); //<S>(partialSolution, partialSolution));

                return r;
            })
            ;

        return result;
    }
}
