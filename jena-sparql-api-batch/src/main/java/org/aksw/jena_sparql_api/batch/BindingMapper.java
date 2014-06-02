package org.aksw.jena_sparql_api.batch;

import com.hp.hpl.jena.sparql.engine.binding.Binding;

/**
 * Similar to a RowMapper in spring-jdbc, except this interface is for (jena) bindings.
 * @author raven
 *
 */
public interface BindingMapper<T> {
    T map(Binding binding, long rowNum);
}