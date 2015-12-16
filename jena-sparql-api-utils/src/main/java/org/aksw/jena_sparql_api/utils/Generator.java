package org.aksw.jena_sparql_api.utils;

public interface Generator<T>
//extends Enumeration<Var>
{
    T next();
    T current();

    /**
     * Clones should idependently yield the same sequences of items as the original object
     *
     * @return
     */
    Generator<T> clone();
}
