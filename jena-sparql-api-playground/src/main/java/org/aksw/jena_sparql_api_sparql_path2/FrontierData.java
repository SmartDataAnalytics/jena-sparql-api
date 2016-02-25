package org.aksw.jena_sparql_api_sparql_path2;

import java.util.Set;

public class FrontierData<S, V, E> {
    //protected int frontierId;
    protected Set<S> states;
    protected DirectedProperty<NestedPath<V, E>> pathHead;

    public FrontierData(int frontierId, DirectedProperty<NestedPath<V, E>> pathHead) {
        super();
        this.pathHead = pathHead;
    }

//    public int getFrontierId() {
//        return frontierId;
//    }

    public DirectedProperty<NestedPath<V, E>> getPathHead() {
        return pathHead;
    }
}