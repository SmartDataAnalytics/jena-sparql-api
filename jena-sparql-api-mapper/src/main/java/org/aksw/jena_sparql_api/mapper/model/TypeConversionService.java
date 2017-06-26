package org.aksw.jena_sparql_api.mapper.model;

public interface TypeConversionService
{
    TypeConverter getConverter(String datatypeIri, Class<?> clazz);
}