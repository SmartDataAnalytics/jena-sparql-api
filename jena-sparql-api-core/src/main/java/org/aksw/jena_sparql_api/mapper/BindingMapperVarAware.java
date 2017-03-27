package org.aksw.jena_sparql_api.mapper;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

/**
 * Binding mapper that is aware of its referenced variables
 *
 * @author raven
 *
 */
public interface BindingMapperVarAware<T>
    extends BindingMapper<T>
{
    Set<Var> getVarsMentioned();
}
