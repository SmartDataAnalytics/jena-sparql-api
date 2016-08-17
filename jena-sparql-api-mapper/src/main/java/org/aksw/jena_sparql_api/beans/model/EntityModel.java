package org.aksw.jena_sparql_api.beans.model;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.mapper.impl.type.RdfTypeFactoryImpl;
import org.springframework.core.annotation.AnnotationUtils;

public class EntityModel
    implements EntityOps
{
    protected Class<?> associatedClass;
    
    protected Supplier<?> newInstance;
    protected Map<String, PropertyModel> propertyOps;
    
    protected Function<Class<?>, Object> annotationFinder;
    
    public EntityModel() {
        this(null, null, null);
    }

    public EntityModel(Class<?> associatedClass, Supplier<?> newInstance,
            Map<String, PropertyModel> propertyOps) {
        super();
        this.associatedClass = associatedClass;
        this.newInstance = newInstance;
        this.propertyOps = propertyOps;
        
//        @SuppressWarnings("unchecked")
        this.annotationFinder = (annotationClass) -> AnnotationUtils.findAnnotation(this.associatedClass, (Class)annotationClass);
    }

    public Function<Class<?>, Object> getAnnotationFinder() {
        return annotationFinder;
    }

    public void setAnnotationFinder(Function<Class<?>, Object> annotationFinder) {
        this.annotationFinder = annotationFinder;
    }

    public Object newInstance() {
        Object result = newInstance.get();
        return result;
    }

    public Map<String, PropertyModel> getPropertyOps() {
        return propertyOps;
    }
    
    public Supplier<?> getNewInstance() {
        return newInstance;
    }

    public void setNewInstance(Supplier<?> newInstance) {
        this.newInstance = newInstance;
    }

    public void setPropertyOps(Map<String, PropertyModel> propertyOps) {
        this.propertyOps = propertyOps;
    }

    
    public static EntityModel createDefaultModel(Class<?> clazz) {
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(clazz);
        } catch (IntrospectionException e1) {
            throw new RuntimeException(e1);
        }
        
        Map<String, PropertyModel> propertyOps = new HashMap<String, PropertyModel>();
        for(PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            String propertyName = pd.getName();

            Function<Object, Object> getter = null;
            Method readMethod = pd.getReadMethod();
            if(readMethod != null) {
                getter = (entity) -> {
                    try {
                        Object r = readMethod.invoke(entity);
                        return r;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                };
            }
                        
            BiConsumer<Object, Object> setter = null;            
            Method writeMethod = pd.getWriteMethod();
            if(writeMethod != null) {
                setter = (entity, value) -> {
                    try {
                        writeMethod.invoke(entity, value);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                };
            }

            Function<Class<?>, Object> annotationFinder = (annotationClass) -> RdfTypeFactoryImpl.findPropertyAnnotation(clazz, pd, (Class)annotationClass);
            
            PropertyModel p = new PropertyModel(propertyName, clazz, getter, setter, annotationFinder);
            p.setReadMethod(readMethod);
            p.setWriteMethod(writeMethod);

            propertyOps.put(propertyName, p);     
        }
     
        EntityModel result = new EntityModel();
        result.setAssociatedClass(clazz);
        result.setNewInstance(() -> {
            try {
                return clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        result.setPropertyOps(propertyOps);
        
        return result;
    }

    @Override
    public String toString() {
        return "EntityOps [newInstance=" + newInstance + ", propertyOps="
                + propertyOps + "]";
    }

    @Override
    public Collection<? extends PropertyModel> getProperties() {
        Collection<? extends PropertyModel> result = propertyOps.values();
        return result;
    }

    @Override
    public PropertyModel getProperty(String name) {
        PropertyModel result = propertyOps.get(name);
        return result;
    }
    
    public void setAssociatedClass(Class<?> associatedClass) {
        this.associatedClass = associatedClass;
    }

    @Override
    public Class<?> getAssociatedClass() {
        return associatedClass;
    }

    @Override
    public <A> A findAnnotation(Class<A> annotationClass) {
        Object o = annotationFinder.apply(annotationClass);
        @SuppressWarnings("unchecked")
        A result = (A)o;
        return result;
    }
    
}
