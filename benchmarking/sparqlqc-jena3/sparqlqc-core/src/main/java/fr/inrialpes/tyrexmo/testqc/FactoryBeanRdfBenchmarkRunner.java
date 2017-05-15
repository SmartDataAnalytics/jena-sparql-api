package fr.inrialpes.tyrexmo.testqc;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.aksw.beast.benchmark.performance.BenchmarkTime;
import org.aksw.beast.benchmark.performance.PerformanceBenchmark;
import org.aksw.beast.enhanced.ResourceEnh;
import org.aksw.beast.rdfstream.RdfStream;
import org.aksw.beast.vocabs.CV;
import org.aksw.beast.vocabs.IV;
import org.aksw.iguana.vocab.IguanaVocab;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * A benchmark workflow model.
 * 
 * Given a set of resources, where each resource corresponds to a task description.
 * Each task resource is parsed into a callable, whose invocation corresponds to the execution of a task.
 * 
 * 
 * 
 * 
 * @author raven
 *
 * @param <S> A solver object
 * @param <R>
 */
public class FactoryBeanRdfBenchmarkRunner<T> {
	
	private static final Logger logger = LoggerFactory.getLogger(FactoryBeanRdfBenchmarkRunner.class);

	
	protected Class<T> taskClass;

	/**
	 * Meta information, such as the series resources and labels go in here.
	 * The meta model is copied into each observation resource
	 */
	protected Model metaModel;

	/**
	 * Maps a workload to a data series
	 */
	protected Function<Resource, Resource> seriesMapper;
	
	protected String observationUriPattern = "http://ex.org/observation-{0}-{1}-{2}";
	protected int numWarmupRuns = 10;
	protected int numEvalRuns = 10;

	protected Collection<Resource> workload;
	
	/**
	 * taskParser(taskDescriptionResource, outObservationResource, runnable)
	 * 
	 */
	protected Function<? super Resource, ? extends T> taskParser;
	protected BiFunction<Resource, T, Object> taskExecutor;
	protected BiFunction<Resource, T, Object> expectedValueSupplier;
	
	
	//protected postProcessor(V)
	
	//protected Function<Resource, Object> expectedValue;
	
	
	protected Function<Resource, String> resourceToLabel = FactoryBeanRdfBenchmarkRunner::getResourceLabel; 
	
	
	
	
	
	public Model getMetaModel() {
		return metaModel;
	}

	public FactoryBeanRdfBenchmarkRunner<T> setMetaModel(Model metaModel) {
		this.metaModel = metaModel;
		return this;
	}

	public Function<Resource, Resource> getMethodLabel() {
		return seriesMapper;
	}

	public FactoryBeanRdfBenchmarkRunner<T> setMethodLabel(Function<Resource, Resource> seriesMapper) {
		this.seriesMapper = seriesMapper;
		return this;
	}

	public FactoryBeanRdfBenchmarkRunner(Class<T> taskClass) {
		super();
		this.taskClass = taskClass;
	}

	public static <X> FactoryBeanRdfBenchmarkRunner<X> create(Class<X> taskClass) {
		return new FactoryBeanRdfBenchmarkRunner<>(taskClass);
		//return result;
	}
	
	
	
	public static String getResourceLabel(Resource r) {
		if(r.isAnon()) {
			throw new RuntimeException("No blank node expected here, got: " + toString(ResourceUtils.reachableClosure(r), RDFFormat.TURTLE_PRETTY));
		}
		
		String result = r.getLocalName();
		
		if(Strings.isNullOrEmpty(result)) {
			try {
				URI uri = new URI(result);
				result = uri.getFragment();
			} catch(Exception e) {
				// silently ignore
			}
		}
		
		if(Strings.isNullOrEmpty(result)) {
			result = r.getURI();
		}
		
		//System.out.println("test: " + r + " " + result);
		
		return result;
	}
	
	
//    BiFunction<Resource, Object, Task> taskParser = (r, solver) -> {
//        boolean invertExpected = overridden != null && overridden.apply(r.getURI());
//
//        Task task = SparqlQcPreparation.prepareTask(r, solver, invertExpected);
//        return task;
//    };
	
	   public static String toString(Resource r, RDFFormat format) {
	        Model m = ResourceUtils.reachableClosure(r);
	        String result = toString(m, format);
	        return result;
	    }

	    public static String toString(Model model, RDFFormat format) {
	        ByteArrayOutputStream out = new ByteArrayOutputStream();
	        RDFDataMgr.write(out, model, format);
	        String result = out.toString();
	        return result;
	    }

	


	public FactoryBeanRdfBenchmarkRunner<T> setNumWarmupRuns(int numWarmupRuns) {
		this.numWarmupRuns = numWarmupRuns;
		return this;
	}


	public FactoryBeanRdfBenchmarkRunner<T> setNumEvalRuns(int numEvalRuns) {
		this.numEvalRuns = numEvalRuns;
		return this;
	}
	
    public String getObservationUriPattern() {
		return observationUriPattern;
	}


