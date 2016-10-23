package fr.inrialpes.tyrexmo.testqc;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.aksw.iguana.reborn.ChartUtilities2;
import org.aksw.iguana.reborn.TaskDispatcher;
import org.aksw.iguana.reborn.charts.datasets.IguanaDatasetProcessors;
import org.aksw.iguana.reborn.charts.datasets.IguanaVocab;
import org.aksw.jena_sparql_api.concept_cache.core.SparqlQueryContainmentUtils;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.delay.extra.DelayerDefault;
import org.aksw.jena_sparql_api.resources.sparqlqc.SparqlQcReader;
import org.aksw.jena_sparql_api.resources.sparqlqc.SparqlQcVocab;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.aksw.qcwrapper.jsa.ContainmentSolverWrapperJsa;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.RDFS;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;

import com.google.common.util.concurrent.MoreExecutors;
import com.hp.hpl.jena.query.QueryFactory;
import com.itextpdf.text.DocumentException;

import fr.inrialpes.tyrexmo.qcwrapper.sparqlalg.SPARQLAlgebraWrapper;

class TestCase {
	public Query source;
	public Query target;
	public boolean expectedResult;
}


class Task {
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


public class MainTestContain {

	public static final Property WARMUP = ResourceFactory.createProperty("http://ex.org/ontology#warmup");

    public static Stream<Resource> prepareTaskExecutions(Collection<Resource> workloads, String runName, int warmUp, int runs) {
    	Stream<Resource> result = IntStream.range(0, runs).boxed()
    		.flatMap(runId -> workloads.stream().map(workload -> new SimpleEntry<>(runId, workload)))
    		.map(exec -> {
    			int runId = exec.getKey();
                Model m = ModelFactory.createDefaultModel();
                Resource workload = exec.getValue();
                Model n = ResourceUtils.reachableClosure(workload);
                m.add(n);
                workload = workload.inModel(m);

                //workload.getModel().write(System.out, "TURTLE");

                //long queryId = x.getRequiredProperty(IguanaVocab.queryId).getObject().asLiteral().getLong();
                String workloadLabel = workload.getRequiredProperty(RDFS.label).getObject().asLiteral().getString();
                Resource r = m.createResource("http://example.org/query-" + runName + "-" +  workloadLabel + "-run-" + exec.getKey());
                if(runId < warmUp) {
                	r.addLiteral(WARMUP, true);
                }

                r
                	.addProperty(IguanaVocab.workload, workload)
                	.addLiteral(IguanaVocab.run, exec.getKey());
                return r;
    		});
    	return result;
    }

    public static Task prepareLegacy(Resource r, LegacyContainmentSolver solver) {
    	Resource t = r.getRequiredProperty(IguanaVocab.workload).getObject().asResource();

		String srcQueryStr = t.getRequiredProperty(SparqlQcVocab.sourceQuery).getObject().asResource().getRequiredProperty(LSQ.text).getObject().asLiteral().getString();
		String tgtQueryStr = t.getRequiredProperty(SparqlQcVocab.targetQuery).getObject().asResource().getRequiredProperty(LSQ.text).getObject().asLiteral().getString();
		boolean expected = Boolean.parseBoolean(t.getRequiredProperty(SparqlQcVocab.result).getObject().asLiteral().getString());

		Query _viewQuery = SparqlQueryContainmentUtils.queryParser.apply(srcQueryStr);
		//_viewQuery = QueryTransformOps.transform(_viewQuery, QueryUtils.createRandomVarMap(_viewQuery, "x"));

		Query _userQuery = SparqlQueryContainmentUtils.queryParser.apply(tgtQueryStr);
		//_userQuery = QueryTransformOps.transform(_userQuery, QueryUtils.createRandomVarMap(_userQuery, "y"));


//		com.hp.hpl.jena.query.Query viewQuery = QueryFactory.create(srcQueryStr.toString());
//		com.hp.hpl.jena.query.Query userQuery = QueryFactory.create(tgtQueryStr.toString());
		com.hp.hpl.jena.query.Query viewQuery = QueryFactory.create(_viewQuery.toString());
		com.hp.hpl.jena.query.Query userQuery = QueryFactory.create(_userQuery.toString());



    	return new Task(() -> { try {
			boolean actual = solver.entailed(viewQuery, userQuery);
			String str = actual == expected ? "CORRECT" : "WRONG";
			r.addLiteral(RDFS.label, str);
		} catch (ContainmentTestException e) {
			throw new RuntimeException();
		} }, () -> {
			try {
				solver.cleanup();
			} catch (ContainmentTestException e) {
				throw new RuntimeException();
			}
		});
	}


