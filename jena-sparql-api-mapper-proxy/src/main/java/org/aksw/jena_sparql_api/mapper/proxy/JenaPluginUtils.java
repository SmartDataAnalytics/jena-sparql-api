package org.aksw.jena_sparql_api.mapper.proxy;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.function.BiFunction;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.ext.com.google.common.reflect.ClassPath;
import org.apache.jena.ext.com.google.common.reflect.ClassPath.ClassInfo;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sys.JenaSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JenaPluginUtils {

	private static final Logger logger = LoggerFactory.getLogger(JenaPluginUtils.class);

	static { JenaSystem.init(); }
	
	public static void registerJenaResourceClassesUsingPackageScan(Class<?> prototypeClass) {
		String basePackage = prototypeClass.getPackage().getName();
		registerJenaResourceClassesUsingPackageScan(basePackage, BuiltinPersonalities.model);
	}

	public static void registerJenaResourceClassesUsingPackageScan(String basePackage) {
		registerJenaResourceClassesUsingPackageScan(basePackage, BuiltinPersonalities.model);
	}

	public static void registerJenaResourceClassesUsingPackageScan(String basePackage, Personality<RDFNode> p) {
		registerJenaResourceClassesUsingPackageScan(basePackage, p, RDFa.prefixes);
	}
	
	public static void registerJenaResourceClassesUsingPackageScan(String basePackage, Personality<RDFNode> p, PrefixMapping pm) {
		Set<ClassInfo> classInfos;
		try {
			classInfos = ClassPath.from(Thread.currentThread().getContextClassLoader()).getTopLevelClassesRecursive(basePackage);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		for(ClassInfo classInfo : classInfos) {
			Class<?> clazz = classInfo.load();
			
			registerJenaResourceClass(clazz, p, pm);
		}
	}

	public static void registerJenaResourceClasses(Class<?> ... classes) {
		for(Class<?> clazz : classes) {
			registerJenaResourceClass(clazz, BuiltinPersonalities.model, RDFa.prefixes);
		}
	}
	
	
	
	public static void registerJenaResourceClass(Class<?> clazz, Personality<RDFNode> p, PrefixMapping pm) {
		if(Resource.class.isAssignableFrom(clazz)) {
			boolean supportsProxying = supportsProxying(clazz);
			if(supportsProxying) {
				@SuppressWarnings("unchecked")
				Class<? extends Resource> cls = (Class<? extends Resource>)clazz;
				
				logger.debug("Registering " + clazz);
				BiFunction<Node, EnhGraph, ? extends Resource> proxyFactory = MapperProxyUtils.createProxyFactory(cls, pm);
				p.add(cls, new ProxyImplementation(proxyFactory));
			}
		}
	}

	public static boolean supportsProxying(Class<?> clazz) {

		boolean result = false;
		int mods = clazz.getModifiers();
		if(Modifier.isInterface(mods) || !Modifier.isAbstract(mods)) {
			// Check if there ary any @Iri annotations
			result = Arrays.asList(clazz.getDeclaredMethods()).stream()
				.anyMatch(m -> m.getAnnotation(Iri.class) != null || m.getAnnotation(IriNs.class) != null);
		}
		
		return result;
	}
}
