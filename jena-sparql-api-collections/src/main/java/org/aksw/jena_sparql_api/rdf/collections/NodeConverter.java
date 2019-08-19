package org.aksw.jena_sparql_api.rdf.collections;

import java.util.Objects;

public interface NodeConverter<N, T>
{
	Class<?> getJavaClass();
	public boolean canMap(N node);
	
	N toNode(T obj);
	T toJava(N node);

	default N toNodeFromObject(Object obj) {
		Objects.requireNonNull(obj);
		Class<?> clazz = getJavaClass();
		@SuppressWarnings("unchecked")
		N result = clazz.isAssignableFrom(obj.getClass())
				? toNode((T)obj)
				: null;

		return result;
	}
}
