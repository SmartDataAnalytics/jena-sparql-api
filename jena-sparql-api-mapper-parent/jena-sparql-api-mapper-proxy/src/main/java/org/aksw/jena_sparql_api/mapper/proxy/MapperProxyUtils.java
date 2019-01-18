package org.aksw.jena_sparql_api.mapper.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
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

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;
import org.aksw.jena_sparql_api.utils.model.NodeMapper;
import org.aksw.jena_sparql_api.utils.model.NodeMapperFactory;
import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.aksw.jena_sparql_api.utils.model.SetFromLiteralPropertyValues;
import org.aksw.jena_sparql_api.utils.model.SetFromMappedPropertyValues;
import org.aksw.jena_sparql_api.utils.model.SetFromPropertyValues;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
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

import com.google.common.collect.ClassToInstanceMap;

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
	boolean isGetter();
	boolean isSetter();
	boolean isCollectionValued();
	
	boolean isDynamicCollection();
	
	/**
	 * Only applicable to setters - is the method's return type
	 * assignable from the method's declaring class?
	 *  
	 * @return
	 */
	boolean isFluentCompatible();
	
	public static MethodDescriptor simpleGetter(Class<?> type) {
		return null;
	}

	public static MethodDescriptor simpleSetter(Class<?> type, boolean fluentCapable) {
		return null;		
	}
	
	public static MethodDescriptor collectionGetter(Class<?> collectionType, Class<?> itemType) {
		return new MethodDescriptorCollectionGetter(collectionType, itemType);
	}

	public static MethodDescriptor dynamicCollectionGetter(Class<?> collectionType, Class<?> boundedItemType) {
		return null;		
	}

	public static MethodDescriptor collectionSetter(Class<?> collectionType, Class<?> itemType) {
		return null;
		
	}
}

