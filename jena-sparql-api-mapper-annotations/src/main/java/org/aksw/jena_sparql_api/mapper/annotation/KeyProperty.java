package org.aksw.jena_sparql_api.mapper.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Define the key property to use for a map
 * 
 * @KeyProperty("http://example.org/key")
 * Map<String, Resource> getMap();
 * 
 * @author raven
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface KeyProperty {

}
