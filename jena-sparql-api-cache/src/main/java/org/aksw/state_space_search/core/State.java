package org.aksw.state_space_search.core;

import java.util.stream.Stream;

public interface State<S> {
    boolean isFinal();
    S getSolution();
    Stream<Action<S>> getActions();
}
