package org.aksw.jena_sparql_api.mapper.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that a property value should be interpreted as an RDF term of type IRI.
 * Applies toString() on the value.
 *
 *
 * @author raven
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface IriType {

}
