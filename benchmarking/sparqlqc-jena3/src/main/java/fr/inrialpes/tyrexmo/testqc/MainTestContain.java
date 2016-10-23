package fr.inrialpes.tyrexmo.testqc;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.AbstractMap.SimpleEntry;
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
import org.aksw.qcwrapper.jsa.ContainmentSolverWrapperJsa;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.RDFS;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;

import com.google.common.util.concurrent.MoreExecutors;
import com.itextpdf.text.DocumentException;

import fr.inrialpes.tyrexmo.qcwrapper.afmu.AFMUContainmentWrapper;
import fr.inrialpes.tyrexmo.qcwrapper.sparqlalg.SPARQLAlgebraWrapper;

class TestCase {
	public Query source;
	public Query target;
	public boolean expectedResult;
}

public class MainTestContain {

    public static Stream<Resource> prepareTaskExecutions(Collection<Resource> workloads, int runs) {
    	Stream<Resource> result = IntStream.range(0, runs).boxed()
    		.flatMap(runId -> workloads.stream().map(workload -> new SimpleEntry<>(runId, workload)))
    		.map(exec -> {
                Model m = ModelFactory.createDefaultModel();
                Resource workload = exec.getValue();
                Model n = ResourceUtils.reachableClosure(workload);
                m.add(n);
                workload = workload.inModel(m);

                //workload.getModel().write(System.out, "TURTLE");

                //long queryId = x.getRequiredProperty(IguanaVocab.queryId).getObject().asLiteral().getLong();
                String workloadLabel = workload.getRequiredProperty(RDFS.label).getObject().asLiteral().getString();
                Resource r = m.createResource("http://example.org/query-" + workloadLabel + "-run-" + exec.getKey());
                r
                	.addProperty(IguanaVocab.workload, workload)
                	.addLiteral(IguanaVocab.run, exec.getKey());
                return r;
    		});
    	return result;
    }

    public void Runnable createContainmentSolver(Query a, Query b) {

    }

    public Runnable createLegacyContainmentSolver(Query viewQuery, Query userQuery) {

    	return () -> { try {
			boolean actual = solver.entailed(viewQuery, userQuery);
			String str = actual == expected ? "CORRECT" : "WRONG";
			r.addLiteral(RDFS.label, str);
		} catch (ContainmentTestException e) {
			throw new RuntimeException();
		} };
    }

    public static Runnable prepare(Resource r, ContainmentSolver solver) {
    	Resource t = r.getRequiredProperty(IguanaVocab.workload).getObject().asResource();

		String srcQueryStr = t.getRequiredProperty(SparqlQcVocab.sourceQuery).getObject().asResource().getRequiredProperty(LSQ.text).getObject().asLiteral().getString();
		String tgtQueryStr = t.getRequiredProperty(SparqlQcVocab.targetQuery).getObject().asResource().getRequiredProperty(LSQ.text).getObject().asLiteral().getString();
		boolean expected = Boolean.parseBoolean(t.getRequiredProperty(SparqlQcVocab.result).getObject().asLiteral().getString());

		Query viewQuery = SparqlQueryContainmentUtils.queryParser.apply(srcQueryStr);
		Query userQuery = SparqlQueryContainmentUtils.queryParser.apply(tgtQueryStr);

		//System.out.println("View Query: " + viewQuery);
		//System.out.println("User Query: " + userQuery);

//		Element viewEl = viewQuery.getQueryPattern();
//		Element userEl = userQuery.getQueryPattern();

//		boolean actualVerdict = SparqlQueryContainmentUtils.tryMatch(viewQuery, userQuery);


//    	new ContainmentSolverWrapperJsa()
    }

	public static void main(String[] args) throws IOException, InterruptedException, DocumentException {
    	List<Resource> tasks = SparqlQcReader.loadTasks("sparqlqc/1.4/benchmark/cqnoproj.rdf", "sparqlqc/1.4/benchmark/noprojection/*");




    	Iterator<Resource> taskExecs = prepareTaskExecutions(tasks, 10).iterator();

    	Map<String, ContainmentSolver> solvers = new LinkedHashMap<>();
    	solvers.put("SA", new SPARQLAlgebraWrapper());
    	solvers.put("JSA", new ContainmentSolverWrapperJsa());
    	solvers.put("AFMU", new AFMUContainmentWrapper());
    	//solvers.put("LMU", //new )

    	for(Entry<String, ContainmentSolver> entry : solvers.entrySet()) {

	    	ContainmentSolver solver = new ContainmentSolverWrapperJsa();

	    	Model result = ModelFactory.createDefaultModel();

	    	PrintStream out = System.out;
			TaskDispatcher<Runnable> taskDispatcher =
				    new TaskDispatcher<Runnable>(
				        taskExecs,
				        t -> prepare(t, solver),
				        (task, r) -> task.run(),
				        //(task, r) -> { try { return task.call(); } catch(Exception e) { throw new RuntimeException(e); } },
				        (task, r, e) -> {},
				        //r -> System.out.println("yay"));
				        r -> result.add(r.getModel()), //r.getModel().write(out, "TURTLE"),
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


			QueryExecutionFactory qef = IguanaDatasetProcessors.createQef(result);
			qef.createQueryExecution("CONSTRUCT { ex:DefaultDataset rdfs:label \"Cached\" } { }").execConstruct(result);
			qef.createQueryExecution("CONSTRUCT { ?x qb:dataset ex:DefaultDataset } { ?x ig:run ?r }").execConstruct(result);

			result.write(System.out, "TURTLE");

			IguanaDatasetProcessors.enrichWithAvgAndStdDeviation(result);
    	}

		CategoryDataset dataset = IguanaDatasetProcessors.createDataset(result);
		//CategoryDataset dataset = createTestDataset();

		JFreeChart chart = IguanaDatasetProcessors.createStatisticalBarChart(dataset);
		ChartUtilities2.saveChartAsPDF(new File("/home/raven/tmp/test.pdf"), chart, 1000, 500);


	}
}
