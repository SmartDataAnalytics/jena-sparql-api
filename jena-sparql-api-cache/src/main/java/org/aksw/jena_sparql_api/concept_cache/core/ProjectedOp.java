package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.Collection;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;

public class ProjectedOp {
	//protected VarInfo varInfo;
	protected VarExprList projectVars;
	protected boolean distinct;
	protected Op residualOp;

	public ProjectedOp(Collection<Var> projectVars, boolean distinct, Op residualOp) {
		super();
		this.projectVars = new VarExprList();
		for(Var v : projectVars) {
			this.projectVars.add(v);
		}

		this.distinct = distinct;
		this.residualOp = residualOp;
	}

	public ProjectedOp(VarExprList projection, boolean distinct, Op residualOp) {
		super();
		this.projectVars = projection;
		this.distinct = distinct;
		this.residualOp = residualOp;
	}

	public VarExprList getProjection() {
		return projectVars;
	}

	public boolean isDistinct() {
		return distinct;
	}

	public Op getResidualOp() {
		return residualOp;
	}

	@Override
	public String toString() {
		return "ProjectedOp [projection=" + projectVars + ", distinct=" + distinct + ", residualOp=" + residualOp + "]";
	}
}
