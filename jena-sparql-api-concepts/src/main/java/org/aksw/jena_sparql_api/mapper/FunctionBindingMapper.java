package org.aksw.jena_sparql_api.mapper;

import java.util.function.Function;

import org.apache.jena.sparql.engine.binding.Binding;

/**
 * Guava Function wrapper for BindingMapper objects
 *
 * @author raven
 *
 */
public class FunctionBindingMapper<T>
    implements Function<Binding, T>
{
    private BindingMapper<T> bindingMapper;
    private long offset;

    public FunctionBindingMapper(BindingMapper<T> bindingMapper) {
        this(bindingMapper, 0);
    }

    public FunctionBindingMapper(BindingMapper<T> bindingMapper, long offset) {
        this.bindingMapper = bindingMapper;
        this.offset = offset;
    }

    @Override
    public T apply(Binding binding) {
        T result = bindingMapper.apply(binding, offset);
        return result;
    }

    public static <T> FunctionBindingMapper<T> create(BindingMapper<T> bindingMapper) {
        FunctionBindingMapper<T> result = new FunctionBindingMapper<T>(bindingMapper);
        return result;
    }
}
