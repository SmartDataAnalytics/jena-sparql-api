package org.aksw.jena_sparql_api.mapper.proxy;

import java.lang.reflect.Method;
import java.util.Map.Entry;

public interface MethodDescriptor {

//	boolean isCollection();
//	boolean isScalar();
//	boolean isMap();
//	MethodDescriptorCollection asCollection();
//	MethodDescriptorSimple asScalar();
//	MethodDescriptorMap asMap();

    /**
     * The method described by this descriptor
     *
     * @return
     */
    Method getMethod();

    boolean isGetter();

    /**
     * If a method is not a getter, it is by default assumed to be setter
     * @return
     */
    default boolean isSetter() { return !isGetter(); }
    boolean isCollectionValued();

    /**
     * For signatures where the item type is dynamic based on a given class:
     * <T extends RDFNode> Collection<T> get(Class<T> clazz)
     * Only applies if isCollectionValued is true
     *
     * @return
     */
    boolean isDynamicCollection();

    Class<?> getType();

    Class<?> getItemType();
    Class<?> getCollectionType();


    // For map types;
    boolean isMapType();
    Class<?> getKeyType();
    Class<?> getValueType(); // Note: We could reuse getItemType();


    /**
     * Only applicable to setters - is the method's return type
     * assignable from the method's declaring class?
     *
     * @return
     */
    boolean isFluentCompatible();

    public static MethodDescriptor simpleGetter(Method method, Class<?> type) {
        return new MethodDescriptorSimple(method, true, false, type);
    }

    public static MethodDescriptor simpleSetter(Method method, boolean fluentCapable, Class<?> type) {
        return new MethodDescriptorSimple(method, false, fluentCapable, type);
    }

    public static MethodDescriptor collectionGetter(Method method, Class<?> collectionType, Class<?> itemType) {
        return new MethodDescriptorCollection(method, true, false, collectionType, itemType, false);
    }

    public static MethodDescriptor collectionSetter(Method method, boolean fluentCapable, Class<?> collectionType, Class<?> itemType) {
        return new MethodDescriptorCollection(method, false, fluentCapable, collectionType, itemType, false);
    }

    public static MethodDescriptor dynamicCollectionGetter(Method method, Class<?> collectionType, Class<?> boundedItemType) {
        return new MethodDescriptorCollection(method, true, false, collectionType, boundedItemType, true);
    }

    public static MethodDescriptor mapGetter(Method method, Entry<Class<?>, Class<?>> mapTypes) {
        Class<?> keyType = mapTypes.getKey();
        Class<?> valueType = mapTypes.getValue();
        return new MethodDescriptorMap(method, true, false, keyType, valueType);
    }

    public static MethodDescriptor mapGetter(Method method, Class<?> keyType, Class<?> valueType) {
        return new MethodDescriptorMap(method, true, false, keyType, valueType);
    }

    public static MethodDescriptor mapSetter(Method method, boolean fluentCapable, Class<?> keyType, Class<?> valueType) {
        return new MethodDescriptorMap(method, false, fluentCapable, keyType, valueType);
    }
}