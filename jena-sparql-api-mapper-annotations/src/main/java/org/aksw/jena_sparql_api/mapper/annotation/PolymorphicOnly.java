package org.aksw.jena_sparql_api.mapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that marks a property (regardless of scalar or collection)
 * that only resources which are known to be equivalent-or-subclasses of a requested view type
 * should be exposed.
 * 
 * For all other resources, no view is requested
 * 
 * @author raven
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface PolymorphicOnly {
}
