package org.aksw.jena_sparql_api.mapper;

import org.apache.jena.sparql.engine.binding.Binding;

public class AccLiteral<T>
    implements Acc<T>
{
    private T value = null;
    private long i = 0;
    private BindingMapper<T> bindingMapper;

    public AccLiteral(BindingMapper<T> bindingMapper) {
        this.bindingMapper = bindingMapper;
    }

    @Override
    public void accumulate(Binding binding) {
        // TODO Detect if we override the value and raise a warning!
        value = bindingMapper.apply(binding, i++);
    }

    @Override
    public T getValue() {
        return value;
    }

    public static <T> AccLiteral<T> create(BindingMapper<T> bindingMapper) {
        return new AccLiteral<T>(bindingMapper);
    }

}
