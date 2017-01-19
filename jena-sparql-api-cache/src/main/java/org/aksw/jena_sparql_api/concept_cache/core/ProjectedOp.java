package org.aksw.jena_sparql_api.concept_cache.core;

import org.apache.jena.sparql.algebra.Op;

public class ProjectedOp {
	//protected VarInfo varInfo;
	//protected VarExprList projectVars;
	//protected boolean distinct;
	protected VarInfo varInfo;
	protected Op residualOp;


	// TODO: Add a Filter attribute (maybe require DNF)
	//protected Set<Set<Expr>> filterDnf;

//
//	public ProjectedOp(Set<Var> projectVars, boolean distinct, Op residualOp) {
//		super();
////		this.projectVars = new VarExprList();
////		for(Var v : projectVars) {
////			this.projectVars.add(v);
////		}
////
////		this.distinct = distinct;
//		this.varInfo = new VarInfo(projectVars, distinct == true ? 2 : 0);
//		this.residualOp = residualOp;
//	}

	public ProjectedOp(VarInfo varInfo, Op residualOp) {
		super();
//		this.projectVars = projection;
//		this.distinct = distinct;
		this.varInfo = varInfo;
		this.residualOp = residualOp;
	}

	public VarInfo getProjection() {
		return varInfo;
	}

	@Deprecated
	public boolean isDistinct() {
		return varInfo.getDistinctLevel() > 0;
	}

	public Op getResidualOp() {
		return residualOp;
	}

	@Override
	public String toString() {
		return "ProjectedOp [varInfo=" + varInfo + ", residualOp=" + residualOp + "]";
	}
}
