package org.aksw.jena_sparql_api.mapper.descriptor.impl;

public interface SimpleTypes {
    public static TypeScalar scalar(Class<?> clazz) {
        return new TypeScalarImpl(clazz);
    }

    public static TypeCollection collection(Class<?> collectionClass, SimpleType itemType) {
        return new TypeCollectionImpl(collectionClass, itemType);
    }

    public static TypeCollection collection(Class<?> collectionClass, Class<?> itemClass) {
        return collection(collectionClass, scalar(itemClass));
    }
}
