package org.aksw.jena_sparql_api.mapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to map a Java property type to a specific RDF datatype.
 * (By default, a Java class's natural RDF datatype is used, e.g. xsd:int for Java int)
 *
 * The rdf mapper engine must have the appropriate conversion functions registered.
 *
 * Example usage:
 *
 * <code>
 * class Company {
 *     @Iri("dbp:foundingYear")
 *     @Datatype("xsd:gYear")
 *     private int foundingYear;
 * }
 * </code>
 *
 *
 * @author raven
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface Datatype {
    String value();
}
