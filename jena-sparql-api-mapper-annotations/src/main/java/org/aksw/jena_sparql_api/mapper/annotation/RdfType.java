package org.aksw.jena_sparql_api.mapper.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Generate an RDF type triple
 *
 * @author raven
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RdfType {
    String value() default "";
}
