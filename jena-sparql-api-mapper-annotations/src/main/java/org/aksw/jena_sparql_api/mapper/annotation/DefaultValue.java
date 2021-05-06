package org.aksw.jena_sparql_api.mapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Default value annotation for use in generation of SPARQL functions from
 * Java methods.
 * The provided string must be either a valid lexical value for the arguments datatype
 * corresponding RDF type.
 * 
 * @author raven
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface DefaultValue {
	String value();
}
