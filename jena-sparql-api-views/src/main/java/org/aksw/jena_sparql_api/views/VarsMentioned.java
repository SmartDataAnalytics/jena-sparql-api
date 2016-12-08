package org.aksw.jena_sparql_api.views;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

public interface VarsMentioned {
    Set<Var> varsMentioned();
}
