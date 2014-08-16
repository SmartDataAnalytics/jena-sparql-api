package org.aksw.jena_sparql_api.mapper;

import java.util.Set;

import com.hp.hpl.jena.sparql.core.Var;

public interface Agg<T> {
    Acc<T> createAccumulator();

    /**
     * An accumulator may declare the variable it references.
     * The variables can be derived from e.g. underlying Sparql expressions or
     * sub aggregators.
     * @return
     */

    Set<Var> getDeclaredVars();
}
