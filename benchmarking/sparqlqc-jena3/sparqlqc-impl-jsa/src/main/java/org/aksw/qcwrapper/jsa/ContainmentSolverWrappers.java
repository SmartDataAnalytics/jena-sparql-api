package org.aksw.qcwrapper.jsa;

import fr.inrialpes.tyrexmo.testqc.ContainmentSolver;

public class ContainmentSolverWrappers {
	public static ContainmentSolver varMapper() {
		return new ContainmentSolverWrapperJsaVarMapper();
	}

	public static ContainmentSolver isomorphy() {
		return new ContainmentSolverWrapperJsaVarMapper();
	}
}
