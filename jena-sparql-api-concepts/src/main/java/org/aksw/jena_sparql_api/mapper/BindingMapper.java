package org.aksw.jena_sparql_api.mapper;

import java.util.function.BiFunction;

import org.apache.jena.sparql.engine.binding.Binding;

/**
 * Similar to a RowMapper in spring-jdbc, except this interface is for (jena) bindings.
 * @author raven
 *
 */
@FunctionalInterface
public interface BindingMapper<T>
    extends BiFunction<Binding, Long, T>
{
}