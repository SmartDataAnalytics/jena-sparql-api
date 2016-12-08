package org.aksw.jena_sparql_api.view_matcher;

import java.util.List;
import java.util.Map;

import org.aksw.combinatorics.solvers.ProblemNeighborhoodAware;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;


public class OpProblemVarMap
	// extends Entry<Map<Op, Op>, List<ProblemNeighborhoodAware<Map<Var, Var>, Var>>>>
{
	protected Map<Op, Op> nodeMapping;
	protected List<ProblemNeighborhoodAware<Map<Var, Var>, Var>> problems;

	public OpProblemVarMap(Map<Op, Op> nodeMapping, List<ProblemNeighborhoodAware<Map<Var, Var>, Var>> problems) {
		super();
		this.nodeMapping = nodeMapping;
		this.problems = problems;
	}

	public Map<Op, Op> getNodeMapping() {
		return nodeMapping;
	}

	public List<ProblemNeighborhoodAware<Map<Var, Var>, Var>> getProblems() {
		return problems;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nodeMapping == null) ? 0 : nodeMapping.hashCode());
		result = prime * result + ((problems == null) ? 0 : problems.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OpProblemVarMap other = (OpProblemVarMap) obj;
		if (nodeMapping == null) {
			if (other.nodeMapping != null)
				return false;
		} else if (!nodeMapping.equals(other.nodeMapping))
			return false;
		if (problems == null) {
			if (other.problems != null)
				return false;
		} else if (!problems.equals(other.problems))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "OpProblemVarMap [nodeMapping=" + nodeMapping + ", problems=" + problems + "]";
	}
}
