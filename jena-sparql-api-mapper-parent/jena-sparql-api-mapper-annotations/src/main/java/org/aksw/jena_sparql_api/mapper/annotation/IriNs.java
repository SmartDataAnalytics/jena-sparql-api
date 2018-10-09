package org.aksw.jena_sparql_api.mapper.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Specify a namespace to which the property name is appended in order to yield the final URL
 * 
 * @IriNs("myontologyns")
 * Object foobar(); // RDF predicate will be composed of myontologyns:foobar
 * 
 * 
 * @author Claus Stadler, Oct 9, 2018
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface IriNs {
    String value() default "";
}
