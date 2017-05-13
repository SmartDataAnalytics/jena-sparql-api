package fr.inrialpes.tyrexmo.testqc;

public class FactoryBeanRdfBenchmarkRunner<T> {
	protected Long numWarmupRuns = 10l;
	protected Long numBenchmarkRuns = 10l;
		
	public FactoryBeanRdfBenchmarkRunner<T> setNumWarmupRuns(long numWarmupRuns) {
		this.numWarmupRuns = numWarmupRuns;
		return this;
	}
	
	public FactoryBeanRdfBenchmarkRunner<T> setNumBenchmarkRuns(long numBenchmarkRuns) {
		this.numBenchmarkRuns = numBenchmarkRuns;
		return this;
	}
	
//	public FactoryBeanRdfBenchmarkRunner<T> setInputStream() {
//		
//	}
	
	
	//public static create<T> create(Class<T> taskClass)
}
