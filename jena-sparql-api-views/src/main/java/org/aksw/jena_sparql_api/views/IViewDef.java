package org.aksw.jena_sparql_api.views;

import java.util.Map;

import org.aksw.jena_sparql_api.restriction.RestrictionManagerImpl;
import org.apache.jena.sparql.core.QuadPattern;
import org.apache.jena.sparql.core.Var;

public interface IViewDef {
    String getName();
    QuadPattern getTemplate();

    RestrictionManagerImpl getVarRestrictions();

    VarDefinition getVarDefinition();
    IViewDef copyRenameVars(Map<Var, Var> oldToNew);


}
