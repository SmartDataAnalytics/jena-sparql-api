package org.aksw.jena_sparql_api.mapper.proxy;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.ext.com.google.common.reflect.ClassPath;
import org.apache.jena.ext.com.google.common.reflect.ClassPath.ClassInfo;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JenaPluginUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(JenaPluginUtils.class);


	public static void registerJenaResourceClassesUsingPackageScan(String basePackage) {
		registerJenaResourceClassesUsingPackageScan(basePackage, BuiltinPersonalities.model);
	}


	public static void registerJenaResourceClassesUsingPackageScan(String basePackage, Personality<RDFNode> p) {
		Set<ClassInfo> classInfos;
		try {
			classInfos = ClassPath.from(Thread.currentThread().getContextClassLoader()).getTopLevelClassesRecursive(basePackage);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		for(ClassInfo classInfo : classInfos) {
			Class<?> clazz = classInfo.load();
			
			if(Resource.class.isAssignableFrom(clazz) && supportsProxying(clazz)) {
				@SuppressWarnings("unchecked")
				Class<? extends Resource> cls = (Class<? extends Resource>)clazz;
				
				logger.debug("Registering " + clazz);
				p.add(cls, new ProxyImplementation(MapperProxyUtils.createProxyFactory(cls)));
			}
		}
	}

	public static boolean supportsProxying(Class<?> clazz) {

		boolean result = false;
		int mods = clazz.getModifiers();
		if(Modifier.isInterface(mods) || !Modifier.isAbstract(mods)) {
			// Check if there ary any @Iri annotations
			result = Arrays.asList(clazz.getDeclaredMethods()).stream()
				.anyMatch(m -> m.getAnnotation(Iri.class) != null);
		}
		
		return result;
	}
}
