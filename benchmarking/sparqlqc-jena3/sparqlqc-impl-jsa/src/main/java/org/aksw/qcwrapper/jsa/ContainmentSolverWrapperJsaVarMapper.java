package org.aksw.qcwrapper.jsa;

import org.aksw.jena_sparql_api.sparql.algebra.mapping.VarMapper;

public class ContainmentSolverWrapperJsaVarMapper
	extends ContainmentSolverWrapperJsaBase
//	implements ContainmentSolver // JCL does not find transitive interfaces :/
{
	public ContainmentSolverWrapperJsaVarMapper() {
		super(VarMapper::createVarMapCandidates);
	}
}