    public static Task prepare(Resource r, Object o) {
    	Task result
    		= o instanceof ContainmentSolver ? prepare(r, (ContainmentSolver)o)
    		: o instanceof LegacyContainmentSolver ? prepareLegacy(r, (LegacyContainmentSolver)o)
    		: null;

    	return result;
    }

    public static Task prepare(Resource r, ContainmentSolver solver) {
    	Resource t = r.getRequiredProperty(IguanaVocab.workload).getObject().asResource();

		String srcQueryStr = t.getRequiredProperty(SparqlQcVocab.sourceQuery).getObject().asResource().getRequiredProperty(LSQ.text).getObject().asLiteral().getString();
		String tgtQueryStr = t.getRequiredProperty(SparqlQcVocab.targetQuery).getObject().asResource().getRequiredProperty(LSQ.text).getObject().asLiteral().getString();
		boolean expected = Boolean.parseBoolean(t.getRequiredProperty(SparqlQcVocab.result).getObject().asLiteral().getString());

		Query _viewQuery = SparqlQueryContainmentUtils.queryParser.apply(srcQueryStr);
		Query viewQuery = QueryTransformOps.transform(_viewQuery, QueryUtils.createRandomVarMap(_viewQuery, "x"));

		Query _userQuery = SparqlQueryContainmentUtils.queryParser.apply(tgtQueryStr);
		Query userQuery = QueryTransformOps.transform(_userQuery, QueryUtils.createRandomVarMap(_userQuery, "y"));

    	return new Task(() -> { try {
			boolean actual = solver.entailed(viewQuery, userQuery);
			String str = actual == expected ? "CORRECT" : "WRONG";
			r.addLiteral(RDFS.label, str);
		} catch (ContainmentTestException e) {
			throw new RuntimeException();
		} }, () -> {
			try {
				solver.cleanup();
			} catch (ContainmentTestException e) {
				throw new RuntimeException();
			}
		});
	}


