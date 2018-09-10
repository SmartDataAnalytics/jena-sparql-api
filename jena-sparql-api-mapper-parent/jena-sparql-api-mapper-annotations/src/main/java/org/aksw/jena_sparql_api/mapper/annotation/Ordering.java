package org.aksw.jena_sparql_api.mapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to resolve which value
 * to set as a property value if there are multiple
 * candidates on the RDF level.
 *
 * @author raven
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Ordering {
    Class<?> value(); // default NaturalComparator.class;
}
