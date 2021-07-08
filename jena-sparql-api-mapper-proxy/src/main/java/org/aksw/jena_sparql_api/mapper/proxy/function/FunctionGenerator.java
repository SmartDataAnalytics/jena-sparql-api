package org.aksw.jena_sparql_api.mapper.proxy.function;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.aksw.commons.collections.IterableUtils;
import org.aksw.commons.util.convert.Converter;
import org.aksw.commons.util.convert.ConverterImpl;
import org.aksw.commons.util.convert.ConverterRegistry;
import org.aksw.commons.util.convert.ConverterRegistryImpl;
import org.aksw.jena_sparql_api.mapper.annotation.DefaultValue;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.Function;

import com.google.common.collect.Iterables;


/**
 * Class for generation of wrappers for Java methods that make them usable as
 * {@link Function}s in Jena's SPARQL engine.
 *
 * @author raven
 *
 */
public class FunctionGenerator {

    protected TypeMapper typeMapper;
    protected ConverterRegistry converterRegistry;

    /** Declarations for mapping jena argument java types to jena
     *  For example, jena.GeometryWrapper may be mapped to jts.Geometry
     *
     *  The cardinality is n:1 - so many jena types may be mapped to the same argument type
     */
    // protected Map<Class<?>, Class<?>> argumentTypeMap;

    /** Declarations for mapping java types to jena internal types
     *  For example, jts.Geometry map by remapped to jena.GeometryWrapper
     *
     *  The cardinality is 1:1 - one return type can only map to one jena type
     */
    protected Map<Class<?>, Class<?>> returnTypeMap;

    /* WKTDatype (subclass of RDFDatatype) as of Jena 4 lacks the info about the corresponding Java class
     * So we have to add support for working around missing java class declaration... */
    protected Map<Class<?>, String> typeByClassOverrides = new HashMap<>();

    public FunctionGenerator() {
        this(TypeMapper.getInstance(), new ConverterRegistryImpl(), new HashMap<>());
    }

    public FunctionGenerator(
            TypeMapper typeMapper,
            ConverterRegistry converterRegistry,
            Map<Class<?>, Class<?>> returnTypeMap) {
        super();
        this.typeMapper = typeMapper;
        this.converterRegistry = converterRegistry;
        this.returnTypeMap = returnTypeMap;
    }

    public Map<Class<?>, String> getTypeByClassOverrides() {
        return typeByClassOverrides;
    }

    public TypeMapper getTypeMapper() {
        return typeMapper;
    }

    public ConverterRegistry getConverterRegistry() {
        return converterRegistry;
    }

    public Map<Class<?>, Class<?>> getJavaToRdfTypeMap() {
        return returnTypeMap;
    }


    public Converter getPreConvert(Class<?> targetJavaType, Class<?> internalJavaType) {
        Converter preConvert = null;
        if (internalJavaType != null) {
            preConvert = converterRegistry.getConverter(targetJavaType, internalJavaType);

            if (preConvert == null) {
                throw new RuntimeException(String.format("Conversion from %1$s to %2$s declared but no converter found",
                        targetJavaType, internalJavaType));
            }
        }

        return preConvert;
    }

    public Function wrap(Method method) {
        return wrap(method, null);
    }


    /**
     * Pendant counterpart to Guava's:
     * Iterators.getNext(Iterators.filter(Arrays.asList(arr).iterator(), type), null)
     *
     * @param <T>
     * @param arr
     * @param type
     * @return
     */
//	@SuppressWarnings("unchecked")
//	public static <T> List<T> findItemsByType(Object[] arr, Class<T> type) {
//		ArrayList<T> result = new ArrayList<>();
//		for (int i = 0; i < arr.length; ++i) {
//			Object item = arr[i];
//
//			if (item != null && type.isAssignableFrom(item.getClass())) {
//				result.add((T)item);
//			}
//		}
//
//		return result;
//	}


