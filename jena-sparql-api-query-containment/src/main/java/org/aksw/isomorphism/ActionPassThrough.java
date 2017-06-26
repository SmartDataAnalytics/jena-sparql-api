package org.aksw.isomorphism;

import org.aksw.state_space_search.core.Action;
import org.aksw.state_space_search.core.State;

/**
 * Action that has a pre-set state.
 *
 * @author raven
 *
 * @param <S>
 */
public class ActionPassThrough<S>
    implements Action<S>
{
    protected State<S> state;
    protected double cost;

    public ActionPassThrough(State<S> state, double cost) {
        super();
        this.state = state;
        this.cost = cost;
    }

    @Override
    public double getCost() {
        return cost;
    }

    @Override
    public State<S> apply() {
        return state;
    }

}
