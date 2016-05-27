package org.aksw.jena_sparql_api.views;

import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.restriction.RestrictionManagerImpl;
import org.apache.jena.sparql.core.QuadPattern;
import org.apache.jena.sparql.core.Var;

public interface IViewDef {
    String getName();
    QuadPattern getTemplate();

    RestrictionManagerImpl getVarRestrictions();

    Set<Var> getVarsMentioned();
    VarDefinition getVarDefinition();
    IViewDef copyRenameVars(Map<Var, Var> oldToNew);

}
