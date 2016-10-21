package fr.inrialpes.tyrexmo.testqc;

import java.io.IOException;
import java.io.PrintStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.aksw.iguana.reborn.TaskDispatcher;
import org.aksw.iguana.reborn.charts.datasets.IguanaVocab;
import org.aksw.jena_sparql_api.delay.extra.DelayerDefault;
import org.aksw.jena_sparql_api.resources.sparqlqc.SparqlQcReader;
import org.aksw.jena_sparql_api.resources.sparqlqc.SparqlQcVocab;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.RDF;
import org.springframework.core.io.ClassPathResource;

import com.google.common.util.concurrent.MoreExecutors;

class TestCase {
	public Query source;
	public Query target;
	public boolean expectedResult;
}

public class MainTestContain {

	// TODO Move to SparqlQcReader
    public static List<Resource> loadTasks(String testCases, String queries) throws IOException {
		Model tests = ModelFactory.createDefaultModel();
		RDFDataMgr.read(tests, new ClassPathResource(testCases).getInputStream(), Lang.RDFXML);
        Model model = SparqlQcReader.readResources(queries);
        tests.add(model);
        List<Resource> result = tests.listResourcesWithProperty(RDF.type, SparqlQcVocab.ContainmentTest).toList();

        return result;
    }

    public static Stream<Resource> prepareTaskExecutions(Collection<Resource> tasks, int runs) {
    	Stream<Resource> result = IntStream.range(0, runs).boxed()
    		.flatMap(runId -> tasks.stream().map(task -> new SimpleEntry<>(runId, task)))
    		.map(exec -> {
                Model m = ModelFactory.createDefaultModel();
                Resource x = exec.getValue();
                Model n = ResourceUtils.reachableClosure(x);
                m.add(n);
                x = x.inModel(m);
                long queryId = x.getRequiredProperty(IguanaVocab.queryId).getObject().asLiteral().getLong();
                Resource r = m.createResource("http://example.org/query-" + queryId + "-run-" + exec.getKey());
                r.addProperty(IguanaVocab.workload, x);
                r.addLiteral(IguanaVocab.run, exec.getKey());
                return r;
    		});
    	return result;
    }

    public static Runnable exec(Resource r) {
    	return null;
    }

	public static void main(String[] args) throws IOException, InterruptedException {
    	List<Resource> tasks = loadTasks("sparqlqc/1.4/benchmark/cqnoproj.rdf", "sparqlqc/1.4/benchmark/noprojection/*");
    	Iterator<Resource> taskExecs = prepareTaskExecutions(tasks, 10).iterator();

    	PrintStream out = System.out;
		TaskDispatcher<Runnable> taskDispatcher =
			    new TaskDispatcher<Runnable>(
			        taskExecs,
					MainTestContain::exec,
			        (task, r) -> task.run(),
			        new DelayerDefault(0),
			        r -> r.getModel().write(out, "TURTLE"));


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


	}
}
