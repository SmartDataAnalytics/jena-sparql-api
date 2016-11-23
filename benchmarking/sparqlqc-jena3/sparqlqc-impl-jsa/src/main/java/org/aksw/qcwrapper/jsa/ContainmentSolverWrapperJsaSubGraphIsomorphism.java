package org.aksw.qcwrapper.jsa;

import org.aksw.jena_sparql_api.view_matcher.QueryToGraph;

public class ContainmentSolverWrapperJsaSubGraphIsomorphism
	extends ContainmentSolverWrapperJsaBase
//	implements ContainmentSolver // JCL does not find transitive interfaces :/
{
	public ContainmentSolverWrapperJsaSubGraphIsomorphism() {
		super(QueryToGraph::match);
	}
}
