package org.aksw.state_space_search.core;

import java.util.stream.Stream;

public interface State<S> {
    boolean isFinal();
    S getResult();
    Stream<Action<S>> getActions();
}
