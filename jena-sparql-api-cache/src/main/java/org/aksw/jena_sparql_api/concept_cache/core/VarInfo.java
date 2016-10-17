package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

public class VarInfo {
	protected Set<Var> projectVars;
	//protected Set<Var> distinctVars;
	protected int distinctLevel; // 0: no-distinct, 1: reduced, 2: distinct

	public VarInfo(Set<Var> projectVars, int distinctLevel) {
		super();
		this.projectVars = projectVars;
		//this.distinctVars = distinctVars;
		this.distinctLevel = distinctLevel;
	}

	public Set<Var> getProjectVars() {
		return projectVars;
	}

	public int getDistinctLevel() {
		return distinctLevel;
	}
}