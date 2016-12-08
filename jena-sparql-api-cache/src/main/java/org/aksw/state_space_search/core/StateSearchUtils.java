package org.aksw.state_space_search.core;

import java.util.Comparator;
import java.util.stream.Stream;

import com.google.common.collect.Ordering;

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
            State::getSolution,
            State::getActions,
            Action::apply,
            depth,
            maxDepth);
        return result;
    }

    public static <S> Stream<S> depthFirstSearch(State<S> state, int maxDepth) {
        Stream<S> result = depthFirstSearch(state, 0, maxDepth);
        return result;
    }

    public static <S> Stream<S> depthFirstSearch(State<S> state, int depth, int maxDepth) {

        Stream<S> result = SearchUtils.<State<S>, Action<S>, S>depthFirstSearch(
            state,
            State::isFinal,
            State::getSolution,
            State::getActions,
            //Ordering.<Comparator<Action<S>>>natural(),
            //cmp,
            (a, b) -> (int)(b.getCost() - a.getCost()),
            Action::apply,
            depth,
            maxDepth);
        return result;
    }

}
