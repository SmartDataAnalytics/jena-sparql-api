package org.aksw.jena_sparql_api.views;

public class NestedStack<T>
    extends GenericNestedStack<T, NestedStack<T>>
{
    public NestedStack(NestedStack<T> parent, T value) {
        super(parent, value);
    }
}
