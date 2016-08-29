package org.aksw.jena_sparql_api.beans.model;

import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;

/**
 * PropertyOps implementation that delegates most calls 
 * 
 * @author raven
 *
 */
public class PropertyModel
    implements PropertyOps
{
	private static final Logger logger = LoggerFactory.getLogger(PropertyModel.class);
	
    protected String name;
    protected Class<?> type;
    protected Function<Object, ?> getter;
    protected BiConsumer<Object, Object> setter;
    protected Function<Class<?>, Object> annotationFinder;
    protected Method readMethod;
    protected Method writeMethod;
    protected ConversionService conversionService;
    
    public PropertyModel() {
    }

    public PropertyModel(String name, Class<?> clazz, Function<Object, ?> getter,
            BiConsumer<Object, Object> setter,
            ConversionService conversionService,
            Function<Class<?>, Object> annotationFinder) {
        super();
        this.name = name;
        this.type = clazz;
        this.getter = getter;
        this.setter = setter;
        this.conversionService = conversionService;
        this.annotationFinder = annotationFinder;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?> getType() {
        return type;
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
    	if(value != null) {
    		Class<?> valueClass = value.getClass();
    		if(!type.equals(valueClass) && conversionService != null) {
    			boolean canConvert = conversionService.canConvert(valueClass, type);
    			if(canConvert) {
    				value = conversionService.convert(value, type);
    			}
    		}
    	}

    	setter.accept(entity, value);
    }

    @Override
    public String toString() {
        return "PropertyModel [name=" + name + ", getter=" + getter
                + ", setter=" + setter + "]";
    }

    @Override
    public boolean isWritable() {
        boolean result = setter != null;;
        return result;
    }

    @Override
    public boolean isReadable() {
        boolean result = getter != null;;
        return result;
    }    
    
    public Method getReadMethod() {
        return readMethod;
    }

    public void setReadMethod(Method readMethod) {
        this.readMethod = readMethod;
    }

    public Method getWriteMethod() {
        return writeMethod;
    }

    public void setWriteMethod(Method writeMethod) {
        this.writeMethod = writeMethod;
    }

    @Override
    public <A> A findAnnotation(Class<A> annotationClass) {
        Object tmp = annotationFinder.apply(annotationClass);
        @SuppressWarnings("unchecked")
        A result = (A)tmp;
        return result;
    }

    public Function<Class<?>, Object> getAnnotationFinder() {
        return annotationFinder;
    }

    public void setAnnotationFinder(Function<Class<?>, Object> annotationFinder) {
        this.annotationFinder = annotationFinder;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }
    
    
}
