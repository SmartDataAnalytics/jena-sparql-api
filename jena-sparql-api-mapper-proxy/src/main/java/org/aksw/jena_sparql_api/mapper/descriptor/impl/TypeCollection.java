package org.aksw.jena_sparql_api.mapper.descriptor.impl;

/**
 * A (bounded) collection type - i.e.g List<? extends ItemClass>
 * TODO Add a flag to mark bounded or make another collection type for unbounded?
 *
 *
 * @author raven
 *
 */
public interface TypeCollection
    extends SimpleType
{
    Class<?> getCollectionClass();
    SimpleType getItemType();

    boolean isList();
    boolean isSet();
}
