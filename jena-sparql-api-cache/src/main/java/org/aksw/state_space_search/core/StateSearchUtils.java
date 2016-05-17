package org.aksw.state_space_search.core;

import java.util.stream.Stream;

public class StateSearchUtils
{
    public static <S> Stream<S> breadthFirstSearch(State<S> state, int maxDepth) {
        Stream<S> result = breadthFirstSearch(state, 0, maxDepth);
        return result;
    }

    public static <S> Stream<S> breadthFirstSearch(State<S> state, int depth, int maxDepth) {
        Stream<S> result = SearchUtils.<State<S>, Action<S>, S>breadthFirstSearch(
            state,
            State::isFinal,
            State::getResult,
            State::getActions,
            Action::apply,
            depth,
            maxDepth);
        return result;
    }


}
