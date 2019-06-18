package org.aksw.jena_sparql_api.mapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation on how to generate IDs for the annotated class.
 * Spring SPEL expressions can be used to create IRIs from instance's state.
 *
 * If used on properties (i.e. field or method), this iri will be used
 * if the attribute is null
 *
 * @author raven
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface DefaultIri {
    String value();
}
