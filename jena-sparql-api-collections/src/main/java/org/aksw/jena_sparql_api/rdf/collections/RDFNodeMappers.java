package org.aksw.jena_sparql_api.rdf.collections;

import org.aksw.jena_sparql_api.mapper.proxy.TypeDecider;
import org.aksw.jena_sparql_api.mapper.proxy.TypeDeciderImpl;
import org.apache.jena.datatypes.TypeMapper;

public class RDFNodeMappers {
	public static RDFNodeMapper<Object> from(TypeDecider typeDecider) {
		TypeMapper typeMapper = TypeMapper.getInstance();

		RDFNodeMapper<Object> result = from(typeMapper, typeDecider);
		return result;
	}

	public static RDFNodeMapper<Object> from(TypeMapper typeMapper, TypeDecider typeDecider) {
		RDFNodeMapper<Object> result = from(Object.class, typeMapper, typeDecider);
		return result;
	}

	public static RDFNodeMapper<Object> from(TypeMapper typeMapper) {
		RDFNodeMapper<Object> result = from(Object.class, typeMapper, new TypeDeciderImpl());
		return result;
	}

	public static <T> RDFNodeMapper<T> from(Class<T> viewBaseClass, TypeDecider typeDecider) {
		TypeMapper typeMapper = TypeMapper.getInstance();
		return new RDFNodeMapperImpl<T>(viewBaseClass, typeMapper, typeDecider);
	}

	public static <T> RDFNodeMapper<T> from(Class<T> viewBaseClass, TypeMapper typeMapper, TypeDecider typeDecider) {
		return new RDFNodeMapperImpl<T>(viewBaseClass, typeMapper, typeDecider);
	}
	
}
