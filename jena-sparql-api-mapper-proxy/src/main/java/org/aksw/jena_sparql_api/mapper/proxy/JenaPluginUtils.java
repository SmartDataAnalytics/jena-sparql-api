package org.aksw.jena_sparql_api.mapper.proxy;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.function.BiFunction;

import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.Implementation;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.ext.com.google.common.reflect.ClassPath;
import org.apache.jena.ext.com.google.common.reflect.ClassPath.ClassInfo;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sys.JenaSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JenaPluginUtils {

	private static final Logger logger = LoggerFactory.getLogger(JenaPluginUtils.class);

	
	static {
		JenaSystem.init();
	}

	/**
	 *  If you get an exception on typeDecider such as java.lang.NullPointerException
	 *  ensure to call JenaSystem.init() before calling methods on this class
	 */
	protected static final TypeDeciderImpl typeDecider = new TypeDeciderImpl();


	
	public static TypeDecider getTypeDecider() {
		return typeDecider;
	}
	
	public static void scan(Class<?> prototypeClass) {
		String basePackage = prototypeClass.getPackage().getName();
		scan(basePackage, BuiltinPersonalities.model);
	}

	public static void scan(String basePackage) {
		scan(basePackage, BuiltinPersonalities.model);
	}

	public static void scan(String basePackage, Personality<RDFNode> p) {
		scan(basePackage, p, DefaultPrefixes.prefixes);
	}
	
	public static void scan(String basePackage, Personality<RDFNode> p, PrefixMapping pm) {
		Set<ClassInfo> classInfos;
		try {
			classInfos = ClassPath.from(Thread.currentThread().getContextClassLoader()).getTopLevelClassesRecursive(basePackage);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		for(ClassInfo classInfo : classInfos) {
			Class<?> clazz = classInfo.load();
			
			registerResourceClass(clazz, p, pm);
		}
	}

	public static void registerResourceClasses(Class<?> ... classes) {
		registerResourceClasses(Arrays.asList(classes));
	}
	
	public static void registerResourceClasses(Iterable<Class<?>> classes) {
		for(Class<?> clazz : classes) {
			registerResourceClass(clazz, BuiltinPersonalities.model, DefaultPrefixes.prefixes);
		}		
	}
	
	public static void registerResourceClass(Class<? extends Resource> inter, Class<?> impl) {
		Personality<RDFNode> p = BuiltinPersonalities.model;
		
		if(Resource.class.isAssignableFrom(impl)) {
			boolean supportsProxying = supportsProxying(impl);
			if(supportsProxying) {
				@SuppressWarnings("unchecked")
				Class<? extends Resource> cls = (Class<? extends Resource>)impl;
				p.add(inter, createImplementation(cls, DefaultPrefixes.prefixes));
			}
		}
	}
	
	public static Implementation createImplementation(Class<?> clazz, PrefixMapping pm) {
		@SuppressWarnings("unchecked")
		Class<? extends Resource> cls = (Class<? extends Resource>)clazz;
		
		logger.debug("Registering " + clazz);
		BiFunction<Node, EnhGraph, ? extends Resource> proxyFactory = 
				MapperProxyUtils.createProxyFactory(cls, pm, typeDecider);

		
		typeDecider.registerClasses(clazz);

		BiFunction<Node, EnhGraph, ? extends Resource> proxyFactory2 = (n, m) -> {
			Resource r = new ResourceImpl(n, m);
			typeDecider.writeTypeTriples(r, cls);
			
			return proxyFactory.apply(n, m);
		};
		
		Implementation result = new ProxyImplementation(proxyFactory2);
		return result;
	}
	
	public static void registerResourceClass(Class<?> clazz, Personality<RDFNode> p, PrefixMapping pm) {
		if(Resource.class.isAssignableFrom(clazz)) {
			boolean supportsProxying = supportsProxying(clazz);
			if(supportsProxying) {
				@SuppressWarnings("unchecked")
				Class<? extends Resource> cls = (Class<? extends Resource>)clazz;
				p.add(cls, createImplementation(cls, pm));
			}
		}
	}

	public static boolean supportsProxying(Class<?> clazz) {

		boolean result = false;
		//int mods = clazz.getModifiers();
		//if(Modifier.isInterface(mods) || !Modifier.isAbstract(mods)) {
			// Check if the class is annotated by @ResourceView
			result = clazz.getAnnotationsByType(ResourceView.class).length != 0;
			
			// Check if there ary any @Iri annotations
			result = result || Arrays.asList(clazz.getDeclaredMethods()).stream()
				.anyMatch(m -> m.getAnnotation(Iri.class) != null || m.getAnnotation(IriNs.class) != null);
		//}
		
		return result;
	}
}
