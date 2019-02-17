package org.aksw.jena_sparql_api.mapper.proxy;

import java.beans.Introspector;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.aksw.commons.accessors.ListFromConverter;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;
import org.aksw.jena_sparql_api.utils.model.ConverterFromNodeMapper;
import org.aksw.jena_sparql_api.utils.model.ConverterFromNodeMapperAndModel;
import org.aksw.jena_sparql_api.utils.model.ListFromRDFList;
import org.aksw.jena_sparql_api.utils.model.NodeMapper;
import org.aksw.jena_sparql_api.utils.model.NodeMapperFactory;
import org.aksw.jena_sparql_api.utils.model.NodeMapperRdfDatatype;
import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.aksw.jena_sparql_api.utils.model.SetFromLiteralPropertyValues;
import org.aksw.jena_sparql_api.utils.model.SetFromMappedPropertyValues;
import org.aksw.jena_sparql_api.utils.model.SetFromPropertyValues;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.enhanced.EnhGraph;
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

import com.google.common.base.Defaults;
import com.google.common.collect.Lists;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.Proxy;



/*
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

interface MethodDescriptor {
	Method getMethod();
	
	boolean isGetter();
	default boolean isSetter() { return !isGetter(); }
	boolean isCollectionValued();
	
	boolean isDynamicCollection();
	
	Class<?> getType();

	Class<?> getItemType();
	Class<?> getCollectionType();
	
	
	/**
	 * Only applicable to setters - is the method's return type
	 * assignable from the method's declaring class?
	 *  
	 * @return
	 */
	boolean isFluentCompatible();
	
	public static MethodDescriptor simpleGetter(Method method, Class<?> type) {
		return new MethodDescriptorSimple(method, true, false, type);
	}

	public static MethodDescriptor simpleSetter(Method method, boolean fluentCapable, Class<?> type) {
		return new MethodDescriptorSimple(method, false, fluentCapable, type);
	}
	
	public static MethodDescriptor collectionGetter(Method method, Class<?> collectionType, Class<?> itemType) {
		return new MethodDescriptorCollection(method, true, false, collectionType, itemType, false);
	}

	public static MethodDescriptor collectionSetter(Method method, boolean fluentCapable, Class<?> collectionType, Class<?> itemType) {
		return new MethodDescriptorCollection(method, false, fluentCapable, collectionType, itemType, false);
	}

	public static MethodDescriptor dynamicCollectionGetter(Method method, Class<?> collectionType, Class<?> boundedItemType) {
		return new MethodDescriptorCollection(method, true, false, collectionType, boundedItemType, true);
	}
}

abstract class MethodDescriptorBase
	implements MethodDescriptor
{
	protected Method method; 
	protected boolean isGetter;
	protected boolean isFluentCompatible;
	
	public MethodDescriptorBase(Method method, boolean isGetter, boolean isFluentCompatible) {
		this.method = method;
		this.isGetter = isGetter;
		this.isFluentCompatible = isFluentCompatible;
	}
	
	@Override public Method getMethod() { return method; }
	@Override public boolean isGetter() { return isGetter; }
	@Override public boolean isFluentCompatible() { return isFluentCompatible; } //throw new RuntimeException("not applicable"); }
}

class MethodDescriptorCollection
	extends MethodDescriptorBase
{
	protected Class<?> collectionType;
	protected Class<?> itemType;
	protected boolean isDynamic;
	
	public MethodDescriptorCollection(Method method, boolean isGetter, boolean isFluentCompatible, Class<?> collectionType, Class<?> itemType, boolean isDynamic) {
		super(method, isGetter, isFluentCompatible);
		this.collectionType = collectionType;
		this.itemType = itemType;
		this.isDynamic = isDynamic;
	}

//	@Override public boolean isSetter() { return false; }
	@Override public boolean isCollectionValued() { return true; }
	@Override public boolean isDynamicCollection() { return isDynamic; }
	
	@Override public Class<?> getType() { return null; }	
	@Override public Class<?> getCollectionType() { return collectionType; }
	@Override public Class<?> getItemType() { return itemType; }	

}

