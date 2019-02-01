package org.aksw.jena_sparql_api_sparql_path2.playground;

import java.io.Serializable;
import java.util.Set;

import org.aksw.jena_sparql_api.sparql_path2.NestedPath;
import org.aksw.jena_sparql_api.utils.model.Directed;

public class FrontierData<I, S, V, E>
    implements Serializable
{
    private static final long serialVersionUID = -4626919083765538609L;

    protected I frontierId;
    protected Set<S> states;
    protected Directed<NestedPath<V, E>> pathHead;

    public FrontierData(I frontierId, Set<S> states, Directed<NestedPath<V, E>> pathHead) {
        super();
        this.frontierId = frontierId;
        this.states = states;
        this.pathHead = pathHead;
    }

    public I getFrontierId() {
        return frontierId;
    }

    public Set<S> getStates() {
        return states;
    }

    public Directed<NestedPath<V, E>> getPathHead() {
        return pathHead;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((frontierId == null) ? 0 : frontierId.hashCode());
        result = prime * result
                + ((pathHead == null) ? 0 : pathHead.hashCode());
        result = prime * result + ((states == null) ? 0 : states.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FrontierData<?, ?, ?, ?> other = (FrontierData<?, ?, ?, ?>) obj;
        if (frontierId == null) {
            if (other.frontierId != null)
                return false;
        } else if (!frontierId.equals(other.frontierId))
            return false;
        if (pathHead == null) {
            if (other.pathHead != null)
                return false;
        } else if (!pathHead.equals(other.pathHead))
            return false;
        if (states == null) {
            if (other.states != null)
                return false;
        } else if (!states.equals(other.states))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "FrontierData [frontierId=" + frontierId + ", states=" + states
                + ", pathHead=" + pathHead + "]";
    }


}