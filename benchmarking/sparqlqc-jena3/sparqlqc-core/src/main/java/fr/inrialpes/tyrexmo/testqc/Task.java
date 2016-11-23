package fr.inrialpes.tyrexmo.testqc;

public class Task {
	protected Runnable run;
	protected Runnable cleanup;

	public Task(Runnable run, Runnable cleanup) {
		super();
		this.run = run;
		this.cleanup = cleanup;
	}

	public Runnable getRun() {
		return run;
	}

	public Runnable getCleanup() {
		return cleanup;
	}
}