    public Function wrap(Method method, Object invocationTarget) {
        // Set up conversion of the result value
        java.util.function.Function<Object, NodeValue> returnValueConverter;
        {
            Converter resultConverter;
            Class<?> targetJavaType = method.getReturnType();
            AnnotatedType art = method.getAnnotatedReturnType();

            // TODO Check for an @IriType annotation that would turn Strings into IRIs

            Class<?> internalJavaType = returnTypeMap.get(targetJavaType);
            Class<?> workingType = internalJavaType != null ? internalJavaType : targetJavaType;

            Converter preConvert = internalJavaType == null ? null : getPreConvert(targetJavaType, internalJavaType);

            Converter internalTypeToNodeValue = createNodeValueMapper(
                    workingType,
                    converterRegistry,
                    typeMapper,
                    typeByClassOverrides);


            resultConverter = preConvert == null
                ? internalTypeToNodeValue
                : preConvert.andThen(internalTypeToNodeValue);

            returnValueConverter = in -> (NodeValue)resultConverter.convert(in);
        }

        // Set up parameter conversions and default values
        int n = method.getParameterCount();
        Class<?>[] pts = method.getParameterTypes();
        // AnnotatedType[] apts = method.getAnnotatedParameterTypes();
        Annotation[][] pas = method.getParameterAnnotations();

        // Once a default value is seen all further parameters must also have a
        // specified default value
        int firstDefaultValueIdx = -1;

        Param[] params = new Param[n];
        for (int i = 0; i < n; ++i) {
            Annotation[] as = pas[i];


            DefaultValue defaultValueAnnotation = IterableUtils.expectZeroOrOneItems(
                    Iterables.filter(Arrays.asList(as), DefaultValue.class));
            Class<?> paramClass = pts[i];

            Class<?> inputClass = returnTypeMap.get(paramClass);
            Class<?> rdfClass = inputClass != null ? inputClass : paramClass;
            Converter inputConverter = inputClass == null ? null : getPreConvert(inputClass, paramClass);

            // Consult override map first because some datatypes may lack appropriate metadata
            String datatypeIri = typeByClassOverrides.get(rdfClass);

            RDFDatatype dtype = datatypeIri != null
                    ? typeMapper.getTypeByName(datatypeIri)
                    : typeMapper.getTypeByClass(rdfClass);

            // RDFDatatype dtype = typeMapper.getTypeByClass(rdfClass);

            if (dtype == null) {
                throw new RuntimeException(String.format("TypeMapper does not contain an entry for the java class %1$s", inputClass));
            }

            Object defaultValue = null;
            if (defaultValueAnnotation != null) {
                if (firstDefaultValueIdx < 0) {
                    firstDefaultValueIdx = i;
                }
                String str = defaultValueAnnotation.value();

                if (str != null) {
                    Object internalObj = dtype.parse(str);
                    defaultValue = FunctionAdapter.convert(internalObj, paramClass, converterRegistry);
                } else {
                    defaultValue = null;
                }
            } else {
                if (firstDefaultValueIdx >= 0) {
                    throw new RuntimeException(String.format(
                            "Parameter at index %d does not declare a default value although a prior parameter at index %d declared one",
                            i, firstDefaultValueIdx));
                }
            }

            Param param = new Param(paramClass, inputClass, inputConverter, defaultValue);
            params[i] = param;
        }

        FunctionAdapter result = new FunctionAdapter(
                method, invocationTarget,
                returnValueConverter, params,
                typeMapper, converterRegistry);

        return result;
    }

    public static Converter createNodeValueMapper(
            Class<?> clz,
            ConverterRegistry converterRegistry,
            TypeMapper typeMapper,
            Map<Class<?>, String> typeByClassOverrides) {
        // Check the converterRegistry for a direct conversion
        Converter result = converterRegistry.getConverter(clz, NodeValue.class);

        if (result == null) {
            String datatypeIri = typeByClassOverrides.get(clz);

            RDFDatatype dtype = datatypeIri != null
                    ? typeMapper.getTypeByName(datatypeIri)
                    : typeMapper.getTypeByClass(clz);

            // RDFDatatype dtype = typeMapper.getTypeByClass(clz);

            if (dtype == null) {
                throw new RuntimeException(String.format("No RDF datatype registered for %1$s", clz));
            }

            result = new ConverterImpl(clz, NodeValue.class, (Object obj) -> {
                Node node = NodeFactory.createLiteralByValue(obj, dtype);
                NodeValue r = NodeValue.makeNode(node);
                return r;
            });
        }

        return result;
    }
}
