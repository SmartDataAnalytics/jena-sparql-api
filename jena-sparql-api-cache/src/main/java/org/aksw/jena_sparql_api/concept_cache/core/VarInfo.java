package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

public class VarInfo {
	public Set<Var> projectVars;
	public Set<Var> distinctVars;

	public VarInfo(Set<Var> projectVars, Set<Var> distinctVars) {
		super();
		this.projectVars = projectVars;
		this.distinctVars = distinctVars;
	}

}