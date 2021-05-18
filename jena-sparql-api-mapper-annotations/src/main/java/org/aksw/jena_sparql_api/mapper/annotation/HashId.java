package org.aksw.jena_sparql_api.mapper.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This is just an idea, the MapperProxyUtils annotation processor does not implement this annotation.
 *
 * Annotation to mark properties of a Resource implementation as contributing to the
 * identity of that annotated Resource.
 *
 *
 * The interpretation of {@code @HashId} depends on the value of the annotated property:
 * (.) Resource typed properties have a structural hash taken from the outgoing properties
 * (.) The value of String typed properties are used directly as the hash. Special and
 * non-IRI characters should be avoided.
 *
 *
 * <pre>
 * {@code @}ResourceView
 * interface Person
 *   extends Resource
 * {
 *   {@code @Iri("foaf:firstName")}
 *   {@code @HashId}
 *   String getFirstName();
 *   Person setFirstName(String fn);
 *
 *   {@code @Iri("foaf:lastName")}
 *   {@code @HashId}
 *   String getLastName();
 *   Person setLastName(String fn);
 *
 *   {@code @Iri("foaf:age")}
 *   Integer getAge();
 *   Person setAge(Integer age);
 * }
 *
 * Person person = ModelFactory.createDefaultModel().as(Person.class)
 *   .setFirstName("Foo")
 *   .setLastName("Bar")
 *   .setAge(20)
 *
 * // Every person with same first/last name will yield the same hash - regardless of age
 * String hash = JenaPluginUtils.computeHashId(person);
 *
 * </pre>
 *
 *
 * Using {@code @HashId} on class level allows post-processing all obtained hashes
 * with a hash based on the class. By default it is derived from the class name.
 * The following example demonstrates that even if .getId() of A and B yield the same hash,
 * the final hash will be combined with the hash of A and B respectively:
 *
 * <pre>
 * {@code @HashId}
 * intereface A {
 *   {@code @HashId}
 *   {@code @Iri("dct:identifier")}
 *   String getId();
 * }
 *
 * {@code @HashId}
 * intereface B {
 *   {@code @HashId}
 *   {@code @Iri("dct:identifier")}
 *   String getId();
 * }
 * </pre>
 *
 * @author raven
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface HashId {
	/** By default, the predicate provided by the @Iri annotation is included in hash id generation from its value.
	 * Setting this flag to false makes hash id computation dependent only on the value for that predicate. */
    boolean excludeRdfProperty() default false;
//    Class<? extends HashIdMergeStrategy> value() default HashIdMergeStrategy.class;
}
