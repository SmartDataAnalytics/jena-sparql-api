package org.aksw.jena_sparql_api.mapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for mapping between collection valued Java properties
 * and multiple instances of an RDF predicate
 *
 * @author raven
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface MultiValued {

}