class MethodDescriptorCollectionGetter
	implements MethodDescriptor
{
	protected Class<?> collectionType;
	protected Class<?> itemType;
	
	public MethodDescriptorCollectionGetter(Class<?> collectionType, Class<?> itemType) {
		super();
		this.collectionType = collectionType;
		this.itemType = itemType;
	}

	@Override public boolean isGetter() { return true; }
	@Override public boolean isSetter() { return false; }
	@Override public boolean isCollectionValued() { return true; }
	@Override public boolean isDynamicCollection() { return false; }
	@Override public boolean isFluentCompatible() { return false; } //throw new RuntimeException("not applicable"); }
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
                result = (Class<?>)types[0];
            }

        }

        return result;
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
	public static Function<Class<?>, Function<Property, Function<Resource, Object>>> viewAsCollectionViewer(Method m, TypeMapper typeMapper) {		
		Function<Class<?>, Function<Property, Function<Resource, Object>>> result = null;

		Class<?> baseItemType = canActAsCollectionView(m, Set.class, null);


		if(baseItemType != null) {
			result = clazz -> viewAsCollectionViewer(m, typeMapper, clazz);
		}
		
		return result;
	}

    
	public static Function<Property, Function<Resource, Object>> viewAsCollectionViewer(Method m, TypeMapper typeMapper, Class<?> itemType) {
		Function<Property, Function<Resource, Object>> result = null;

		boolean isIriType = m.getAnnotation(IriType.class) != null;
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
	
	
	public static MethodDescriptor classifyMethod(Method m) {
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
					result = MethodDescriptor.collectionGetter(returnType, itemType);
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
	public static Function<Property, Function<Resource, Object>> viewAsGetter(Method m, TypeMapper typeMapper) {
		//MethodInfo result;
		//PropertyDescriptor
		//TypeResolver.
		
		Class<?> returnType = m.getReturnType();
		
		Function<Property, Function<Resource, Object>> result = null;		
		int paramCount = m.getParameterCount();

		//boolean isIterableReturnType = false;
		// Class<?> itemType = null;
		
		
		
		if(paramCount == 0) {
			// Deal with (non-nested) collections first
			if(Iterable.class.isAssignableFrom(returnType)) {
				Class<?> itemType = extractItemType(m.getGenericReturnType());
				if(itemType != null) {
					result = viewAsCollectionViewer(m, typeMapper, itemType);
				}				
			} else if(RDFNode.class.isAssignableFrom(returnType)) {
				@SuppressWarnings("unchecked")
				Class<? extends RDFNode> rdfType = (Class<? extends RDFNode>)returnType;
				result = p -> s -> ResourceUtils.getPropertyValue(s, p, rdfType);
			} else {
				boolean isIriType = m.getAnnotation(IriType.class) != null;
				if(isIriType) {
					if(!String.class.isAssignableFrom(returnType)) {
						// TODO Change to warning
						throw new RuntimeException("@IriType annotation requires String type");
					}
					
					result = p -> s -> ResourceUtils.getPropertyValue(s, p, NodeMapperFactory.uriString);					
				} else {
					RDFDatatype dtype = typeMapper.getTypeByClass(returnType);
	
					if(dtype != null) {
						result = p -> s -> ResourceUtils.getLiteralPropertyValue(s, p, returnType);
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
	public static boolean matchesCollectionViewGetter(Method m, Class<?> expectedReturnType, Class<?> expectedArgType) {
		boolean result = false;

		Class<?> actualReturnType = m.getReturnType();
		
		
		if(actualReturnType.isAssignableFrom(expectedReturnType)) {
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
	public static Class<?> canActAsCollectionView(Method m, Class<?> iterableClass, Class<?> typeVariableBound) {
		// Check if there is exactly one type variable
		Class<?> result = null;
		boolean isRawMatch = matchesCollectionViewGetter(m, iterableClass, Class.class);

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
	public static Function<Property, BiConsumer<Resource, Object>> viewAsSetter(Method m, TypeMapper typeMapper) {
		// Strict setters return void, but e.g. in the case of fluent APIs return types may vary 

		Function<Property, BiConsumer<Resource, Object>> result = null;
		
		
		Class<?>[] paramTypes = m.getParameterTypes();
		if(paramTypes.length == 1) {
			Class<?> paramType = paramTypes[0];
			if(RDFNode.class.isAssignableFrom(paramType)) {
//				@SuppressWarnings("unchecked")
//				Class<? extends RDFNode> rdfType = (Class<? extends RDFNode>)paramType;
				result = p -> (s, o) -> ResourceUtils.setProperty(s, p, (RDFNode)o);
			} else {
				boolean isIriType = m.getAnnotation(IriType.class) != null;
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
		}
		
		return result;
	}
	
	public static String deriveBeanPropertyName(String methodName) {
		// TODO Check whether the subsequent character is upper case
		boolean isGetterOrSetter = methodName.startsWith("get") || methodName.startsWith("set") || methodName.startsWith("is");
		String result = isGetterOrSetter ? methodName.substring(3) : methodName;
		
		// TODO We may want to use the Introspector's public decapitalize method
		//result = Introspector.decapitalize(result);
		result = StringUtils.uncapitalize(result);
		
		return result;
	}
	
	public static P_Path0 derivePathFromMethod(Method method, PrefixMapping pm) {
		P_Path0 result = null;
		
		Iri iri = method.getAnnotation(Iri.class);
		IriNs iriNs = method.getAnnotation(IriNs.class);
		if(iri != null) {
			String rdfPropertyStr = iri.value();
			
			String prefix = pm.getNsURIPrefix(rdfPropertyStr);
			// If there is no prefix, we assume an uri
			String pathStr = prefix == null ? "<" + rdfPropertyStr + ">" : rdfPropertyStr;
			
			// Expand against default namespaces
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
	
	public static <T extends Resource> BiFunction<Node, EnhGraph, T> createProxyFactory(Class<T> clazz, PrefixMapping pm) {
		// Search for methods with @Iri annotation
		// getter pattern: any x()

		TypeMapper typeMapper = TypeMapper.getInstance();
		Map<String, P_Path0> beanPropertyNameToPath = indexPathsByBeanPropertyName(clazz, pm);

		Map<Method, MethodDescriptor> methodClassifications = new HashMap<>();		
		Map<Method, BiFunction<Object, Object[], Object>> methodMap = new LinkedHashMap<>();
		
		
		// Postponed methods will be processed in a subsequent phase
		// - collection setters need access to a collection getter
		List<Method> phaseTwoMethods = new ArrayList<>();
		
		for(Method method : clazz.getMethods()) {
			MethodDescriptor descriptor = classifyMethod(method);
			
			methodClassifications.put(method, descriptor);
			
			
			// System.out.println("Method " + method);
			P_Path0 path = Optional.ofNullable(derivePathFromMethod(method, pm))
					.orElseGet(() -> beanPropertyNameToPath.get(deriveBeanPropertyName(method.getName())));

			if(path != null) {
//				if(path != null && path.toString().contains("style")) {
//					System.out.println("style here");
//				}

				Property p = ResourceFactory.createProperty(path.getNode().getURI());

				Function<Class<?>, Function<Property, Function<Resource, Object>>> collectionViewer = viewAsCollectionViewer(method, typeMapper);
				if(collectionViewer != null) {
					methodMap.put(method, (o, args) -> {
						Class<?> clz = Objects.requireNonNull((Class<?>)args[0]);
						Function<Property, Function<Resource, Object>> ps = collectionViewer.apply(clz);
						Function<Resource, Object> s = ps.apply(p);
						Object r = s.apply((Resource)o);
						return r;
					});
				} else {


					Function<Property, Function<Resource, Object>> getter = viewAsGetter(method, typeMapper);				
					if(getter != null) {
						Function<Resource, Object> g = getter.apply(p);
						methodMap.put(method, (o, args) -> g.apply((Resource)o)); 
					} else {
						phaseTwoMethods.add(method);
					}
				}
			}
		}
		
		for(Method method : phaseTwoMethods) {

			// Check whether a method is a collection setter
			P_Path0 path = Optional.ofNullable(derivePathFromMethod(method, pm))
					.orElseGet(() -> beanPropertyNameToPath.get(deriveBeanPropertyName(method.getName())));

			if(path != null) {
//				if(path != null && path.toString().contains("style")) {
//					System.out.println("style here");
//				}

				Property p = ResourceFactory.createProperty(path.getNode().getURI());

				
				
				//MethodDescriptor d = methodClassifications.get(method);
				
				
				
				Function<Property, BiConsumer<Resource, Object>> setter = viewAsSetter(method, typeMapper);
				
				if(setter != null) {
					BiConsumer<Resource, Object> s = setter.apply(p);
					methodMap.put(method, (o, args) -> {
						s.accept((Resource)o, args[0]);
						
						// Detect fluent API style methods - i.e.
						// methods that return the class it is defined in or one of its super types.
						Object r = method.getReturnType().isAssignableFrom(clazz)
							? o
							: null;
						
						return r;
					});
				}
			}
		}
		
		BiFunction<Node, EnhGraph, T> result;
		boolean useCgLib = true;
		if(useCgLib) {
			Enhancer enhancer = new Enhancer();
			enhancer.setSuperclass(ResourceImpl.class);
			enhancer.setInterfaces(new Class<?>[] { clazz });
			enhancer.setCallback(new MethodInterceptor() {				
			    public Object intercept(Object obj, java.lang.reflect.Method method, Object[] args,
                        MethodProxy proxy) throws Throwable {
			    	
				    BiFunction<Object, Object[], Object> delegate = methodMap.get(method);
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
		            	
			            	BiFunction<Object, Object[], Object> delegate = methodMap.get(m);
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
