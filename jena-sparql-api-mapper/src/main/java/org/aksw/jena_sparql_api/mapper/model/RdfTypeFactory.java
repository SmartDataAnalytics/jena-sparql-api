package org.aksw.jena_sparql_api.mapper.model;

public interface RdfTypeFactory {
    RdfType forJavaType(Class<?> clazz);
}
