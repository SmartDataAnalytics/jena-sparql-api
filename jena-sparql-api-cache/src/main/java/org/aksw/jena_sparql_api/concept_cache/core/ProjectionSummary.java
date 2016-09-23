package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

public class ProjectionSummary {
	protected boolean isDistinct;
	protected Set<Var> vars;

	public ProjectionSummary(boolean isDistinct, Set<Var> vars) {
		super();
		this.isDistinct = isDistinct;
		this.vars = vars;
	}

	public boolean isDistinct() {
		return isDistinct;
	}

	public Set<Var> getVars() {
		return vars;
	}

	@Override
	public String toString(){
		return (isDistinct ? "DISTINCT " : "") + vars;
	}

}
