package org.aksw.isomorphism;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

public class ProblemUnsolvable<S>
    implements Problem<S>
{
//    @Override
//    public int compareTo(Problem<S> o) {
//        return
//    }

    @Override
    public long getEstimatedCost() {
        return 0;
    }

    @Override
    public Stream<S> generateSolutions() {
        return Stream.of(null);
    }

    @Override
    public Collection<Problem<S>> refine(S partialSolution) {
        return Collections.singleton(this);
    }
}
