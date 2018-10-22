package org.aksw.jena_sparql_api.mapper.proxy;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;
import org.aksw.jena_sparql_api.utils.model.NodeMapperFactory;
import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.aksw.jena_sparql_api.utils.model.SetFromLiteralPropertyValues;
import org.aksw.jena_sparql_api.utils.model.SetFromMappedPropertyValues;
import org.aksw.jena_sparql_api.utils.model.SetFromPropertyValues;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.PathParser;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.Proxy;


public class MapperProxyUtils {

	
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
	 * If the method qualifies as a getter, returns a factory function
	 * that for a given property yields another function that accesses this property for a 
	 * 
	 * @param m
	 * @param typeMapper
	 * @return
	 */
	public static Function<Property, Function<Resource, Object>> viewAsGetter(Method m, TypeMapper typeMapper) {
		Class<?> returnType = m.getReturnType();
		
		Function<Property, Function<Resource, Object>> result = null;

		if(m.getParameterCount() == 0) {
			// Deal with (non-nested) collections first
			if(Iterable.class.isAssignableFrom(returnType)) {
				Class<?> itemType = extractItemType(m.getGenericReturnType());
				
				if(itemType != null) {
					boolean isIriType = m.getAnnotation(IriType.class) != null;
					if(String.class.isAssignableFrom(itemType) && isIriType) {
						result = p -> s -> new SetFromMappedPropertyValues<>(s, p, NodeMapperFactory.uriString);						
					} else if(RDFNode.class.isAssignableFrom(itemType)) {
						@SuppressWarnings("unchecked")
						Class<? extends RDFNode> rdfType = (Class<? extends RDFNode>)itemType;
						result = p -> s -> new SetFromPropertyValues<>(s, p, rdfType);						
					} else {
						RDFDatatype dtype = typeMapper.getTypeByClass(returnType);
						
						if(dtype != null) {
							result = p -> s -> new SetFromLiteralPropertyValues<>(s, p, returnType);
						}
					}
				
				}
				
			} else if(RDFNode.class.isAssignableFrom(returnType)) {
				@SuppressWarnings("unchecked")
				Class<? extends RDFNode> rdfType = (Class<? extends RDFNode>)returnType;
				result = p -> s -> ResourceUtils.getPropertyValue(s, p, rdfType);
			} else {
				RDFDatatype dtype = typeMapper.getTypeByClass(returnType);
				
				if(dtype != null) {
					result = p -> s -> ResourceUtils.getLiteralPropertyValue(s, p, returnType);
				}
			}
		}
		
		return result;
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
				RDFDatatype dtype = typeMapper.getTypeByClass(paramType);
				
				if(dtype != null) {
					result = p -> (s, o) -> ResourceUtils.updateLiteralProperty(s, p, (Class)paramType, o);
				}
			}
		}
		
		return result;
	}
	
	public static String deriveBeanPropertyName(String methodName) {
		boolean isGetterOrSetter = methodName.startsWith("get") || methodName.startsWith("set");
		String result = isGetterOrSetter ? methodName.substring(3) : methodName;
		return result;
	}
	
	public static P_Path0 derivePathFromMethod(Method method) {
		P_Path0 result = null;
		
		Iri iri = method.getAnnotation(Iri.class);
		if(iri != null) {
			String rdfPropertyStr = iri.value();
			// Expand against default namespaces
			result = (P_Path0)PathParser.parse(rdfPropertyStr, PrefixMapping.Extended);
			
			//Node p = NodeFactory.createURI(rdfPropertyStr);
			
			//result = new P_Link(p);
		}
		
		return result;
	}

	public static Map<String, P_Path0> indexPathsByBeanPropertyName(Class<?> clazz) {
		Map<String, P_Path0> result = new LinkedHashMap<>();
		for(Method method : clazz.getMethods()) {
			String methodName = method.getName();
			String beanPropertyName = deriveBeanPropertyName(methodName);
			P_Path0 path = derivePathFromMethod(method);
			
			if(path != null) {
				result.put(beanPropertyName, path);
			}
		}
		
		return result;
	}
	
	public static <T extends Resource> BiFunction<Node, EnhGraph, T> createProxyFactory(Class<T> clazz) {
		// Search for methods with @Iri annotation
		// getter pattern: any x()

		TypeMapper typeMapper = TypeMapper.getInstance();
		Map<String, P_Path0> beanPropertyNameToPath = indexPathsByBeanPropertyName(clazz);
		
		Map<Method, BiFunction<Object, Object[], Object>> methodMap = new LinkedHashMap<>();
		
		for(Method method : clazz.getMethods()) {
			// System.out.println("Method " + method);
			P_Path0 path = Optional.ofNullable(derivePathFromMethod(method))
					.orElseGet(() -> beanPropertyNameToPath.get(deriveBeanPropertyName(method.getName())));

			if(path != null) {
				Property p = ResourceFactory.createProperty(path.getNode().getURI());

				Function<Property, Function<Resource, Object>> getter = viewAsGetter(method, typeMapper);				
				if(getter != null) {
					Function<Resource, Object> g = getter.apply(p);
					methodMap.put(method, (o, args) -> g.apply((Resource)o)); 
				} else {
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
