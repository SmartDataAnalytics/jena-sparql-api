package org.aksw.jena_sparql_api.mapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Define the key property to use on entries of an RDF-based map.
 *
 * {@code
 * @KeyIri("urn:myKey")
 * Map<String, Resource> getMap();
 * }
 *
 * @author raven
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface KeyIri {
    String value();
}
