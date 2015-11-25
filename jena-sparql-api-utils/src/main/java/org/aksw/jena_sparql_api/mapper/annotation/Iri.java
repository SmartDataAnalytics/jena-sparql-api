package org.aksw.jena_sparql_api.mapper.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation has different semantics depending on where it appears.
 * The following rules are taken from Alibaba for compatibility reasons:
 * <ul>
 *   <li>Class level: the RDF type</li>
 *   <li>Attribute: the RDF property</li>
 * </ul>
 *
 * If the value is an empty string, the annotated property's name
 * will be treated as a relative IRI.
 *
 *
 * @author raven
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Iri {
    String value() default "";
}
