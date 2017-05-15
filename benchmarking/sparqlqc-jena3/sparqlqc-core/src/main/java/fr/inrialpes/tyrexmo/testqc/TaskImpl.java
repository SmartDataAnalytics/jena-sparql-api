package fr.inrialpes.tyrexmo.testqc;

import java.util.concurrent.Callable;

public class TaskImpl {
	protected TestCase testCase;
	protected Callable<Boolean> run;
	protected Runnable cleanup;

	public TaskImpl(TestCase testCase, Callable<Boolean> run, Runnable cleanup) {
		super();
		this.testCase = testCase;
		this.run = run;
		this.cleanup = cleanup;
	}

	public TestCase getTestCase() {
		return testCase;
	}

	public TaskImpl setTestCase(TestCase testCase) {
		this.testCase = testCase;
		return this;
	}

	public Boolean call() {
		boolean result;
		try {
			result = getRun().call();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		return result;
	}
	
	public Callable<Boolean> getRun() {
		return run;
	}

	public Runnable getCleanup() {
		return cleanup;
	}
}