package org.aksw.jena_sparql_api.mapper;

import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.function.FunctionEnv;

/**
 * An accumulator similar to that of Jena, however it uses a generic for the
 * value.
 * 
 * @author raven
 * 
 * @param <T>
 */
public interface Acc<T> {
    public void accumulate(Binding binding);

    T getValue();
}
