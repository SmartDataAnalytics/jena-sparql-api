package org.aksw.qcwrapper.jsa;

import org.aksw.jena_sparql_api.sparql.algebra.mapping.VarMapper;

public class ContainmentSolverWrapperJsaVarMapper
	extends ContainmentSolverWrapperJsaBase
{
	public ContainmentSolverWrapperJsaVarMapper() {
		super(VarMapper::createVarMapCandidates);
	}
}
