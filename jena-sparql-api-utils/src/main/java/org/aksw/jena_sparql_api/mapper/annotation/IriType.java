package org.aksw.jena_sparql_api.mapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a property value should be interpreted as an RDF term of type IRI.
 * Applies toString() on the value.
 *
 *
 * @author raven
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface IriType {
}
