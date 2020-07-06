package org.aksw.jena_sparql_api.rdf.collections;

import org.aksw.jena_sparql_api.mapper.proxy.TypeDecider;
import org.apache.jena.datatypes.TypeMapper;

public class RDFNodeMappers {

    /**
     * Convenience static function which just delegates to the ctor.
     *
     * RDFNodeMapperImpl.from() is slightly nicer to use than new RDFNodeMapperImpl<>().
     *
     *
     * @param <T>
     * @param viewBaseClass
     * @param typeMapper
     * @param typeDecider
     * @param isViewAll
     * @param enableCanAsCheck
     * @return
     */
    public static <T> RDFNodeMapper<T> from(
            Class<T> viewBaseClass,
            TypeMapper typeMapper,
            TypeDecider typeDecider,
            boolean polymorphicOnly,
            boolean enableCanAsCheck) {
        return new RDFNodeMapperImpl<T>(viewBaseClass, typeMapper, typeDecider, polymorphicOnly, enableCanAsCheck);
    }

//	public static RDFNodeMapper<Object> from(TypeDecider typeDecider) {
//	TypeMapper typeMapper = TypeMapper.getInstance();
//
//	RDFNodeMapper<Object> result = from(typeMapper, typeDecider);
//	return result;
//}

//public static RDFNodeMapper<Object> from(TypeMapper typeMapper, TypeDecider typeDecider) {
//	RDFNodeMapper<Object> result = from(Object.class, typeMapper, typeDecider);
//	return result;
//}
//
//public static RDFNodeMapper<Object> from(TypeMapper typeMapper) {
//	RDFNodeMapper<Object> result = from(Object.class, typeMapper, new TypeDeciderImpl());
//	return result;
//}

//public static <T> RDFNodeMapper<T> from(Class<T> viewBaseClass, TypeDecider typeDecider) {
//	TypeMapper typeMapper = TypeMapper.getInstance();
//	return new RDFNodeMapperImpl<T>(viewBaseClass, typeMapper, typeDecider);
//}

}
