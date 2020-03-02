package org.aksw.jena_sparql_api.mapper.proxy;

import java.beans.Introspector;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.aksw.commons.accessors.CollectionFromConverter;
import org.aksw.commons.accessors.ListFromConverter;
import org.aksw.commons.collections.sets.SetFromCollection;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;
import org.aksw.jena_sparql_api.mapper.annotation.PolymorphicOnly;
import org.aksw.jena_sparql_api.rdf.collections.ConverterFromNodeMapper;
import org.aksw.jena_sparql_api.rdf.collections.ConverterFromNodeMapperAndModel;
import org.aksw.jena_sparql_api.rdf.collections.ConverterFromRDFNodeMapper;
import org.aksw.jena_sparql_api.rdf.collections.ListFromRDFList;
import org.aksw.jena_sparql_api.rdf.collections.NodeMapper;
import org.aksw.jena_sparql_api.rdf.collections.NodeMapperFromRdfDatatype;
import org.aksw.jena_sparql_api.rdf.collections.NodeMappers;
import org.aksw.jena_sparql_api.rdf.collections.RDFNodeMapper;
import org.aksw.jena_sparql_api.rdf.collections.RDFNodeMappers;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.jena_sparql_api.rdf.collections.SetFromLiteralPropertyValues;
import org.aksw.jena_sparql_api.rdf.collections.SetFromMappedPropertyValues;
import org.aksw.jena_sparql_api.rdf.collections.SetFromPropertyValues;
import org.aksw.jena_sparql_api.utils.views.map.MapFromKeyConverter;
import org.aksw.jena_sparql_api.utils.views.map.MapFromResource;
import org.aksw.jena_sparql_api.utils.views.map.MapFromValueConverter;
import org.aksw.jena_sparql_api.utils.views.map.MapVocab;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.PathParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Converter;
import com.google.common.base.Defaults;
import com.google.common.collect.Lists;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.Proxy;



/*
 * 
 * TODO Check if the points below have been addressed
 * 
 * The getter/setter detection is suboptimal, due to the following issues
 * - IriType needs to be present on both getter and setter, as the information is not stored
 *   on the property (so the setter does not know about the annotation if there is a getter)
 * - Collection views are not supported; (but halfway implemented)
 * - In general bean property detection is messy, although we just want to check all available
 *   methods on a type (for now let's not bother about declared methods), and then assemble the
 *   properties.
 *   
 * 
 * 
 *   
 * 
 * 
 * 
 * 
 * 
 */




//class CollectionGetter {
//	
//}
//
//class MethodDescriptorCollection {
//	
//}
//
//class MethodDescriptorCollectionSetter {
//	protected Class<?> collectionType;
//	protected Class<?> itemType;
//	
//	protected Class<?> returnType;
//	protected boolean isReturnTypeFluentCompatible;
//}
//

/**
 * 
 * TODO IriType annotation currently has to be provided on both getter and setter
 * 
 * @author Claus Stadler, Nov 29, 2018
 *
 */
public class MapperProxyUtils {

	
	private static final Logger logger = LoggerFactory.getLogger(MapperProxyUtils.class);

	
	// Getter must be no-arg methods, whose result type is either a subclass of
	// RDFNode or a type registered at jena's type factory
	
    public static List<Class<?>> extractItemTypes(Type genericType) {
        List<Class<?>> result = new ArrayList<>();
        if(genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType)genericType;
            java.lang.reflect.Type[] types = pt.getActualTypeArguments();
            for( java.lang.reflect.Type argType : types) {
            	if(argType instanceof Class) {
            		result.add((Class<?>)argType);
            	} else if(argType instanceof WildcardType) {
            		// TODO We should take bounds into account
            		result.add(Object.class);
            	} else {
            		result.add(null);
            		//throw new RuntimeException("Don't know how to handle " + argType);
            	}            	
            }
        }
        return result;
    }

    public static Entry<Class<?>, Class<?>> extractMapTypes(Type genericType) {
    	Entry<Class<?>, Class<?>> result = null;
        List<Class<?>> types = extractItemTypes(genericType);
        if(types.size() == 2) {
        	Class<?> keyType = types.get(0);
        	Class<?> valueType = types.get(1);
        	if(keyType != null && valueType != null) {
        		result = Maps.immutableEntry(keyType, valueType);
        	} else {
        		throw new RuntimeException("Don't know how to handle " + genericType);        		
        	}
        }
        return result;
    }

    public static Class<?> extractItemType(Type genericType) {
    	Class<?> result = null;
        List<Class<?>> types = extractItemTypes(genericType);
        if(types.size() == 1) {
        	Class<?> argType = types.get(0);
        	if(argType != null) {
        		result = argType;
        	} else {
        		throw new RuntimeException("Don't know how to handle " + genericType);        		
        	}
        }
    	
//        if(genericType instanceof ParameterizedType) {
//            ParameterizedType pt = (ParameterizedType)genericType;
//            java.lang.reflect.Type[] types = pt.getActualTypeArguments();
//            if(types.length == 1) {
//            	Type argType = types[0];
//            	if(argType instanceof Class) {
//            		result = (Class<?>)argType;
//            	} else if(argType instanceof WildcardType) {
//            		// TODO We should take bounds into account
//            		result = Object.class;
//            	} else {
//            		throw new RuntimeException("Don't know how to handle " + argType);
//            	}
//            }
//        }

        return result;
    }
   