	public static void main(String[] args) throws IOException, InterruptedException, DocumentException {
    	List<Resource> tasks = SparqlQcReader.loadTasks("sparqlqc/1.4/benchmark/cqnoproj.rdf", "sparqlqc/1.4/benchmark/noprojection/*");

//    	tasks = tasks.stream()
//    			.filter(t -> !t.getURI().contains("19") && !t.getURI().contains("6"))
//    			.collect(Collectors.toList());


    	Map<String, Object> solvers = new LinkedHashMap<>();
    	solvers.put("JSA", new ContainmentSolverWrapperJsa());
    	solvers.put("SA", new SPARQLAlgebraWrapper());
    	//solvers.put("AFMU", new AFMUContainmentWrapper());
    	//solvers.put("TS", new TreeSolverWrapper());
    	//solvers.put("LMU", //new )

    	Model overall = ModelFactory.createDefaultModel();
    	for(Entry<String, Object> entry : solvers.entrySet()) {
    		String dataset = entry.getKey();

    		// Attach the solver to the resource
        	Iterator<Resource> taskExecs = prepareTaskExecutions(tasks, dataset, 200, 700)
        			.iterator();

	    	//ContainmentSolver solver = new ContainmentSolverWrapperJsa();
    		Object solver = entry.getValue();

	    	Model strategy = ModelFactory.createDefaultModel();

	    	PrintStream out = System.out;
			TaskDispatcher<Task> taskDispatcher =
				    new TaskDispatcher<Task>(
				        taskExecs,
				        t -> prepare(t, solver),
				        (task, r) -> task.run.run(),
				        //(task, r) -> { try { return task.call(); } catch(Exception e) { throw new RuntimeException(e); } },
				        (task, r, e) -> {}, //task.close(),
				        //r -> System.out.println("yay"));
				        r -> { if(r.getProperty(WARMUP) == null) {
//				        	System.out.println("GOT: ");
//				        	ResourceUtils.reachableClosure(r).write(System.out, "TURTLE");
				        	strategy.add(r.getModel()); }
				        }, //r.getModel().write(out, "TURTLE"),
				        new DelayerDefault(0));


			List<Runnable> runnables = Collections.singletonList(taskDispatcher);

			List<Callable<Object>> callables = runnables.stream()
				.map(Executors::callable)
				.collect(Collectors.toList());

			int workers = 1;
			ExecutorService executorService = (workers == 1
				    ? MoreExecutors.newDirectExecutorService()
				    : Executors.newFixedThreadPool(workers));

			List<Future<Object>> futures = executorService.invokeAll(callables);

			executorService.shutdown();
			executorService.awaitTermination(5, TimeUnit.SECONDS);

			if(out != System.out) {
				out.close();
			}

			for(Future<?> future : futures) {
			    try {
			        future.get();
			    } catch(Exception ex) {
			        ex.printStackTrace();
			    }
			}


			QueryExecutionFactory qef = IguanaDatasetProcessors.createQef(strategy);
			qef.createQueryExecution("CONSTRUCT { ex:" + dataset + " rdfs:label \"" + dataset + "\" } { }").execConstruct(strategy);
			qef.createQueryExecution("CONSTRUCT { ?x qb:dataset ex:" + dataset +" } { ?x ig:run ?r }").execConstruct(strategy);

			//strategy.write(System.out, "TURTLE");

			IguanaDatasetProcessors.enrichWithAvgAndStdDeviation(strategy);
			overall.add(strategy);

    	}

    	//overall.write(System.out, "TURTLE");

		CategoryDataset dataset = IguanaDatasetProcessors.createDataset(overall);

		if(false) {
			List l = dataset.getColumnKeys();
	//		String headings = dataset.getColumnKeys().stream()
	//				.map(x -> x.toString())
	//				.collect(Collectors.joining(", "));
	//
	//		System.out.println(headings);

			dataset.getRowKeys().stream().forEach(rowKey -> {
				List<String> tmp = new ArrayList<>();
				tmp.add("" + rowKey);
				for(int i = 0; i < l.size(); ++i) {
					tmp.add("" + dataset.getValue((Comparable)rowKey, (Comparable)l.get(i)));
				}
				String rowStr = String.join(", ", tmp);

	//			String rowStr = Stream.concat(
	//					Stream.of(rowKey.toString()))
	////					dataset.getColumnKeys().stream()
	////					.map(colKey -> dataset.getValue(rowKey, colKey).toString()))
	//				.collect(Collectors.joining(", "));

				System.out.println(rowStr);
			});
		}
//
//		for(int i = 0; i < dataset.getRowCount(); ++i) {
//			dataset.getK
//			String str = IntStream.range(0, dataset.getColumnCount())
//				.mapToObj(j -> "" + dataset.getValue(i, j))
//				.collect(Collectors.joining(", "));
//
//			System.out.println(str);
//		}
		//System.out.println(dataset);
		//dataset.
		//CategoryDataset dataset = createTestDataset();

		JFreeChart chart = IguanaDatasetProcessors.createStatisticalBarChart(dataset);
		ChartUtilities2.saveChartAsPDF(new File("/home/raven/tmp/test.pdf"), chart, 1000, 500);


		System.out.println("Done.");
	}
}
