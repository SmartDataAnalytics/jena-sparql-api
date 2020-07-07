package org.aksw.jena_sparql_api.mapper.descriptor.impl;


/**
 * A simplified typ class for capturing scalar types and collection types with a lower bounded type.
 * - TypeScalar.from(Integer.class), TypeScalar.from(RdfPerson.class)
 * - new TypeCollection(List.class, TypeScalar.from(RdfPerson.class) -> List<? extends RdfPerson>
 *
 *
 * @author raven
 *
 */
public interface SimpleType {
    boolean isScalar();
    boolean isCollection();

    TypeScalar asScalar();
    TypeCollection asCollection();

    /**
     * Attempt to yield the stricter type of this and the other.
     * For example, if B extends A, Type[A].stricterType(Type[B]) yields B.
     * Type[Collection<B>].stricterType(Set<A>)] yields Set<B>
     *
     * If there is no common type, null is returned
     *
     *
     * @param other
     * @return
     */
    SimpleType stricterType(SimpleType other);

//    boolean isAssignableFrom(SimpleType other);
}
