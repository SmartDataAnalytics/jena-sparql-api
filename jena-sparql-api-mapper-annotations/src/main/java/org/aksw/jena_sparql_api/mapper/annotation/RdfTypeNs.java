package org.aksw.jena_sparql_api.mapper.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Generate an RDF type triple
 * The class name will be used as the local name
 *
 * @author raven
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RdfTypeNs {
    String value() default "";
}
