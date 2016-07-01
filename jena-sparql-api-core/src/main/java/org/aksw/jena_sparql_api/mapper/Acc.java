package org.aksw.jena_sparql_api.mapper;

import org.apache.jena.sparql.engine.binding.Binding;

/**
 * An accumulator similar to that of Jena, however it uses a generic for the
 * value.
 *
 * @author raven
 *
 * @param <T>
 */
public interface Acc<T>
    extends Accumulator<Binding, T>
{
//    public void accumulate(Binding binding);
//
//    T getValue();
}
