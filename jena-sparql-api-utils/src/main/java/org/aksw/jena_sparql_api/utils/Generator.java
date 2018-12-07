package org.aksw.jena_sparql_api.utils;

public interface Generator<T>
//extends Enumeration<Var>
{
    T next();
    T current();

    //T prefer(T item);

    /**
     * Clones should independently yield the same sequences of items as the original object
     *
     * @return
     */
    Generator<T> clone();
}
