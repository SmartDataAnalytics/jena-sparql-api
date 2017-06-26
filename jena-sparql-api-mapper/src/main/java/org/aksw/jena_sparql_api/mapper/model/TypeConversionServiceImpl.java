package org.aksw.jena_sparql_api.mapper.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TypeConversionServiceImpl
    implements TypeConversionService
{
    //protected Multimap<String, RDFDatatype> converters = HashMultimap.create();
    protected Map<String, Map<Class<?>, TypeConverter>> dtToClazzToFn = new HashMap<>();

    //protected Map<Class<>, Map<Class<?>, Function<Expr, Expr>>> dtToClazzToFn = new HashMap<>();

    //protected ConversionService conversionService;


//    public void put(String datatypeIri, Class<?> clazz, Class<? extends Expr> expr) {
//    	put(datatypeIri, clazz, (e) -> expr)
//    }

    public void put(TypeConverter typeConverter) {
        String datatypeURI = typeConverter.getDatatypeURI();
        Class<?> javaClass = typeConverter.getJavaClass();

        dtToClazzToFn
            .computeIfAbsent(datatypeURI, x -> new HashMap<>())
            .put(javaClass, typeConverter);
    }

    public TypeConverter getConverter(String datatypeIri, Class<?> clazz) {
        TypeConverter result = dtToClazzToFn.getOrDefault(datatypeIri, Collections.emptyMap()).get(clazz);
        return result;
    }

//
//    public Function<Expr, Expr> getConverter(String datatypeIri, Class<?> clazz) {
//        //Map<Class<?>, RDFDatatype> clazzToDt = converters.getOrDefault(datatypeIri, Collections.emptyMap());
//        Collection<RDFDatatype> candidates = converters.get(datatypeIri);
//
//
//        //Entry<RDFDatatype, Integer> bestMatch = null;
//        RDFDatatype result = null;
//        for(RDFDatatype dt : candidates) {
//            Class<?> dtClass = dt.getJavaClass();
//            boolean isCompatibleClass = dtClass.isAssignableFrom(clazz);
//
//            if(isCompatibleClass) {
//                result = dt;
//                break;
//            }
////            } else {
////                boolean isConvertible = conversionService.canConvert(clazz, dtClass);
////                if(isConvertible) {
////                    result = dt;
////                }
////            }
//        }
//
//        return result;
//    }

}