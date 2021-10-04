package org.aksw.jena_sparql_api.mapper.util;

public interface Labeled<T> {
	T getObject();
	String getLabel();
}