//	public static Function<Class<?>, Function<Property, Function<Resource, Object>>>
//		viewAsDynamicList(Method m, boolean isIriType, TypeMapper typeMapper)
//	{
////		Function<Class<?>, Function<Property, Function<Resource, Object>>> result = null;
////
////		// Check for subClassOf List
////		Class<?> baseItemType = canActAsCollectionView(m, List.class, true, null);
////		if(baseItemType != null) {
////
////			
////			
////			result = clazz -> viewAsCollectionViewer(m, isIriType, typeMapper, clazz);
////
////		}
//		return null;
//	}
    
    /**
     * The collection view factory first takes the class that denotes the item type as argument.
     * Then, for a given property, it yields a function that for a given subject
     * yields a collection view of the objects
     * 
     * TODO We should reuse the code from 'viewAsGetter': This class only differs in the aspect
     * that the item type is dynamic,
     * whereas viewAsGetter attempts to obtain a static item type using reflection.
     * 
     * @param m
     * @param typeMapper
     * @return
     */
	public static Function<Class<?>, Function<Property, Function<Resource, Object>>>
		viewAsDynamicSet(Method m, boolean isIriType, boolean isViewAll, TypeMapper typeMapper, TypeDecider typeDecider)
	{		
//
		Function<Class<?>, Function<Property, Function<Resource, Object>>> result = null;
//
//		// Check for subClassOf List
//		Class<?> baseItemType = canActAsCollectionView(m, List.class, true, null);
//		if(baseItemType != null) {
//
//			
//			
//			result = clazz -> viewAsCollectionViewer(m, isIriType, typeMapper, clazz);
//
//		}
//		
		// Check for superClassOfSet
		Class<?> baseItemType = canActAsCollectionView(m, Set.class, false, null);


		if(baseItemType != null) {
			result = clazz -> viewAsSet(m, isIriType, isViewAll, clazz, typeMapper, typeDecider);
		}
		
		return result;
	}


	public static Function<Class<?>, Function<Property, Function<Resource, Object>>>
		viewAsDynamicList(Method m, boolean isIriType, boolean isViewAll, TypeMapper typeMapper, TypeDecider typeDecider)
	{		
		Function<Class<?>, Function<Property, Function<Resource, Object>>> result = null;

		// Check for superClassOfSet
		Class<?> baseItemType = canActAsCollectionView(m, List.class, true, null);
	
	
		if(baseItemType != null) {
			result = clazz -> viewAsList(m, isIriType, isViewAll, clazz, typeMapper, typeDecider);
		}
		
		return result;
	}

	public static Function<Property, Function<Resource, Object>> viewAsSet(Method m, boolean isIriType, boolean isViewAll, Class<?> itemType, TypeMapper typeMapper, TypeDecider typeDecider) {
		Function<Property, Function<Resource, Object>> result = null;

//		boolean isIriType = m.getAnnotation(IriType.class) != null;
		if(String.class.isAssignableFrom(itemType) && isIriType) {
			result = p -> s -> new SetFromMappedPropertyValues<>(s, p, NodeMappers.uriString);						
		} else {
			RDFNodeMapper<?> rdfNodeMapper = RDFNodeMappers.from(itemType, typeMapper, typeDecider, isViewAll);

			result = p -> s ->
				new SetFromCollection<>(
						new CollectionFromConverter<>(
								new SetFromPropertyValues<>(s, p, RDFNode.class),
								new ConverterFromRDFNodeMapper<>(rdfNodeMapper)));						
		}

		return result;
	}

	public static Function<Property, Function<Resource, Object>> viewAsSetOld(Method m, boolean isIriType, TypeMapper typeMapper, Class<?> itemType) {
		Function<Property, Function<Resource, Object>> result = null;

//		boolean isIriType = m.getAnnotation(IriType.class) != null;
		if(String.class.isAssignableFrom(itemType) && isIriType) {
			result = p -> s -> new SetFromMappedPropertyValues<>(s, p, NodeMappers.uriString);						
		} else if(RDFNode.class.isAssignableFrom(itemType)) {
			@SuppressWarnings("unchecked")
			Class<? extends RDFNode> rdfType = (Class<? extends RDFNode>)itemType;
			result = p -> s -> new SetFromPropertyValues<>(s, p, rdfType);						
		} else {
			RDFDatatype dtype = typeMapper.getTypeByClass(itemType);
			
			if(dtype != null) {
				result = p -> s -> new SetFromLiteralPropertyValues<>(s, p, itemType);
			}
			
			// This method can only return null, if itemType is neither a subclass of
			// RDFNode nor registered in the given type mapper
		}

		return result;
	}
	
	
	public static Function<Property, Function<Resource, Object>>
		viewAsMap(Method m, boolean isValueIriType, boolean isViewAll, Class<?> keyType, Class<?> valueType, TypeMapper typeMapper, TypeDecider typeDecider)
	{
		Function<Property, Function<Resource, Object>> result = null;
		
	//	boolean isIriType = m.getAnnotation(IriType.class) != null;
		if(String.class.isAssignableFrom(valueType) && isValueIriType) {
			throw new RuntimeException("Not implemented yet");
//			result = p -> s ->
//				new ListFromConverter<String, RDFNode>(
//						new ListFromRDFList(s, p),
//						new ConverterFromNodeMapperAndModel<>(s.getModel(), RDFNode.class, new ConverterFromNodeMapper<>(NodeMappers.uriString)));						
		} else {
			RDFNodeMapper<?> keyMapper = RDFNodeMappers.from(keyType, typeMapper, typeDecider, isViewAll);
			Converter<RDFNode, ?> keyConverter  = new ConverterFromRDFNodeMapper<>(keyMapper);

			RDFNodeMapper<?> valueMapper = RDFNodeMappers.from(valueType, typeMapper, typeDecider, isViewAll);
			Converter<RDFNode, ?> valueConverter  = new ConverterFromRDFNodeMapper<>(valueMapper);
			
			result = p -> s -> 
					new MapFromValueConverter<>(new MapFromKeyConverter<>(
						new MapFromResource(s, p, MapVocab.key, MapVocab.value),
					keyConverter), valueConverter);						
		}
	
		return result;
	}

	
	
	public static Function<Property, Function<Resource, Object>>
		viewAsList(Method m, boolean isIriType, boolean isViewAll, Class<?> itemType, TypeMapper typeMapper, TypeDecider typeDecider)
	{
		Function<Property, Function<Resource, Object>> result = null;
	
	//	boolean isIriType = m.getAnnotation(IriType.class) != null;
		if(String.class.isAssignableFrom(itemType) && isIriType) {
			result = p -> s ->
				new ListFromConverter<String, RDFNode>(
						new ListFromRDFList(s, p),
						new ConverterFromNodeMapperAndModel<>(s.getModel(), RDFNode.class, new ConverterFromNodeMapper<>(NodeMappers.uriString)));						
		} else {
			RDFNodeMapper<?> rdfNodeMapper = RDFNodeMappers.from(itemType, typeMapper, typeDecider, isViewAll);

			result = p -> s -> new ListFromConverter<>(
					new ListFromRDFList(s, p),
					new ConverterFromRDFNodeMapper<>(rdfNodeMapper));//new SetFromPropertyValues<>(s, p, rdfType);						
		}
	
		return result;
	}
	
	
	public static Function<Property, Function<Resource, Object>>
		viewAsListOld(Method m, boolean isIriType, TypeMapper typeMapper, Class<?> itemType)
	{
		Function<Property, Function<Resource, Object>> result = null;
	
	//	boolean isIriType = m.getAnnotation(IriType.class) != null;
		if(String.class.isAssignableFrom(itemType) && isIriType) {
			result = p -> s ->
				new ListFromConverter<String, RDFNode>(
						new ListFromRDFList(s, p),
						new ConverterFromNodeMapperAndModel<>(s.getModel(), RDFNode.class, new ConverterFromNodeMapper<>(NodeMappers.uriString)));						
		} else if(RDFNode.class.isAssignableFrom(itemType)) {
			@SuppressWarnings("unchecked")
			Class<? extends RDFNode> rdfType = (Class<? extends RDFNode>)itemType;
			
			// TODO Apply a filter to the list
			
			result = p -> s -> new ListFromConverter<>(new ListFromRDFList(s, p), Converter.<RDFNode, RDFNode>from(r -> r, r -> r.as(rdfType)));//new SetFromPropertyValues<>(s, p, rdfType);						
		} else {
			RDFDatatype dtype = typeMapper.getTypeByClass(itemType);
			
			if(dtype != null) {
				result = p -> s -> new ListFromConverter<>(new ListFromRDFList(s, p), new ConverterFromNodeMapperAndModel<RDFNode, Object>(s.getModel(), RDFNode.class, new ConverterFromNodeMapper<>(new NodeMapperFromRdfDatatype<Object>(dtype))));	 
				
				//new SetFromLiteralPropertyValues<>(s, p, itemType);
			}
			
			// This method can only return null, if itemType is neither a subclass of
			// RDFNode nor registered in the given type mapper
		}
	
		return result;
	}
	
	public static MethodDescriptor classifyMethod(Method m) {
		MethodDescriptor result = null;

		result = ObjectUtils.firstNonNull(
				tryClassifyAsMapGetter(m),
				//tryClassifyAsMapSetter(m),
				tryClassifyAsDynamicCollectionGetter(m),
				tryClassifyAsCollectionGetter(m),
				tryClassifyAsCollectionSetter(m),
				tryClassifyAsScalarGetter(m),
				tryClassifyAsScalarSetter(m));		
	
		return result;
	}
	
	

	/**
	 * If the method qualifies as a getter, returns a factory function
	 * that for a given property yields another function that accesses this property for a 
	 * 
	 * @param m
	 * @param typeMapper
	 * @return
	 */
	public static MethodDescriptor tryClassifyAsScalarGetter(
			Method m) {
			//boolean isIriType,
			//TypeMapper typeMapper) {
		MethodDescriptor result;
		
		Class<?> returnType = m.getReturnType();		
		int paramCount = m.getParameterCount();
			
		result = paramCount == 0 && !Iterable.class.isAssignableFrom(returnType)  
				? MethodDescriptor.simpleGetter(m, returnType)
				: null;
			
//			if(RDFNode.class.isAssignableFrom(returnType)) {
//				@SuppressWarnings("unchecked")
//				Class<? extends RDFNode> rdfType = (Class<? extends RDFNode>)returnType;
//				
//				result = MethodDescriptor.simpleGetter(m, rdfType);
//			} else {
////				boolean isIriType = m.getAnnotation(IriType.class) != null;
//				if(isIriType) {
//					if(!String.class.isAssignableFrom(returnType)) {
//						// TODO Change to warning
//						throw new RuntimeException("@IriType annotation requires String type");
//					}
//										
//					// TODO 
//					result = MethodDescriptor.simpleGetter(m, returnType);		 					
//				} else {
//					result = MethodDescriptor.simpleGetter(m, returnType);
//					
////					RDFDatatype dtype = typeMapper.getTypeByClass(returnType);
////	
////					if(dtype != null) {
//////						result = p -> s -> ResourceUtils.getLiteralPropertyValue(s, p, returnType);
////					}
//				}
//			}
//		}
//		else if(paramCount == 1) {
//			// Match getters that return collection views, such as
//			// <T> Iterable<T> getSomeCollection(Class<T> itemClazz)
//		}
		
		return result; //result;
	}
	
	
	
	public static MethodDescriptor tryClassifyAsScalarSetter(Method m) {
		MethodDescriptor result = null;

		Class<?> clazz = m.getDeclaringClass();
		Class<?> returnType = m.getReturnType();
		
		int paramCount = m.getParameterCount();
		
		if(paramCount == 1) {
			Class<?> paramType = m.getParameterTypes()[0];
			
			// Deal with (non-nested) collections first
			if(!Iterable.class.isAssignableFrom(paramType)) {
				boolean isFluentCompatible = returnType.isAssignableFrom(clazz);
				
				result = MethodDescriptor.simpleSetter(m, isFluentCompatible, paramType);
			}
		}
		
		return result;
//
//		
//		result = paramCount == 1 && !Iterable.class.isAssignableFrom(returnType)  
//				? MethodDescriptor.simpleGetter(m, returnType)
//				: null;
//
//		return result; //result;
	}
	
	
	public static MethodDescriptor tryClassifyAsCollectionSetter(Method m) {
		MethodDescriptor result = null;

		Class<?> clazz = m.getDeclaringClass();
		Class<?> returnType = m.getReturnType();
		
		int paramCount = m.getParameterCount();
		
		if(paramCount == 1) {
			Class<?> paramType = m.getParameterTypes()[0];
			
			// Deal with (non-nested) collections first
			if(Iterable.class.isAssignableFrom(paramType)) {
				Class<?> itemType = extractItemType(m.getParameters()[0].getParameterizedType());
				boolean isFluentCompatible = returnType.isAssignableFrom(clazz);
				
				result = MethodDescriptor.collectionSetter(m, isFluentCompatible, paramType, itemType);
			}
		}
		
		return result;
	}

	

	public static MethodDescriptor tryClassifyAsMapGetter(Method m) {
		MethodDescriptor result = null;
		Class<?> returnType = m.getReturnType();
		
		int paramCount = m.getParameterCount();

		//boolean isIterableReturnType = false;
		// Class<?> itemType = null;
	
		
		if(paramCount == 0) {
			// Deal with (non-nested) collections first
			if(Map.class.isAssignableFrom(returnType)) {
				Entry<Class<?>, Class<?>> mapTypes = extractMapTypes(m.getGenericReturnType());
				if(mapTypes != null) {
					result = MethodDescriptor.mapGetter(m, mapTypes);
				}				
			}
		}
		
		return result;
	}
	
	/* TODO TBD
	public static MethodDescriptor tryClassifyAsMapSetter(Method m) {
		MethodDescriptor result = null;

		Class<?> clazz = m.getDeclaringClass();
		Class<?> returnType = m.getReturnType();
		
		int paramCount = m.getParameterCount();
		
		if(paramCount == 1) {
			Class<?> paramType = m.getParameterTypes()[0];
			
			// Deal with (non-nested) collections first
			if(Iterable.class.isAssignableFrom(paramType)) {
				Class<?> itemType = extractItemType(m.getParameters()[0].getParameterizedType());
				boolean isFluentCompatible = returnType.isAssignableFrom(clazz);
				
				result = MethodDescriptor.collectionSetter(m, isFluentCompatible, paramType, itemType);
			}
		}
		
		return result;
	}
	*/
	
	
	public static MethodDescriptor tryClassifyAsCollectionGetter(Method m) {
		MethodDescriptor result = null;
		Class<?> returnType = m.getReturnType();
		
		int paramCount = m.getParameterCount();

		//boolean isIterableReturnType = false;
		// Class<?> itemType = null;
	
		
		if(paramCount == 0) {
			// Deal with (non-nested) collections first
			if(Iterable.class.isAssignableFrom(returnType)) {
				Class<?> itemType = extractItemType(m.getGenericReturnType());
				if(itemType != null) {
					result = MethodDescriptor.collectionGetter(m, returnType, itemType);
				}				
			}
		}
		
		return result;
	}
	
	
	public static Function<Property, Function<Resource, Object>> viewAsScalarGetter(
			MethodDescriptor methodDescriptor,
			Class<?> effectiveType,
			boolean isIriType,
			boolean isViewAll,
			TypeMapper typeMapper,
			TypeDecider typeDecider) {
		Function<Property, Function<Resource, Object>> result = null;		
		
		if(methodDescriptor.isGetter()) {
			if(isIriType) {
				if(!String.class.isAssignableFrom(effectiveType)) {
					// TODO Change to warning
					throw new RuntimeException("@IriType annotation requires String type");
				}
				
				result = p -> s -> ResourceUtils.getPropertyValue(s, p, NodeMappers.uriString);					
			} else {
				RDFNodeMapper<?> rdfNodeMapper = RDFNodeMappers.from(effectiveType, typeMapper, typeDecider, isViewAll);
				result = p -> s -> ResourceUtils.getPropertyValue(s, p, (RDFNodeMapper)rdfNodeMapper);
			}
		}

		return result;
	}
	

	/**
	 * If the method qualifies as a getter, returns a factory function
	 * that for a given property yields another function that accesses this property for a 
	 * 
	 * @param m
	 * @param typeMapper
	 * @return
	 */
	public static Function<Property, Function<Resource, Object>> viewAsScalarGetterOldAndUnused(
			MethodDescriptor methodDescriptor,
			Class<?> effectiveType,
			boolean isIriType,
			TypeMapper typeMapper,
			TypeDecider typeDecider) {
		//MethodInfo result;
		//PropertyDescriptor
		//TypeResolver.
		
//		Method method = methodDescriptor.getMethod();
		//Class<?> type = methodDescriptor.getType();
		
		Function<Property, Function<Resource, Object>> result = null;		
		//int paramCount = m.getParameterCount();

		//boolean isIterableReturnType = false;
		// Class<?> itemType = null;
		
		
		
		if(methodDescriptor.isGetter()) {
//			// Deal with (non-nested) collections first
//			if(Iterable.class.isAssignableFrom(returnType)) {
//				Class<?> itemType = extractItemType(m.getGenericReturnType());
//				if(itemType != null) {
//					result = viewAsCollectionViewer(m, typeMapper, itemType);
//				}				
//			}
			if(RDFNode.class.isAssignableFrom(effectiveType)) {
				@SuppressWarnings("unchecked")
				Class<? extends RDFNode> rdfType = (Class<? extends RDFNode>)effectiveType;
				result = p -> s -> ResourceUtils.getPropertyValue(s, p, rdfType);
			} else {
				if(isIriType) {
					if(!String.class.isAssignableFrom(effectiveType)) {
						// TODO Change to warning
						throw new RuntimeException("@IriType annotation requires String type");
					}
					
					result = p -> s -> ResourceUtils.getPropertyValue(s, p, NodeMappers.uriString);					
				} else {
					Object defaultValue = effectiveType.isPrimitive()
							? Defaults.defaultValue(effectiveType)
							: null;
					
					RDFDatatype dtype = typeMapper.getTypeByClass(effectiveType);
	
					if(dtype != null) {
						result = p -> s -> {
							Object r = ResourceUtils.getLiteralPropertyValue(s, p, effectiveType);
							if(r == null) {
								r = defaultValue;
//								System.out.println(p + " " + effectiveType + " " + defaultValue);
							}
							return r;
						};
					}
				}
			}
		}
//		else if(paramCount == 1) {
//			// Match getters that return collection views, such as
//			// <T> Iterable<T> getSomeCollection(Class<T> itemClazz)
//		}
		
		return result;
	}
	
	
	/**
	 * Check whether the method is compatible with the signature
	 * 
	 * VoidOrSuperClassOfDeclaringClass myCandidateSetter(Iterable<?>)
	 * 
	 * @return
	 */
	public static boolean matchesCollectionViewSetter(Method m) {
		// Check whether this is a setter of a collection
		// If so, check whether there is collection view getter
		// If so, simply create a setter implementation that
		//   calls the getter, clears the items of the returned collection
		//   and then copies items from the argument collection
				
		
		return false;
	}
	

	/**
	 * Check whether the method is compatible with the signature
	 * 
	 * Iterable myMethod(Class);
	 * 
	 * This means the following conditions are satisfied:
	 * <ul>
	 *   <li>The method's result is a super (or equal) class of the returnType argument</li>
	 *   <li>There exists exactly one argument</li>
	 *   <li>The argument is a super (or equal) class of the given argType argument</li> 
	 * </ul>
	 * 
	 * 
	 * @param m
	 * @param iterableClass
	 * @param typeVariableBound
	 * @return
	 */
	public static boolean matchesDynamicCollectionViewGetter(Method m, Class<?> expectedReturnType, boolean expectSubClassOfIterable, Class<?> expectedArgType) {
		boolean result = false;

		Class<?> actualReturnType = m.getReturnType();
		
		boolean isCollectionTypeMatch = expectSubClassOfIterable
				? expectedReturnType.isAssignableFrom(actualReturnType)
				: actualReturnType.isAssignableFrom(expectedReturnType);
		
		if(isCollectionTypeMatch) {
			Class<?>[] pts = m.getParameterTypes();
			if(pts.length == 1) {
				Class<?> pt = pts[0];
				
				if(pt.isAssignableFrom(expectedArgType)) {
					result = true;
				}
			}
		}
		
		return result;
	}
	
	
	public static MethodDescriptor tryClassifyAsDynamicCollectionGetter(Method m) {
		Class<?> boundedType = canActAsCollectionView(m, Iterable.class, true, null);
		
		MethodDescriptor result = boundedType == null
				? null
				: MethodDescriptor.dynamicCollectionGetter(m, m.getReturnType(), boundedType);
		
//		Class<?> actualReturnType = m.getReturnType();
//
////		MethodDescriptor result = MethodDescriptor.dynamicCollectionGetter(method, collectionType, boundedItemType)
////		
////		boolean isCollectionTypeMatch = expectSubClassOfIterable
////				? expectedReturnType.isAssignableFrom(actualReturnType)
////				: actualReturnType.isAssignableFrom(expectedReturnType);
//		
//		if(Iterable.class.isAssignableFrom(actualReturnType)) {
//			Class<?>[] pts = m.getParameterTypes();
//			if(pts.length == 1) {
//				Class<?> pt = pts[0];
//				
//				if(pt.isAssignableFrom(Class.class)) {
//					result = true;
//				}
//			}
//		}
//		
		return result;
	}

	
	/**
	 * Check if the method signature matches the pattern:
	 * 
	 * IterableClass<T extends SomeSubClassOfResource> IterableBaseClass[T] method(Class[T])
	 * 
	 * IterableClass: A candidate method must return a this class or a super class.
	 * Example: If a method with a return type of Set.class is searched, then methods with return type Iterable and Collection
	 * are matches, but HashSet and List are not.
	 * 
	 * Returns the itemType if the method is a match - null otherwise.
	 * 
	 * 
	 * @param m
	 * @param iterableClass
	 * @param typeVariableBound The 
	 * @return
	 */
	public static Class<?> canActAsCollectionView(Method m, Class<?> iterableClass, boolean expectSubClassOfIterable, Class<?> typeVariableBound) {
		// Check if there is exactly one type variable
		Class<?> result = null;
		boolean isRawMatch = matchesDynamicCollectionViewGetter(m, iterableClass, expectSubClassOfIterable, Class.class);

		if(isRawMatch) {
			
			TypeVariable<Method>[] tps = m.getTypeParameters();
			if(tps.length == 1) {
				// Check whether there is exactly 1 type variable that is bound to a subclass
				// of resource
				TypeVariable<Method> tv = tps[0];
				Type[] bounds = tv.getBounds();
				
				Type bound = null;
				switch(bounds.length) {
				case 0:
					bound = Object.class;
					break;
				case 1:
					bound = bounds[0];
					break;
				default:
					logger.debug("Candidate collection view rejected, because exactly 1 bound expected, got: " + bounds.length + " " + Arrays.asList(bounds) + "; " + m);
					break;
				}
				
				if(bound != null && bound instanceof Class<?>) {
					Class<?> boundClass = (Class<?>)bound;
					
					// Check if the boundClass is a sub class of the given bound
					boolean isCompatibleBound = typeVariableBound == null || typeVariableBound.isAssignableFrom(boundClass);
					
					if(isCompatibleBound) {
						result = boundClass;
						logger.debug("Candidate collection view accepted; detected item type " + result + "; " + m);
					} else {
						logger.debug("Candidate collection view rejected, because bound class " + boundClass + " does not satisfy compatibility with " + typeVariableBound);
					}
				} else {
					logger.debug("Candidate collection view rejected, because bound is a type but not a class " + bound + "; " + m);
					
				}
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static Function<Property, BiConsumer<Resource, Object>> viewAsCollectionView(Method m, TypeMapper typeMapper) {
		///boolean canAct
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static Function<Property, BiConsumer<Resource, Object>> viewAsScalarSetter(
			MethodDescriptor methodDescriptor,
			Class<?> effectiveType,
			boolean isIriType,
			boolean isViewAll,
			TypeMapper typeMapper,
			TypeDecider typeDecider) {
		// Strict setters return void, but e.g. in the case of fluent APIs return types may vary 

		Function<Property, BiConsumer<Resource, Object>> result = null;		
		Class<?> paramType = effectiveType; //methodDescriptor.getType();
		
		if(isIriType) {
			if(!String.class.isAssignableFrom(paramType)) {
				// TODO Change to warning
				throw new RuntimeException("@IriType annotation requires String type");
			}
			result = p -> (s, o) -> ResourceUtils.updateProperty(s, p, (NodeMapper)NodeMappers.uriString, o);					
		} else {
			RDFNodeMapper<?> rdfNodeMapper = RDFNodeMappers.from(effectiveType, typeMapper, typeDecider, isViewAll);
			
			result = p -> (s, o) -> ResourceUtils.updateProperty(s, p, (RDFNodeMapper)rdfNodeMapper, o);
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static Function<Property, BiConsumer<Resource, Object>> viewAsScalarSetterOld(
			MethodDescriptor methodDescriptor,
			Class<?> effectiveType,
			boolean isIriType,
			TypeMapper typeMapper,
			TypeDecider typeDecider) {
		// Strict setters return void, but e.g. in the case of fluent APIs return types may vary 

		Function<Property, BiConsumer<Resource, Object>> result = null;
		//Method m = methodDescriptor.getMethod();
		
		Class<?> paramType = effectiveType; //methodDescriptor.getType();
		
		//Class<?>[] paramTypes = m.getParameterTypes();
		
		if(RDFNode.class.isAssignableFrom(paramType)) {
//				@SuppressWarnings("unchecked")
//				Class<? extends RDFNode> rdfType = (Class<? extends RDFNode>)paramType;
			result = p -> (s, o) -> ResourceUtils.setProperty(s, p, (RDFNode)o);
		} else {
			if(isIriType) {
				if(!String.class.isAssignableFrom(paramType)) {
					// TODO Change to warning
					throw new RuntimeException("@IriType annotation requires String type");
				}
				result = p -> (s, o) -> ResourceUtils.updateProperty(s, p, (NodeMapper)NodeMappers.uriString, o);					
			} else {
				RDFDatatype dtype = typeMapper.getTypeByClass(paramType);
				
				if(dtype != null) {
					result = p -> (s, o) -> ResourceUtils.updateLiteralProperty(s, p, (Class)paramType, o);
				}
			}
		}
		
		return result;
	}
	
	public static String deriveBeanPropertyName(String methodName) {
		// TODO Check whether the subsequent character is upper case
		List<String> prefixes = Arrays.asList("get", "set", "is");
		
		String usedPrefix = prefixes.stream()
				.filter(methodName::startsWith)
				.findAny()
				.orElse(null);
		
		String result = usedPrefix != null ? methodName.substring(usedPrefix.length()) : methodName;
		
		// TODO We may want to use the Introspector's public decapitalize method
		result = Introspector.decapitalize(result);
		//result = StringUtils.uncapitalize(result);
		
		return result;
	}
	
	public static P_Path0 derivePathFromMethod(Method method, PrefixMapping pm) {
		P_Path0 result = null;
		
		Iri iri = method.getAnnotation(Iri.class);
		IriNs iriNs = method.getAnnotation(IriNs.class);
		if(iri != null) {
			String rdfPropertyStr = iri.value();
			
			// Always expand URIs
			// FIXME This will break for general paths - perform prefix expansion using a path transformer!
			String expanded = pm.expandPrefix(rdfPropertyStr);
			String pathStr = "<" + expanded + ">";
			
			result = (P_Path0)PathParser.parse(pathStr, pm);

			//logger.debug("Parsed bean property RDF annotation " + pathStr + " into " + result + " on " + method);
			if(logger.isDebugEnabled()) {
				logger.debug("Found @Iri annotation on " + method + ":");
				if(Objects.equals(rdfPropertyStr, expanded)) {
					logger.debug("  " + rdfPropertyStr);
				} else {
					logger.debug("  " + rdfPropertyStr + " expanded to " + result);
				}
			}

			//Node p = NodeFactory.createURI(rdfPropertyStr);
			
			//result = new P_Link(p);
		} else if(iriNs != null) {
			String ns = iriNs.value();
			String uri = pm.getNsPrefixURI(ns);
			if(uri == null) {
				throw new RuntimeException("Undefined prefix: " + ns);
			}
			String localName = deriveBeanPropertyName(method.getName());

			result = new P_Link(NodeFactory.createURI(uri + localName));
			//result = (P_Path0)PathParser.parse(uri + localName, pm);
		}
		
//		System.out.println(method + " -> " + result);
//		if(result != null && result.toString().contains("style")) {
//			System.out.println("style here");
//		}
		return result;
	}

//	public static Multimap<String, Method> indexMethodsByBeanPropertyName(Class<?> clazz) {
//		Multimap<String, Method> result = ArrayListMultimap.create();
//		for(Method method : clazz.getMethods()) {
//			String methodName = method.getName();
//			String beanPropertyName = deriveBeanPropertyName(methodName);
//
//			result.put(beanPropertyName, method);
//		}
//		
//		return result;
//
//	}

	public static Map<String, P_Path0> indexPathsByBeanPropertyName(Class<?> clazz, PrefixMapping pm) {
		Map<String, P_Path0> result = new LinkedHashMap<>();
		for(Method method : clazz.getMethods()) {
			String methodName = method.getName();
			String beanPropertyName = deriveBeanPropertyName(methodName);
			P_Path0 path = derivePathFromMethod(method, pm);
			
			if(path != null) {
				result.put(beanPropertyName, path);
			}
		}
		
		return result;
	}

//	public static <T extends Resource> BiFunction<Node, EnhGraph, T> createProxyFactory(Class<T> clazz) {
//		return createProxyFactory(clazz, RDFa.prefixes);
//	}
	
	static class MyPropertyDescriptor {
		String propertyName;
		Method readMethod;
		Method writeMethod;
	}
	
	
	public static Class<?> getStricterType(Class<?> a, Class<?> b) {

		Class<?> result;

		if(a != null && b != null) {
			// Find the effective scalar type
			result = a.isAssignableFrom(b)
					? b
					: b.isAssignableFrom(a)
						? a
						: null;
		} else {
			result = ObjectUtils.firstNonNull(a, b);
		}
		return result;
	}
	
	/**
	 * Method level annotations are processed into property level ones. 
	 * 
	 * @param clazz
	 * @param pm
	 * @return
	 */
	public static <T extends Resource> BiFunction<Node, EnhGraph, T> createProxyFactory(
			Class<T> clazz,
			PrefixMapping pm,
			TypeDecider typeDecider) {

		// The map of implementations to be populated
		Map<Method, BiFunction<Object, Object[], Object>> methodImplMap = new LinkedHashMap<>();

		
		// Search for methods with @Iri annotation
		// getter pattern: any x()
		TypeMapper typeMapper = TypeMapper.getInstance();
		
		
		// Find all methods with a @Iri annotation
		Map<String, P_Path0> paths = indexPathsByBeanPropertyName(clazz, pm);

		Map<String, Method> readMethods = new LinkedHashMap<>();
		Map<String, Method> writeMethods = new LinkedHashMap<>();
		//Map<String, String> propertyName = new LinkedHashMap<>();

		Set<String> beanPropertyNames = Sets.union(readMethods.keySet(), writeMethods.keySet());
		
		
		Map<Method, MethodDescriptor> methodDescriptors = new HashMap<>();		
		
		Method[] methods = clazz.getMethods();
		
		for(Method method : methods) {
			
			// Proxy default methods
	    	// Performance note: Without caching of the default method delegate,
	    	// VisualVM reported around 80% of CPU time being used on
	    	// repeatedly setting up that method handle
	    	// These figures were observed with our "Conjure" system which
	    	// heavily uses the visitor pattern in conjunction with default methods
	    	// on Jena Resource classes
			if(method.isDefault()) {
				BiFunction<Object, Object[], Object> defaultMethodDelegate;
				try {
					defaultMethodDelegate = proxyDefaultMethod(method);
				} catch(Exception e) {
					throw new RuntimeException(e);
				}
				
				methodImplMap.put(method, defaultMethodDelegate);
				continue;
			}

			MethodDescriptor descriptor = classifyMethod(method);
			if(descriptor == null) {
				continue;
			}
			
			// Filter out brige methods that get introduced in case of covariant return types
			// - https://stackoverflow.com/questions/6204339/java-class-getmethods-behavior-on-overridden-methods
			// - https://stackoverflow.com/questions/1961350/problem-in-the-getdeclaredmethods-java
			if(method.isBridge()) {
				continue;
			}
			
			
			String beanPropertyName = deriveBeanPropertyName(method.getName());

			// Skip methods not associated with a path
			if(!paths.containsKey(beanPropertyName)) {
				continue;
			}
			
			methodDescriptors.put(method, descriptor);

			if(descriptor.isGetter()) {
//				System.out.println("READ: " + beanPropertyName + " " + method.isBridge() + " " + method);
				readMethods.put(beanPropertyName, method);
			} else if(descriptor.isSetter()) {
//				System.out.println("WRITE: " + beanPropertyName + " " + method.isBridge() + " " + method);
				writeMethods.put(beanPropertyName, method);				
			}
			
			P_Path0 path = derivePathFromMethod(method, pm);
			if(path != null) {
				paths.put(beanPropertyName, path);
			}
		}
		
		
//		System.out.println("BeanPropertyNames: " + beanPropertyNames);
		
		// Process properties
		for(String beanPropertyName : beanPropertyNames) {
			

			// Only consider properties that have a path
			P_Path0 path = paths.get(beanPropertyName);
			if(path == null) {
				continue;
			}

			
			Method readMethod = readMethods.get(beanPropertyName);
			MethodDescriptor readMethodDescriptor = methodDescriptors.get(readMethod);
			
			Method writeMethod = writeMethods.get(beanPropertyName);
			MethodDescriptor writeMethodDescriptor = methodDescriptors.get(writeMethod);
			
			// Check for presence of @IriType annotation
//			if(isIriType && !String.class.isAssignableFrom(returnType)) {
//				// TODO Change to warning
//				throw new RuntimeException("@IriType annotation requires String type");
//			}
			
			BiFunction<Object, Object[], Object> readImpl;
			BiFunction<Object, Object[], Object> writeImpl;

			
			Class<?> readType = null;
			Class<?> readCollectionType = null;
			Class<?> readItemType = null;
//			Class<?> readKeyType = null;
			boolean isReadIriType = false;
			boolean isReadViewAll = false;

			Class<?> writeType = null;
			Class<?> writeCollectionType = null;
			Class<?> writeItemType = null;
			boolean isWriteIriType = false;
			boolean isWriteViewAll = false;

			if(readMethodDescriptor != null) {
				readType = readMethodDescriptor.getType();
				readCollectionType = readMethodDescriptor.getCollectionType();
				readItemType = readMethodDescriptor.getItemType();
				isReadIriType = readMethod.getAnnotation(IriType.class) != null; 
				isReadViewAll = readMethod.getAnnotation(PolymorphicOnly.class) == null; 
			}
			
			if(writeMethodDescriptor != null) {
				writeType = writeMethodDescriptor.getType();
				writeCollectionType = writeMethodDescriptor.getCollectionType();
				writeItemType = writeMethodDescriptor.getItemType();
				isWriteIriType = writeMethod.getAnnotation(IriType.class) != null; 
				isWriteViewAll = writeMethod.getAnnotation(PolymorphicOnly.class) == null; 
			}

			Class<?> effectiveType = getStricterType(readType, writeType);
			Class<?> effectiveCollectionType = getStricterType(readCollectionType, writeCollectionType);
			Class<?> effectiveItemType = getStricterType(readItemType, writeItemType);
			boolean  isIriType = isReadIriType || isWriteIriType;
			boolean isViewAll = isReadViewAll || isWriteViewAll;
			
			Property p = ResourceFactory.createProperty(path.getNode().getURI());
//System.out.println(p);
			// It is only valid to have a read method without a write one
			// if it yields a collection view
			if(readMethod != null) {
				boolean isCollectionValued = readMethodDescriptor.isCollectionValued();
				boolean isDynamicGetter = readMethodDescriptor.isDynamicCollection();
				boolean isMapValued = readMethodDescriptor.isMapType();
				
//				if(isDynamicGetter) {
//					System.out.println("DEBUG POINT");
//				}
				if(isCollectionValued) {

					// Check if the write method is consistent
					if(writeMethod != null) {
						boolean isWriteMethodCollectionValued = writeMethodDescriptor.isCollectionValued();
						
						
						if(isCollectionValued != isWriteMethodCollectionValued) {
							throw new RuntimeException("Invalid type combination: collection and non-collection valued");
						}
						
						// Take the stricter collection type of read / write method

						if(effectiveCollectionType == null) {
							throw new RuntimeException("Incompatible collection types: " + readCollectionType + " vs " + writeCollectionType + " on " + readMethod + " and " + writeMethod);
						}
						
						
					}



					boolean isListType = List.class.isAssignableFrom(effectiveCollectionType);
					// set type is the default for collections if no other concrete type applies
					boolean isSetType =
							Set.class.isAssignableFrom(effectiveCollectionType)
							|| (effectiveCollectionType.isAssignableFrom(Set.class) && !isListType); 

					
					if(isDynamicGetter) {
						Function<Class<?>, Function<Property, Function<Resource, Object>>> collectionGetter;
						if(isSetType) {
							collectionGetter = viewAsDynamicSet(readMethod, isIriType, isViewAll, typeMapper, typeDecider);
						} else if(isListType) {
							collectionGetter = viewAsDynamicList(readMethod, isIriType, isViewAll, typeMapper, typeDecider);
						} else {
							throw new RuntimeException("todo dynamic collection support implement");
						}


						if(collectionGetter != null) {
							readImpl = (o, args) -> {
								Class<?> clz = Objects.requireNonNull((Class<?>)args[0]);
								Function<Property, Function<Resource, Object>> ps = collectionGetter.apply(clz);
								Function<Resource, Object> s = ps.apply(p);
								Object r = s.apply((Resource)o);
								return r;
							};
							
							methodImplMap.put(readMethod, readImpl);
						}
							
							
//							throw new RuntimeException("todo dynamic collection support implement");
					} else { // Non-dynamic collection handling
						Function<Property, Function<Resource, Object>>  collectionView;
						if(isListType) {
							collectionView = viewAsList(readMethod, isIriType, isViewAll, effectiveItemType, typeMapper, typeDecider);
						} else if(isSetType) {
							collectionView = viewAsSet(readMethod, isIriType, isViewAll, effectiveItemType, typeMapper, typeDecider);
						} else {
							throw new RuntimeException("Unsupported collection type");
						}
						
						
						Function<Resource, Object> raw = collectionView.apply(p);
						readImpl = (s, args) -> raw.apply((Resource)s);
						methodImplMap.put(readMethod, readImpl);
						
						
						// Implement write methods based on the read method
						if(writeMethod != null) {

							if(isListType || isSetType) {
								
								boolean returnThis = writeMethodDescriptor.isFluentCompatible();//effectiveItemType.isAssignableFrom(clazz);
	
								methodImplMap.put(writeMethod, (obj, args) -> {
									// Call the read method
									Object collectionViewObj = readImpl.apply(obj, new Object[] {});
									Collection collection = (Collection)collectionViewObj;
									
									List copy = Lists.newArrayList((Iterable)args[0]);
									collection.clear();
									collection.addAll(copy);
									
									Object r = returnThis ? obj: null;								
									return r;
								});
								
//								System.out.println("list type");
							} else { //if(effectiveCollectionType.isAssignableFrom(Set.class) ||
									//Set.class.isAssignableFrom(effectiveCollectionType)) {
								throw new RuntimeException("todo implement");
							}
						}
							
						
					}
//						else if(isListType) {
//							
//						}
//						throw new RuntimeException("todo dynamic collection support implement");
//						if(isListType) {
//							
//							Function<Class<?>, Function<Property, Function<Resource, Object>>> collectionGetter = viewAsDynamicList(readMethod, isIriType, typeMapper, effectiveItemType);
//							if(collectionGetter != null) {
//								readImpl = (o, args) -> {
//									Class<?> clz = Objects.requireNonNull((Class<?>)args[0]);
//									Function<Property, Function<Resource, Object>> ps = collectionGetter.apply(clz);
//									Function<Resource, Object> s = ps.apply(p);
//									Object r = s.apply((Resource)o);
//									return r;
//								};
//								
//								methodImplMap.put(readMethod, readImpl);
//							}
//						} else if(isSetType) {
//							
//							
//						} else {
//							throw new RuntimeException("Unsupported collection type");
//						}

				} else if(isMapValued) { // Case for maps
					//System.out.println("Map type detected");
					Class<?> keyType = readMethodDescriptor.getKeyType();
					Class<?> valueType = readMethodDescriptor.getValueType();
					
					Function<Property, Function<Resource, Object>> getter =
							viewAsMap(readMethod, isIriType, isViewAll, keyType, valueType, typeMapper, typeDecider);
					
					Function<Resource, Object> g = getter.apply(p);
						methodImplMap.put(readMethod, (o, args) -> g.apply((Resource)o)); 
					
				} else { // Case for scalar values / non-collections
					
					if(effectiveType == null) {
						throw new RuntimeException("Incompatible types on getter / setter for property '" + beanPropertyName + "'");
					}
					
					// Scalar case
					Function<Property, Function<Resource, Object>> getter = viewAsScalarGetter(readMethodDescriptor, effectiveType, isIriType, isViewAll, typeMapper, typeDecider);
					if(getter != null) {
						Function<Resource, Object> g = getter.apply(p);
						methodImplMap.put(readMethod, (o, args) -> g.apply((Resource)o)); 
					}
					
					if(writeMethod != null) {
						
						// Non collection valued case
						// We need to find out whether the property type is
						// directly RDFNode based or whether we need to map a Java type
						Function<Property, BiConsumer<Resource, Object>> setter = viewAsScalarSetter(writeMethodDescriptor, effectiveType, isIriType, isViewAll, typeMapper, typeDecider);
						
						
						if(setter != null) {
							BiConsumer<Resource, Object> s = setter.apply(p);
							
							// Detect fluent API style methods - i.e.
							// methods that return the class it is defined in or one of its super types.
							//
							boolean returnThis = writeMethodDescriptor.isFluentCompatible();//effectiveItemType.isAssignableFrom(clazz);
							
							methodImplMap.put(writeMethod, (o, args) -> {
								s.accept((Resource)o, args[0]);
								
								Object r = returnThis ? o: null;
								
								return r;
							});
						}
					}
							// In the case the collection type is merely iterable,
							// default to Set
							
				}
			}
			
		}
		
			
//			if(writeMethod != null) {
//				Function<Property, BiConsumer<Resource, Object>> setter = viewAsSetter(writeMethod, typeMapper);
//				
//				if(setter != null) {
//					BiConsumer<Resource, Object> s = setter.apply(p);
//					methodImplMap.put(writeMethod, (o, args) -> {
//						s.accept((Resource)o, args[0]);
//						
//						// Detect fluent API style methods - i.e.
//						// methods that return the class it is defined in or one of its super types.
//						Object r = method.getReturnType().isAssignableFrom(clazz)
//							? o
//							: null;
//						
//						return r;
//					});
//				}
//
//			}
//
//			
//			
//			// System.out.println("Method " + method);
////			P_Path0 path = Optional.ofNullable(derivePathFromMethod(method, pm))
////					.orElseGet(() -> paths.get(beanPropertyName));
//
//			
//		}
//		
//			
//			
//
//			if(path != null) {
////				if(path != null && path.toString().contains("style")) {
////					System.out.println("style here");
////				}
//
//
//				Function<Class<?>, Function<Property, Function<Resource, Object>>> collectionViewer = viewAsCollectionViewer(method, typeMapper);
//				if(collectionViewer != null) {
//					methodImplMap.put(method, (o, args) -> {
//						Class<?> clz = Objects.requireNonNull((Class<?>)args[0]);
//						Function<Property, Function<Resource, Object>> ps = collectionViewer.apply(clz);
//						Function<Resource, Object> s = ps.apply(p);
//						Object r = s.apply((Resource)o);
//						return r;
//					});
//				} else {
//
//
//					Function<Property, Function<Resource, Object>> getter = viewAsGetter(method, typeMapper);				
//					if(getter != null) {
//						Function<Resource, Object> g = getter.apply(p);
//						methodImplMap.put(method, (o, args) -> g.apply((Resource)o)); 
//					} else {
//						phaseTwoMethods.add(method);
//					}
//				}
//			}
//		}
		
//
//			// Check whether a method is a collection setter
//			P_Path0 path = Optional.ofNullable(derivePathFromMethod(method, pm))
//					.orElseGet(() -> paths.get(deriveBeanPropertyName(method.getName())));
//
//			if(path != null) {
////				if(path != null && path.toString().contains("style")) {
////					System.out.println("style here");
////				}
//
//				Property p = ResourceFactory.createProperty(path.getNode().getURI());
//
//				
//				
//				//MethodDescriptor d = methodClassifications.get(method);
//				
//				
//				
//				Function<Property, BiConsumer<Resource, Object>> setter = viewAsSetter(method, typeMapper);
//				
//				if(setter != null) {
//					BiConsumer<Resource, Object> s = setter.apply(p);
//					methodImplMap.put(method, (o, args) -> {
//						s.accept((Resource)o, args[0]);
//						
//						// Detect fluent API style methods - i.e.
//						// methods that return the class it is defined in or one of its super types.
//						Object r = method.getReturnType().isAssignableFrom(clazz)
//							? o
//							: null;
//						
//						return r;
//					});
//				}
//			}
//		}
		
		BiFunction<Node, EnhGraph, T> result;
		boolean useCgLib = true;
		
		
		if(useCgLib) {
//			new ByteBuddy()
//			.subclass(ResourceImpl.class)
//			.implement(clazz)
//			.method(ElementMatchers.any())
//				.intercept(InvocationHandlerAdapter.of((proxy, method, args) -> {
//					return null;
//				}))
//			.make()
//			.load(clazz.getClassLoader());

			Enhancer enhancer = new Enhancer();
			if(clazz.isInterface()) {
				enhancer.setSuperclass(ResourceProxyBase.class);
				enhancer.setInterfaces(new Class<?>[] { clazz });
			} else {
				if(!Resource.class.isAssignableFrom(clazz)) {
					throw new RuntimeException("Failed to use " + clazz + " as a resource view because but it does not extend  " + Resource.class);
				}
				
				// TODO Check for a (Node, EnhGraph) ctor
				
				enhancer.setSuperclass(clazz);
			}
			enhancer.setCallback(new MethodInterceptor() {				
			    public Object intercept(Object obj, java.lang.reflect.Method method, Object[] args,
                        MethodProxy proxy) throws Throwable {
//			    	try {
//			    		Method m = obj.getClass().getMethod(method.getName(), method.getParameterTypes());
//			    		System.out.println("Found Method: " + m + " - " + m.isDefault());
//			    	} catch(Exception e) {
//			    		System.out.println(e);
//			    	}
			    	
				    BiFunction<Object, Object[], Object> delegate = methodImplMap.get(method);
//				    System.out.println(methodMap);
				    Object r;
				    if(delegate != null) {
				    	r = delegate.apply(obj, args);
				    } else if(method.isDefault()) {
				    	throw new RuntimeException("Should never come here anymore");

//				    	BiFunction<Object, Object[], Object> defaultMethodDelegate;
//				    	synchronized (methodImplMap) {
//						    defaultMethodDelegate = proxyDefaultMethod(method);
//			                
//			                methodImplMap.put(method, defaultMethodDelegate);
//						}
//				    	r = defaultMethodDelegate.apply(obj, args);
				    	

			                //r = method.invoke(hack, args);
			                
			                //				    	r = MethodHandles.lookup()
//					    	.in(declaringClass)
//					    	.unreflectSpecial(method, declaringClass)
//					    	.bindTo(proxy)
//					    	.invokeWithArguments(args);
				    	//throw new RuntimeException("Cannot handle default method yet; TODO Implement something from https://stackoverflow.com/questions/22614746/how-do-i-invoke-java-8-default-methods-reflectively");
//				    	declaringClass = method.getDeclaringClass();
//				    	   constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
//
//				    	   constructor.setAccessible(true);
//
//				    	   result = constructor.
//				    	      newInstance(declaringClass, MethodHandles.Lookup.PRIVATE).
//				    	      unreflectSpecial(method, declaringClass).
//				    	      bindTo(proxy).
//				    	      invokeWithArguments(args);
				    } else {
				    	r = proxy.invokeSuper(obj, args);
				    }
				    return r;
				}
			});
			
			result = (n, g) -> {
				Class<?>[] argTypes = new Class<?>[] {Node.class, EnhGraph.class};
				Object[] argValues = new Object[] {n, g};
				Object o;
				
				// Synchronization due to ISSUE #30 - Race condition in mapper-proxy
				// Also see test case {@link TestMapperProxyRaceCondiditon}
				synchronized(MapperProxyUtils.class) {
					o = enhancer.create(argTypes, argValues);
				}
				return (T)o;
			};
		}		
		else {
			// This approach using only native java reflection does not work, because internally jena
			// performs a class cast to the abstract class EnhNode
			// Hence, we use cglib
			result = (n, g) -> {
				ResourceImpl base = new ResourceImpl(n, g);
				
				Class<?> baseClass = base.getClass();
				
				@SuppressWarnings("unchecked")
		        T obj = (T)Proxy.newProxyInstance(
		            clazz.getClassLoader(),
		            new Class[] { clazz },
		            (o, m, args) -> {
		            	args = args == null ? new Object[0] : args;
	
		            	Class<?>[] argTypes = new Class<?>[args.length];
		            	for(int i = 0; i < args.length; ++i) {
		            		argTypes[i] = Optional.ofNullable(args[i]).map(Object::getClass).orElse(null);
		            	}
		            	
		            	String methodName = m.getName();
		            	Method baseMethod = null;
		            	try {
		            		baseMethod = baseClass.getMethod(methodName, argTypes);
		            	} catch(NoSuchMethodException e) {
		            		// Silently ignore
		            	}
		            	
		            	Object r;
		            	if(baseMethod != null) {
		            		r = baseMethod.invoke(base, args);
		            	} else {
		            	
			            	BiFunction<Object, Object[], Object> delegate = methodImplMap.get(m);
	//	System.out.println("Method map: " + methodMap);
			            	
			            	if(delegate != null) {
			            		// NOTE: We pass the base resource here!
			            		r = delegate.apply(base, args);
			            	} else {
			            		throw new UnsupportedOperationException();
			            	}
		            	}	            	
		            	return r;
		            }
				);
				return obj;
			};
		}

		return result;
	}
	
	public static BiFunction<Object, Object[], Object> proxyDefaultMethod(Method method)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, InstantiationException, IllegalArgumentException, InvocationTargetException {
		BiFunction<Object, Object[], Object> defaultMethodDelegate;
		Class<?> declaringClass = method.getDeclaringClass();
		Constructor<Lookup> constructor =
			Lookup.class.getDeclaredConstructor(Class.class);
		constructor.setAccessible(true);

		MethodHandle undboundHandle = constructor.newInstance(declaringClass)
				.in(declaringClass)
				.unreflectSpecial(method, declaringClass);
		
		defaultMethodDelegate = (o, a) -> {
		    MethodHandle boundHandle = undboundHandle.bindTo(o);
		    Object r;
			try {
				r = boundHandle.invokeWithArguments(a);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		    return r;
		};
		return defaultMethodDelegate;
	}
}