class MethodDescriptorSimple
	extends MethodDescriptorBase
{
	protected Class<?> type;

	public MethodDescriptorSimple(Method method, boolean isGetter, boolean isFluentCompatible, Class<?> type) {
		super(method, isGetter, isFluentCompatible);
		this.type = type;
		this.isFluentCompatible = isFluentCompatible;
	}

	@Override public boolean isCollectionValued() { return false; }
	@Override public boolean isDynamicCollection() { return false; }
	@Override public Class<?> getType() { return type; }	
	@Override public Class<?> getCollectionType() { return null; }
	@Override public Class<?> getItemType() { return null; }	
}


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
	
    public static Class<?> extractItemType(Type genericType) {
        Class<?> result = null;
        if(genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType)genericType;
            java.lang.reflect.Type[] types = pt.getActualTypeArguments();
            if(types.length == 1) {
            	Type argType = types[0];
            	if(argType instanceof Class) {
            		result = (Class<?>)argType;
            	} else if(argType instanceof WildcardType) {
            		// TODO We should take bounds into accont
            		result = Object.class;
            	} else {
            		throw new RuntimeException("Don't know how to handle " + argType);
            	}
            }

        }

        return result;
    }
   
	public static Function<Class<?>, Function<Property, Function<Resource, Object>>>
		viewAsDynamicList(Method m, boolean isIriType, TypeMapper typeMapper)
	{
//		Function<Class<?>, Function<Property, Function<Resource, Object>>> result = null;
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
		return null;
	}
    
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
		viewAsDynamicSet(Method m, boolean isIriType, TypeMapper typeMapper)
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
			result = clazz -> viewAsSet(m, isIriType, typeMapper, clazz);
		}
		
		return result;
	}

    
	public static Function<Property, Function<Resource, Object>> viewAsSet(Method m, boolean isIriType, TypeMapper typeMapper, Class<?> itemType) {
		Function<Property, Function<Resource, Object>> result = null;

//		boolean isIriType = m.getAnnotation(IriType.class) != null;
		if(String.class.isAssignableFrom(itemType) && isIriType) {
			result = p -> s -> new SetFromMappedPropertyValues<>(s, p, NodeMapperFactory.uriString);						
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
		viewAsList(Method m, boolean isIriType, TypeMapper typeMapper, Class<?> itemType)
	{
		Function<Property, Function<Resource, Object>> result = null;
	
	//	boolean isIriType = m.getAnnotation(IriType.class) != null;
		if(String.class.isAssignableFrom(itemType) && isIriType) {
			result = p -> s ->
				new ListFromConverter<String, RDFNode>(
						new ListFromRDFList(s, p),
						new ConverterFromNodeMapperAndModel<>(s.getModel(), RDFNode.class, new ConverterFromNodeMapper<>(NodeMapperFactory.uriString)).reverse());						
		} else if(RDFNode.class.isAssignableFrom(itemType)) {
			@SuppressWarnings("unchecked")
			Class<? extends RDFNode> rdfType = (Class<? extends RDFNode>)itemType;
			result = p -> s -> new ListFromRDFList(s, p);//new SetFromPropertyValues<>(s, p, rdfType);						
		} else {
			RDFDatatype dtype = typeMapper.getTypeByClass(itemType);
			
			if(dtype != null) {
				result = p -> s -> new ListFromConverter<>(new ListFromRDFList(s, p), new ConverterFromNodeMapperAndModel<RDFNode, Object>(s.getModel(), RDFNode.class, new ConverterFromNodeMapper<>(new NodeMapperRdfDatatype<Object>(dtype))).reverse());	 
				
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
				boolean isFluentCompatible = clazz.isAssignableFrom(returnType);
				
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
				boolean isFluentCompatible = clazz.isAssignableFrom(returnType);
				
				result = MethodDescriptor.collectionSetter(m, isFluentCompatible, paramType, itemType);
			}
		}
		
		return result;
	}

	
	
	
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
	

	/**
	 * If the method qualifies as a getter, returns a factory function
	 * that for a given property yields another function that accesses this property for a 
	 * 
	 * @param m
	 * @param typeMapper
	 * @return
	 */
	public static Function<Property, Function<Resource, Object>> viewAsScalarGetter(
			MethodDescriptor methodDescriptor,
			Class<?> effectiveType,
			boolean isIriType,
			TypeMapper typeMapper) {
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
					
					result = p -> s -> ResourceUtils.getPropertyValue(s, p, NodeMapperFactory.uriString);					
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
	 * <T extends SomeSubClassOfResource> IterableBaseClass[T] method(Class[T])
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
			TypeMapper typeMapper) {
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
				result = p -> (s, o) -> ResourceUtils.updateProperty(s, p, (NodeMapper)NodeMapperFactory.uriString, o);					
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
		boolean isGetterOrSetter = methodName.startsWith("get") || methodName.startsWith("set") || methodName.startsWith("is");
		String result = isGetterOrSetter ? methodName.substring(3) : methodName;
		
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
			
			logger.debug("Parsed path " + pathStr + " into " + result);
			
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

	public static <T extends Resource> BiFunction<Node, EnhGraph, T> createProxyFactory(Class<T> clazz) {
		return createProxyFactory(clazz, PrefixMapping.Extended);
	}
	
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
	public static <T extends Resource> BiFunction<Node, EnhGraph, T> createProxyFactory(Class<T> clazz, PrefixMapping pm) {

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
		
		
		
		for(Method method : clazz.getMethods()) {
			MethodDescriptor descriptor = classifyMethod(method);
			if(descriptor == null) {
				continue;
			}
			
			String beanPropertyName = deriveBeanPropertyName(method.getName());

			// Skip methods not associated with a path
			if(!paths.containsKey(beanPropertyName)) {
				continue;
			}
			
			methodDescriptors.put(method, descriptor);

			if(descriptor.isGetter()) {
				readMethods.put(beanPropertyName, method);
			} else if(descriptor.isSetter()) {
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
			boolean isReadIriType = false;

			Class<?> writeType = null;
			Class<?> writeCollectionType = null;
			Class<?> writeItemType = null;
			boolean isWriteIriType = false;

			if(readMethodDescriptor != null) {
				readType = readMethodDescriptor.getType();
				readCollectionType = readMethodDescriptor.getCollectionType();
				readItemType = readMethodDescriptor.getItemType();
				isReadIriType = readMethod.getAnnotation(IriType.class) != null; 
			}
			
			if(writeMethodDescriptor != null) {
				writeType = writeMethodDescriptor.getType();
				writeCollectionType = writeMethodDescriptor.getCollectionType();
				writeItemType = writeMethodDescriptor.getItemType();
				isWriteIriType = writeMethod.getAnnotation(IriType.class) != null; 
			}

			Class<?> effectiveType = getStricterType(readType, writeType);
			Class<?> effectiveCollectionType = getStricterType(readCollectionType, writeCollectionType);
			Class<?> effectiveItemType = getStricterType(readItemType, writeItemType);
			boolean  isIriType = isReadIriType || isWriteIriType;
			
			Property p = ResourceFactory.createProperty(path.getNode().getURI());
//System.out.println(p);
			// It is only valid to have a read method without a write one
			// if it yields a collection view
			if(readMethod != null) {
				boolean isCollectionValued = readMethodDescriptor.isCollectionValued();
				boolean isDynamicGetter = readMethodDescriptor.isDynamicCollection();

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
						if(isSetType) {
							Function<Class<?>, Function<Property, Function<Resource, Object>>> collectionGetter = viewAsDynamicSet(readMethod, isIriType, typeMapper);
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
						}
						else {
							throw new RuntimeException("todo dynamic collection support implement");
							
						}
							
							
//							throw new RuntimeException("todo dynamic collection support implement");
					} else { // Non-dynamic collection handling
						Function<Property, Function<Resource, Object>>  collectionView;
						if(isListType) {
							collectionView = viewAsList(readMethod, isIriType, typeMapper, effectiveItemType);
						} else if(isSetType) {
							collectionView = viewAsSet(readMethod, isIriType, typeMapper, effectiveItemType);
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

				} else { // Case for scalar values / non-collections
					
					if(effectiveType == null) {
						throw new RuntimeException("Incompatible types on getter / setter for property " + beanPropertyName);
					}
					
					// Scalar case
					Function<Property, Function<Resource, Object>> getter = viewAsScalarGetter(readMethodDescriptor, effectiveType, isIriType, typeMapper);
					if(getter != null) {
						Function<Resource, Object> g = getter.apply(p);
						methodImplMap.put(readMethod, (o, args) -> g.apply((Resource)o)); 
					}
					
					if(writeMethod != null) {
						
						// Non collection valued case
						// We need to find out whether the property type is
						// directly RDFNode based or whether we need to map a Java type
						Function<Property, BiConsumer<Resource, Object>> setter = viewAsScalarSetter(writeMethodDescriptor, effectiveType, isIriType, typeMapper);
						
						
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
			Enhancer enhancer = new Enhancer();
			enhancer.setSuperclass(ResourceImpl.class);
			enhancer.setInterfaces(new Class<?>[] { clazz });
			enhancer.setCallback(new MethodInterceptor() {				
			    public Object intercept(Object obj, java.lang.reflect.Method method, Object[] args,
                        MethodProxy proxy) throws Throwable {
			    	
				    BiFunction<Object, Object[], Object> delegate = methodImplMap.get(method);
//				    System.out.println(methodMap);
				    Object r;
				    if(delegate != null) {
				    	r = delegate.apply(obj, args);
				    } else if(method.isDefault()) {
				    	throw new RuntimeException("Cannot handle default method yet; TODO Implement something from https://stackoverflow.com/questions/22614746/how-do-i-invoke-java-8-default-methods-reflectively");
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
				Object o = enhancer.create(new Class<?>[] {Node.class, EnhGraph.class}, new Object[] {n, g});
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
}
