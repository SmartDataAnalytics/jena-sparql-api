package org.aksw.jena_sparql_api.mapper.impl.type;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class PropertyModel
    implements PropertyOps
{
    protected String name;
    protected Function<Object, ?> getter;
    protected BiConsumer<Object, Object> setter;

    public PropertyModel() {
    }

    public PropertyModel(String name, Function<Object, ?> getter,
            BiConsumer<Object, Object> setter) {
        super();
        this.name = name;
        this.getter = getter;
        this.setter = setter;
    }
    
    @Override
    public String getName() {
        return name;
    }

    public Function<Object, ?> getGetter() {
        return getter;
    }

    public void setGetter(Function<Object, ?> getter) {
        this.getter = getter;
    }

    public BiConsumer<Object, ?> getSetter() {
        return setter;
    }

    public void setSetter(BiConsumer<Object, Object> setter) {
        this.setter = setter;
    }

    @Override
    public Object getValue(Object entity) {
        Object result = getter.apply(entity);
        return result;
    }

    @Override
    public void setValue(Object entity, Object value) {
        setter.accept(entity, value);        
    }

    @Override
    public String toString() {
        return "PropertyModel [name=" + name + ", getter=" + getter
                + ", setter=" + setter + "]";
    }
}