	public FactoryBeanRdfBenchmarkRunner<T> setObservationUriPattern(String uriPattern) {
		this.observationUriPattern = uriPattern;
		return this;
	}


	public Function<? super Resource, ? extends T> getTaskParser() {
		return taskParser;
	}


	public FactoryBeanRdfBenchmarkRunner<T> setTaskParser(Function<? super Resource, ? extends T> taskParser) {
		this.taskParser = taskParser;
		return this;
	}


	public Function<Resource, String> getResourceToLabel() {
		return resourceToLabel;
	}


	public FactoryBeanRdfBenchmarkRunner<T> setResourceToLabel(Function<Resource, String> resourceToLabel) {
		this.resourceToLabel = resourceToLabel;
		return this;
	}

	

	public BiFunction<Resource, T, Object> getTaskExecutor() {
		return taskExecutor;
	}

	public FactoryBeanRdfBenchmarkRunner<T> setTaskExecutor(BiFunction<Resource, T, Object> taskExecutor) {
		this.taskExecutor = taskExecutor;
		return this;
	}

	public int getNumWarmupRuns() {
		return numWarmupRuns;
	}


	public int getNumEvalRuns() {
		return numEvalRuns;
	}

	
	public BiFunction<Resource, T, Object> getExpectedValueSupplier() {
		return expectedValueSupplier;
	}

	public FactoryBeanRdfBenchmarkRunner<T> setExpectedValueSupplier(BiFunction<Resource, T, Object> expectedValueSupplier) {
		this.expectedValueSupplier = expectedValueSupplier;
		return this;
	}
	
	public Collection<Resource> getWorkload() {
		return workload;
	}

	public FactoryBeanRdfBenchmarkRunner<T> setWorkload(Collection<Resource> workload) {
		this.workload = workload;
		return this;
	}

	public Stream<Resource> run() throws Exception {

        Consumer<Resource> postProcess = (r) -> {
                TaskImpl task = r.as(ResourceEnh.class).getTag(TaskImpl.class).get();
                task.cleanup.run();

                  if(!r.getRequiredProperty(IV.assessment).getString().equals("CORRECT")) {
                      logger.warn("Incorrect test result for task " + r + "(" + task + "): " + toString(r, RDFFormat.TURTLE_BLOCKS));

                  }
              };

        RdfStream<Resource, ResourceEnh> workflow = PerformanceBenchmark.<T>createQueryPerformanceEvaluationWorkflow(
        		taskClass,
                workloadRes -> {
                	T task = taskParser.apply(workloadRes);
                	return task;
                },
                (obsRes, t) -> {
                	// Copy the meta model into the observation model
                	if(metaModel != null) {
                		obsRes.getModel().add(metaModel);
                	}
                	
                    Object actual = BenchmarkTime.benchmark(obsRes, () -> taskExecutor.apply(obsRes, t));
                    if(actual != null) {
	                    obsRes
	                      .addLiteral(IV.value, actual);
                    }
                    
                    if(expectedValueSupplier != null) {
                    	Object expected = expectedValueSupplier.apply(obsRes, t);
                        String str = Objects.equals(actual, expected) ? "CORRECT" : "WRONG";
                      
                        obsRes
                        	.addLiteral(IV.assessment, str);
                        	postProcess.accept(obsRes);
                    }
                    
//                    
//                    boolean expected = t.getTestCase().getExpectedResult();
//
//                    String str = Objects.equals(actual, expected) ? "CORRECT" : "WRONG";
//                    r
//                        .addLiteral(IV.value, actual)
//                        .addLiteral(IV.assessment, str);
                    postProcess.accept(obsRes);
                },
                numWarmupRuns, numEvalRuns);

        Stream<Resource> result = workflow
            .apply(workload).get()
            //.peek(r -> r.addProperty(CV.categoryLabel, resourceToLabel.apply(r)))
            .peek(obsRes -> {
            	// Extracting the workload label is not necessary here
            	// As this can be done from the workload itself
            	Resource workloadRes = obsRes.getPropertyResourceValue(IguanaVocab.workload);
            	String categoryLabel = workloadRes.getProperty(RDFS.label).getString();
            	
            	obsRes
            		.addProperty(CV.category, IguanaVocab.workload)
            		.addProperty(CV.categoryLabel, categoryLabel);
            })            
            .peek(obsRes -> {
            	Resource workloadRes = obsRes.getPropertyResourceValue(IguanaVocab.workload);
            	Resource seriesRes = seriesMapper.apply(workloadRes);
            	String seriesLabel = seriesRes.getProperty(RDFS.label).getString();
            	
            	// Note: We need the series label here in order to construct a pretty URI for the observation
            	obsRes
            		.addProperty(CV.series, seriesRes)
            		.addProperty(CV.seriesLabel, seriesLabel);
            })
            .map(r -> r.as(ResourceEnh.class).rename(observationUriPattern, CV.seriesLabel, IV.run, CV.categoryLabel));
            //.peek(r -> r.getModel().write(System.out, "TURTLE"));
            //.forEach(r -> result.add(r.getModel()));

        return result;
		
	}
	
}
