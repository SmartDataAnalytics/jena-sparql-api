package org.aksw.state_space_search.core;

public interface Action<S> {
    public double getCost();
    State<S> apply();
}
