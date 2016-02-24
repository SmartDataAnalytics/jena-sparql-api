package org.aksw.jena_sparql_api_sparql_path2;


public class FrontierItem<S, V, E> {
    /**
     * The state in the nfa
     */
    protected S state;

    /**
     * The current path
     */
    protected NestedPath<V, E> path;

}
