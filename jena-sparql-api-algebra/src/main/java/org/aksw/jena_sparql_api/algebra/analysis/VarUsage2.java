package org.aksw.jena_sparql_api.algebra.analysis;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

import com.google.common.collect.Multimap;

public class VarUsage2 {
	// The op node to which this var usage analysis applies
	//protected TreeNode<Op> opNode;

	/**
	 * The set of available variables at this node 
	 */
	protected Set<Var> visibleVars;
	
	
	/**
	 *  Only non-null for op nodes that perform assignments - i.e. OpAssign and OpExtend
	 */
	protected Multimap<Var, Var> varDeps;
	
	/**
	 * The set of essential variables at this node, i.e. variables required in the remainder of the
	 * query.
	 * A variable is essential if
	 * - it is a distinguished variable of the query (i.e. it is part of the final result set)
	 * - used in a filter expression
	 * - used in an assignment, whose target variable is essential
	 * - used in distinct  
	 */
	protected Set<Var> essentialVars = new LinkedHashSet<>();
	
	protected boolean distinct;
	
	public Set<Var> getVisibleVars() {
		return visibleVars;
	}

	public void setVisibleVars(Set<Var> visibleVars) {
		this.visibleVars = visibleVars;
	}

	public Multimap<Var, Var> getVarDeps() {
		return varDeps;
	}

	public void setVarDeps(Multimap<Var, Var> varDeps) {
		this.varDeps = varDeps;
	}

	public Set<Var> getEssentialVars() {
		return essentialVars;
	}

	public void setEssentialVars(Set<Var> essentialVars) {
		this.essentialVars = essentialVars;
	}

	public boolean isDistinct() {
		return distinct;
	}

	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}

	@Override
	public String toString() {
		return "VarUsage2 [visibleVars=" + visibleVars + ", varDeps=" + varDeps + ", essentialVars=" + essentialVars
				+ ", distinct=" + distinct + "]";
	}
	
	
	
	
}
