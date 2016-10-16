package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

public class VarInfo {
	protected Set<Var> projectVars;
	protected Set<Var> distinctVars;

	public VarInfo(Set<Var> projectVars, Set<Var> distinctVars) {
		super();
		this.projectVars = projectVars;
		this.distinctVars = distinctVars;
	}

	public Set<Var> getProjectVars() {
		return projectVars;
	}

	public Set<Var> getDistinctVars() {
		return distinctVars;
	}
}