package org.aksw.jena_sparql_api.beans.model;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.ConversionService;

public class EntityModel
    implements EntityOps
{
	// Simple implies that entities can only be initialized using clone
	// TODO Use a better naming
	protected boolean isPrimitive;

//	protected boolean isCollection;
//	protected Function<Object, Iterator<?>> getItemsFn;
//	protected BiConsumer<Object, Iterator<?>> setItemsFn;

	protected CollectionOps collectionOps = null;
	
    protected Class<?> associatedClass;
    
    protected Supplier<?> newInstance;
    protected Function<Object, ?> clone;
    protected Map<String, PropertyModel> propertyOps;
    
    protected Function<Class<?>, Object> annotationFinder;
    //protected Set<Class<?>> annotationOverrides;
    
    protected Map<Class<?>, Object> classToInstance;
    
    protected ConversionService conversionService;
    //protected ClassToInstanceMap<Objcet
    
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
    
    public ConversionService getConversionService() {
		return conversionService;
	}

	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	public Function<Class<?>, Object> getAnnotationFinder() {
        return annotationFinder;
    }

    public void setAnnotationFinder(Function<Class<?>, Object> annotationFinder) {
        this.annotationFinder = annotationFinder;
    }

    @Override
    public boolean isInstantiable() {
        boolean result = newInstance != null;
        return result;
    }
    
    @Override
    public Object newInstance() {
        Object result = newInstance == null ? null : newInstance.get();
        return result;
    }

    @Override
    public boolean isClonable() {
    	boolean result = clone != null;
    	return result;
    }
    
    public Object clone(Object o) {
    	Object result = clone == null ? null : clone.apply(o);
    	return result;
    }
    
    
    public Function<Object, ?> getClone() {
		return clone;
	}

	public void setClone(Function<Object, ?> clone) {
		this.clone = clone;
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

    
    
    
    public static Constructor<?> tryGetCtor(Class<?> clazz, Class<?> ... args) {
		Constructor<?> result;
		try {
			result = clazz.getConstructor(args);
		} catch (NoSuchMethodException | SecurityException e) {
			result = null;
		}
		return result;    	
    }
    
    public static EntityModel createDefaultModel(Class<?> clazz, ConversionService conversionService) {
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(clazz);
        } catch (IntrospectionException e1) {
            throw new RuntimeException(e1);
        }
        
        
        
        // Check if the entity can act as a collection (TODO: Delegate this check to a separate module)
        CollectionOps collectionOps = null;
        if(Map.class.isAssignableFrom(clazz)) {
        	collectionOps = new CollectionOpsMap();
        } else if(Collection.class.isAssignableFrom(clazz)) {
        	collectionOps = new CollectionOpsCollection();
        }        
        

        boolean isSimple = clazz.isPrimitive();
        Function<Object, ?> copyCtorFn = null;
		Constructor<?> tmpCopyCtor = tryGetCtor(clazz);
		if(tmpCopyCtor == null) {
			Class<?> primitiveClass;
			try {
				primitiveClass = (Class<?>)clazz.getField("TYPE").get(null);
				isSimple = true;
			} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
				primitiveClass = null;
			}

			if(primitiveClass != null) {
				tmpCopyCtor = tryGetCtor(clazz, primitiveClass);
			}				
		}
		
		Constructor<?> copyCtor = tmpCopyCtor;
		if(copyCtor != null) {
			copyCtorFn = (x) -> {
				try {
					Object result = copyCtor.newInstance(x);
					return result;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			};
		}
        
        
        //long.class.
        
        
        //clazz.getConstructor(//parameterTypes)
        
        
        Map<String, PropertyModel> propertyOps = new HashMap<String, PropertyModel>();
        for(PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            Class<?> propertyType = pd.getPropertyType();
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
                        throw new RuntimeException("Failed to invoke " + writeMethod + " with " + (value == null ? null : value.getClass()) + " (" + value + ")", e);
                    }
                };
            }


            Function<Class<?>, Object> annotationFinder = (annotationClass) -> MyAnnotationUtils.findPropertyAnnotation(clazz, pd, (Class)annotationClass);
            
            PropertyModel p = new PropertyModel(propertyName, propertyType, getter, setter, conversionService, annotationFinder);
            p.setReadMethod(readMethod);
            p.setWriteMethod(writeMethod);

            propertyOps.put(propertyName, p);     
        }
     
        EntityModel result = new EntityModel();
        result.setAssociatedClass(clazz);
        result.setClone(copyCtorFn);
        
        try {
            // Check if there is a defaultCtor
            Constructor<?> defaultCtor = clazz.getConstructor();

            result.setNewInstance(() -> {
                try {
                    return clazz.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

        } catch (NoSuchMethodException e) {
            // Ignore
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        }
        
        result.setPropertyOps(propertyOps);
        result.setPrimitive(isSimple);
        
        result.setCollectionOps(collectionOps);
        
        
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

    @Override
    public <T> T getOps(Class<T> opsClass) {
        Object tmp = classToInstance.get(opsClass);
        
        T result = tmp == null ? null : (T)tmp;
 
        return result;
    }

	@Override
	public boolean isPrimitive() {
		return isPrimitive;
	}

	public void setPrimitive(boolean isSimple) {
		this.isPrimitive = isSimple;
	}

	@Override
	public boolean isCollection() {
		boolean result = collectionOps != null;
		return result;
	}

	public void setCollectionOps(CollectionOps collectionOps) {
		this.collectionOps = collectionOps;
	}

	public CollectionOps getCollectionOps() {
		return collectionOps;
	}

	@Override
	public Iterator<?> getItems(Object entity) {
		Iterator<?> result = collectionOps.getItems(entity);
		return result;
	}

	@Override
	public void setItems(Object entity, Iterator<?> items) {
		collectionOps.setItems(entity, items);
	}
	

    
}
