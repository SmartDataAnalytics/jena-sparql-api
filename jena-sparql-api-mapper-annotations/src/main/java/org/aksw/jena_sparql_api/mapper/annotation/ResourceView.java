package org.aksw.jena_sparql_api.mapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Make a Jena Resource view interface or class discoverable by classpath scanning 
 *
 * @author raven
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ResourceView {
	
	/**
	 * A set of super-classes or interfaces of X of the class carrying the ResourceView annotation.
	 * The implementation generated from the annotated class will be associated with each
	 * type in X.
	 * 
	 * Example:
	 * interface A extends Resource { }
	 * @ResourceView(A.class) interface B extends A { } 
	 * 
	 * This will generate a proxy factory for B.class and associate it with A.
	 * As a consequence,
	 * 
	 * ModelFactory.createDefaultModel().createResource().as(A.class) will yield a proxy generated from B.
	 * 
	 * @return
	 */
	Class<?>[] value() default {};
}
