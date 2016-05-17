package org.aksw.isomorphism;

import java.util.Collections;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

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

    protected S baseSolution;
    protected BinaryOperator<S> solutionCombiner;

    protected S result;
    protected boolean isFinal;


    public StateProblemContainer(ProblemContainer<S> problemContainer, S result, boolean isFinal) {
        super();
        this.problemContainer = problemContainer;
        this.result = result;
        this.isFinal = isFinal;
    }

    @Override
    public boolean isFinal() {
        return isFinal;
    }

    @Override
    public S getResult() {
        return result;
    }

    @Override
    public Stream<Action<S>> getActions() {
        ProblemContainerPick<S> pick = problemContainer.pick();

        Problem<S> picked = pick.getPicked();
        ProblemContainer<S> remaining = pick.getRemaining();

        Stream<S> result = picked
            .generateSolutions()
            .flatMap(solutionContribution -> {
                S partialSolution = solutionCombiner.apply(baseSolution, solutionContribution);

                Stream<S> r;
                // If the partial solution is null, then indicate the
                // absence of a solution by returning a stream that yields
                // null as a 'solution'
                if (partialSolution == null) {
                    r = Collections.<S> singleton(null).stream();
                } else {
                    // This step is optional: it refines problems
                    // based on the current partial solution
                    // Depending on your setting, this can give a
                    // performance boost or penalty
                    //ProblemContainerImpl<S> openProblems = remaining;
                    ProblemContainer<S> openProblems = remaining.refine(partialSolution);

                    if (openProblems.isEmpty()) {
                        r = Collections.<S> emptySet().stream();
                    } else {
                        ProblemSolver<S> nextState = new ProblemSolver<S>(openProblems, baseSolution, solutionCombiner);
                        r = nextState.streamSolutions();
                    }
                }
                return r;
            });

        return result;
    }
}
