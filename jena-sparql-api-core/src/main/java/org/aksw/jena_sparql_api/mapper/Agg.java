package org.aksw.jena_sparql_api.mapper;

import java.util.Set;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;


public interface Agg<T>
    extends Aggregator<Binding, T>
{
    Acc<T> createAccumulator();

    /**
     * An accumulator may declare the variable it references.
     * The variables can be derived from e.g. underlying Sparql expressions or
     * sub aggregators.
     * @return
     */

    Set<Var> getDeclaredVars();
}
