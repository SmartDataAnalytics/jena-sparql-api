package fr.inrialpes.tyrexmo.testqc;

import java.util.concurrent.Callable;

public class Task {
	protected TestCase testCase;
	protected Callable<Boolean> run;
	protected Runnable cleanup;

	public Task(TestCase testCase, Callable<Boolean> run, Runnable cleanup) {
		super();
		this.testCase = testCase;
		this.run = run;
		this.cleanup = cleanup;
	}

	public TestCase getTestCase() {
		return testCase;
	}

	public Task setTestCase(TestCase testCase) {
		this.testCase = testCase;
		return this;
	}

	public Callable<Boolean> getRun() {
		return run;
	}

	public Runnable getCleanup() {
		return cleanup;
	}
}