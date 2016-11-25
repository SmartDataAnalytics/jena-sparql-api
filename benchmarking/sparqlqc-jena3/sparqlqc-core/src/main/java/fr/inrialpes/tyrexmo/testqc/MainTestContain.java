package fr.inrialpes.tyrexmo.testqc;

import java.io.File;
import java.io.PrintStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.aksw.commons.util.reflect.MultiMethod;
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
//import org.aksw.qcwrapper.jsa.ContainmentSolverWrapperJsa;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
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
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.xeustechnologies.jcl.JarClassLoader;
import org.xeustechnologies.jcl.JclObjectFactory;
import org.xeustechnologies.jcl.proxy.CglibProxyProvider;
import org.xeustechnologies.jcl.proxy.ProxyProviderFactory;

import com.google.common.base.Predicate;
import com.google.common.util.concurrent.MoreExecutors;

import fr.inrialpes.tyrexmo.testqc.simple.SimpleContainmentSolver;
//import fr.inrialpes.tyrexmo.qcwrapper.sparqlalg.SPARQLAlgebraWrapper;

class TestCase {
    public Query source;
    public Query target;
    public boolean expectedResult;
}

public class MainTestContain {

    public static final Property WARMUP = ResourceFactory.createProperty("http://ex.org/ontology#warmup");

    public static Stream<Resource> prepareTaskExecutions(Collection<Resource> workloads, String runName, int warmUp,
            int runs) {
        Stream<Resource> result = IntStream.range(0, runs).boxed()
                .flatMap(runId -> workloads.stream().map(workload -> new SimpleEntry<>(runId, workload))).map(exec -> {
                    int runId = exec.getKey();
                    Model m = ModelFactory.createDefaultModel();
                    Resource workload = exec.getValue();
                    Model n = ResourceUtils.reachableClosure(workload);
                    m.add(n);
                    workload = workload.inModel(m);

                    // workload.getModel().write(System.out, "TURTLE");

                    // long queryId =
                    // x.getRequiredProperty(IguanaVocab.queryId).getObject().asLiteral().getLong();
                    String workloadLabel = workload.getRequiredProperty(RDFS.label).getObject().asLiteral().getString();
                    Resource r = m.createResource(
                            "http://example.org/query-" + runName + "-" + workloadLabel + "-run-" + exec.getKey());
                    if (runId < warmUp) {
                        r.addLiteral(WARMUP, true);
                    }

                    r.addProperty(IguanaVocab.workload, workload).addLiteral(IguanaVocab.run, exec.getKey());
                    return r;
                });
        return result;
    }

    public static Task prepareLegacy(Resource r, LegacyContainmentSolver solver) {
        Resource t = r.getRequiredProperty(IguanaVocab.workload).getObject().asResource();

        String srcQueryStr = t.getRequiredProperty(SparqlQcVocab.sourceQuery).getObject().asResource()
                .getRequiredProperty(LSQ.text).getObject().asLiteral().getString();
        String tgtQueryStr = t.getRequiredProperty(SparqlQcVocab.targetQuery).getObject().asResource()
                .getRequiredProperty(LSQ.text).getObject().asLiteral().getString();
        boolean expected = Boolean
                .parseBoolean(t.getRequiredProperty(SparqlQcVocab.result).getObject().asLiteral().getString());

        Query _viewQuery = SparqlQueryContainmentUtils.queryParser.apply(srcQueryStr);
        // _viewQuery = QueryTransformOps.transform(_viewQuery,
        // QueryUtils.createRandomVarMap(_viewQuery, "x"));

        Query _userQuery = SparqlQueryContainmentUtils.queryParser.apply(tgtQueryStr);
        // _userQuery = QueryTransformOps.transform(_userQuery,
        // QueryUtils.createRandomVarMap(_userQuery, "y"));

        // com.hp.hpl.jena.query.Query viewQuery =
        // QueryFactory.create(srcQueryStr.toString());
        // com.hp.hpl.jena.query.Query userQuery =
        // QueryFactory.create(tgtQueryStr.toString());
        com.hp.hpl.jena.query.Query viewQuery = com.hp.hpl.jena.query.QueryFactory.create(_viewQuery.toString());
        com.hp.hpl.jena.query.Query userQuery = com.hp.hpl.jena.query.QueryFactory.create(_userQuery.toString());

        return new Task(() -> {
            try {
                boolean actual = solver.entailed(viewQuery, userQuery);
                String str = actual == expected ? "CORRECT" : "WRONG";
                r.addLiteral(RDFS.label, str);
            } catch (ContainmentTestException e) {
                throw new RuntimeException(e);
            }
        }, () -> {
            try {
                solver.cleanup();
            } catch (ContainmentTestException e) {
                throw new RuntimeException();
            }
        });
    }

    public static Task prepare(Resource r, Object o) {
        Task result = o instanceof ContainmentSolver ? prepare(r, (ContainmentSolver) o)
                : o instanceof LegacyContainmentSolver ? prepareLegacy(r, (LegacyContainmentSolver) o) : null;

        return result;
    }

    public static Task prepare(Resource r, ContainmentSolver solver) {
        Resource t = r.getRequiredProperty(IguanaVocab.workload).getObject().asResource();

        String srcQueryStr = t.getRequiredProperty(SparqlQcVocab.sourceQuery).getObject().asResource()
                .getRequiredProperty(LSQ.text).getObject().asLiteral().getString();
        String tgtQueryStr = t.getRequiredProperty(SparqlQcVocab.targetQuery).getObject().asResource()
                .getRequiredProperty(LSQ.text).getObject().asLiteral().getString();
        boolean expected = Boolean
                .parseBoolean(t.getRequiredProperty(SparqlQcVocab.result).getObject().asLiteral().getString());

        Query _viewQuery = SparqlQueryContainmentUtils.queryParser.apply(srcQueryStr);
        Query viewQuery = QueryTransformOps.transform(_viewQuery, QueryUtils.createRandomVarMap(_viewQuery, "x"));

        Query _userQuery = SparqlQueryContainmentUtils.queryParser.apply(tgtQueryStr);
        Query userQuery = QueryTransformOps.transform(_userQuery, QueryUtils.createRandomVarMap(_userQuery, "y"));

        return new Task(() -> { // try {
            boolean actual = solver.entailed(viewQuery, userQuery);
            String str = actual == expected ? "CORRECT" : "WRONG";
            r.addLiteral(RDFS.label, str);
            // } catch (ContainmentTestException e) {
            // throw new RuntimeException(e);
            // }
        }, () -> {
            try {
                solver.cleanup();
            } catch (ContainmentTestException e) {
                throw new RuntimeException();
            }
        });
    }

    public static void main(String[] args) throws Exception {

        if (true) {
            FrameworkFactory frameworkFactory = ServiceLoader.load(FrameworkFactory.class).iterator().next();

            Map<String, String> config = new HashMap<String, String>();
            config.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
            config.put(Constants.FRAMEWORK_BOOTDELEGATION, "*");

            config.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, String.join(",",
                "fr.inrialpes.tyrexmo.testqc.simple;version=\"1.0.0\"",
                "fr.inrialpes.tyrexmo.testqc;version=\"1.0.0\"",
                "org.apache.jena.sparql.algebra;version=\"1.0.0\"",
                "org.apache.jena.sparql.algebra.optimize;version=\"1.0.0\"",
                "org.apache.jena.sparql.algebra.op;version=\"1.0.0\"",
                "org.apache.jena.sparql.algebra.expr;version=\"1.0.0\"",
                "org.apache.jena.sparql.core;version=\"1.0.0\"",
                "org.apache.jena.sparql.syntax;version=\"1.0.0\"",
                "org.apache.jena.sparql.expr;version=\"1.0.0\"",
                "org.apache.jena.sparql.graph;version=\"1.0.0\"",
                "org.apache.jena.query;version=\"1.0.0\"",
                "org.apache.jena.graph;version=\"1.0.0\"",
                "org.apache.jena.ext.com.google.common.collect;version=\"1.0.0\"",
                "org.apache.jena.sparql.engine.binding;version=\"1.0.0\"",
                "org.slf4j;version=\"1.0.0\"",
                "org.apache.log4j;version=\"1.0.0\""
            ));

            Framework framework = frameworkFactory.newFramework(config);
            try {
                framework.init();
                framework.start();
                BundleContext context = framework.getBundleContext();
                Bundle bundle = context.installBundle(
                        "reference:file:/home/raven/Projects/Eclipse/jena-sparql-api-parent/benchmarking/sparqlqc-jena3/sparqlqc-impl-jsa/target/sparqlqc-impl-jsa-1.0.0-SNAPSHOT.jar");
                try {
                    bundle.start();
                    {
                        ServiceReference<ContainmentSolver> sr = context.getServiceReference(ContainmentSolver.class);
                        if (sr != null) {
                            ContainmentSolver c = context.getService(sr);
                            Query yy = QueryFactory.create("SELECT * { ?s ?p ?o }");
                            boolean result = c.entailed(yy, yy);
                            System.out.println("API: " + result);
                            //throw new RuntimeException("Service reference is null");
                        }
                    }

                    {
                        ServiceReference<SimpleContainmentSolver> sr = context.getServiceReference(SimpleContainmentSolver.class);
                        if (sr != null) {
                            SimpleContainmentSolver c = context.getService(sr);
                            Query yy = QueryFactory.create("SELECT * { ?s ?p ?o }");
                            boolean result = c.entailed("" + yy, "" + yy);
                            System.out.println("API-SIMPLE: " + result);
                        }
                    }
                } finally {
                    //bundle.uninstall();
                    bundle.stop();
                }
            } finally {

                framework.stop();
                framework.waitForStop(0);
            }
            System.out.println("done.");
            System.exit(0);
            return;
        }

        List<Resource> allTasks = SparqlQcReader.loadTasks("sparqlqc/1.4/benchmark/cqnoproj.rdf",
                "sparqlqc/1.4/benchmark/noprojection/*");

        // tasks = tasks.stream()
        // .filter(t -> !t.getURI().contains("19") && !t.getURI().contains("6"))
        // .collect(Collectors.toList());

        JarClassLoader jcl = new JarClassLoader();
        // jcl.getSystemLoader().setOrder(-1);
        // jcl.getParentLoader().setOrder(0);
        // jcl.getSystemLoader().setEnabled(false);
        // jcl.getLocalLoader().setEnabled(false);

        jcl.getParentLoader().setEnabled(false);
        jcl.getThreadLoader().setEnabled(false);
        jcl.getCurrentLoader().setEnabled(false);

        jcl.getLocalLoader().setOrder(100);
        jcl.addLoader(jcl.getLocalLoader());

        jcl.add("/home/raven/Projects/Eclipse/jena-sparql-api-parent/benchmarking/sparqlqc-jena3/sparqlqc-impl-jsa/target/sparqlqc-impl-jsa-1.0.0-SNAPSHOT-jar-with-dependencies.jar");

        JclObjectFactory factory;
        if (false) {
            ProxyProviderFactory.setDefaultProxyProvider(new CglibProxyProvider());
            factory = JclObjectFactory.getInstance(true);
        } else {
            factory = JclObjectFactory.getInstance();
        }

        // Create object of loaded class

        // jcl.u

        Map<String, Object> solvers = new LinkedHashMap<>();
        // C = custom - I = isomorphy
        Object o = factory.create(jcl, "org.aksw.qcwrapper.jsa.ContainmentSolverWrapperJsaVarMapper");

        // jcl.loadClass("org.aksw.qcwrapper.jsa.ContainmentSolverWrappers");

        // JclUtils.cast(object, clazz)
        Query yy = QueryFactory.create("SELECT * { ?s ?p ?o }");
        // Object zz = JclUtils.deepClone(yy);
        MultiMethod.invoke(o, "cleanup");
        Arrays.asList(o.getClass().getSuperclass().getDeclaredMethods()).stream().forEach(m -> {
            List<Class<?>> pts = Arrays.asList(m.getParameterTypes());
            pts.forEach(t -> {
                System.out.println(Query.class + " = " + t + ": " + Query.class.equals(t));
                System.out.println("  " + Query.class.getClassLoader() + " vs " + t.getClassLoader());
            });
        });
        System.out.println("foo: " + MultiMethod.invoke(o, "entailed", yy, yy));

        // ContainmentSolver xx = JclUtils.cast(o, ContainmentSolver.class,
        // MainTestContain.class.getClassLoader());
        // System.out.println("bar: " + xx.entailed(yy, yy));

        // solvers.put("JSAC", xx);
        // solvers.put("JSAC", JclUtils.toCastable(factory.create(jcl,
        // "org.aksw.qcwrapper.jsa.ContainmentSolverWrapperJsaVarMapper"),
        // ContainmentSolver.class));

        // solvers.put("JSAI", JclUtils.cast(factory.create(jcl,
        // "org.aksw.qcwrapper.jsa.ContainmentSolverWrapperJsaSubGraphIsomorphism"),
        // ContainmentSolver.class));
        // solvers.put("JSAC", new
        // ContainmentSolverWrapperJsa(VarMapper::createVarMapCandidates));
        // solvers.put("JSAG", new
        // ContainmentSolverWrapperJsa(QueryToGraph::match));
        // solvers.put("SA", new SPARQLAlgebraWrapper());

        // solvers.put("AFMU", new AFMUContainmentWrapper());
        // solvers.put("TS", new TreeSolverWrapper());
        // solvers.put("LMU", //new )

        // TODO Ideally have the blacklist in the data
        Map<String, Predicate<String>> blackLists = new HashMap<>();
        blackLists.put("AFMU", (r) -> Arrays.asList("nop3", "nop4", "nop15", "nop16").stream().anyMatch(r::contains));

        Model overall = ModelFactory.createDefaultModel();
        for (Entry<String, Object> entry : solvers.entrySet()) {
            // JarClassLoader jcl = new JarClassLoader();
            // jcl.add("/home/raven/Projects/Eclipse/jena-sparql-api-parent/benchmarking/sparqlqc-jena3/repo/lib/");

            String dataset = entry.getKey();

            Predicate<String> p = blackLists.get(dataset);

            List<Resource> tasks = allTasks.stream().filter(r -> p == null ? true : !p.apply(r.getURI()))
                    .collect(Collectors.toList());

            // Attach the solver to the resource
            Iterator<Resource> taskExecs = prepareTaskExecutions(tasks, dataset, 100, 200).iterator();

            // ContainmentSolver solver = new ContainmentSolverWrapperJsa();
            Object solver = entry.getValue();

            Model strategy = ModelFactory.createDefaultModel();

            PrintStream out = System.out;
            TaskDispatcher<Task> taskDispatcher = new TaskDispatcher<Task>(taskExecs, t -> prepare(t, solver),
                    (task, r) -> task.run.run(),
                    // (task, r) -> { try { return task.call(); }
                    // catch(Exception e) { throw new RuntimeException(e); } },
                    (task, r, e) -> {
                    }, // task.close(),
                    // r -> System.out.println("yay"));
                    r -> {
                        if (r.getProperty(WARMUP) == null) {
                            // System.out.println("GOT: ");
                            // ResourceUtils.reachableClosure(r).write(System.out,
                            // "TURTLE");
                            strategy.add(r.getModel());
                        }
                    }, // r.getModel().write(out, "TURTLE"),
                    new DelayerDefault(0));

            List<Runnable> runnables = Collections.singletonList(taskDispatcher);

            List<Callable<Object>> callables = runnables.stream().map(Executors::callable).collect(Collectors.toList());

            int workers = 1;
            ExecutorService executorService = (workers == 1 ? MoreExecutors.newDirectExecutorService()
                    : Executors.newFixedThreadPool(workers));

            List<Future<Object>> futures = executorService.invokeAll(callables);

            executorService.shutdown();
            executorService.awaitTermination(5, TimeUnit.SECONDS);

            if (out != System.out) {
                out.close();
            }

            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            QueryExecutionFactory qef = IguanaDatasetProcessors.createQef(strategy);
            qef.createQueryExecution("CONSTRUCT { ex:" + dataset + " rdfs:label \"" + dataset + "\" } { }")
                    .execConstruct(strategy);
            qef.createQueryExecution("CONSTRUCT { ?x qb:dataset ex:" + dataset + " } { ?x ig:run ?r }")
                    .execConstruct(strategy);

            // strategy.write(System.out, "TURTLE");

            IguanaDatasetProcessors.enrichWithAvgAndStdDeviation(strategy);
            overall.add(strategy);

        }

        // overall.write(System.out, "TURTLE");

        CategoryDataset dataset = IguanaDatasetProcessors.createDataset(overall);

        if (false) {
            List l = dataset.getColumnKeys();
            // String headings = dataset.getColumnKeys().stream()
            // .map(x -> x.toString())
            // .collect(Collectors.joining(", "));
            //
            // System.out.println(headings);

            dataset.getRowKeys().stream().forEach(rowKey -> {
                List<String> tmp = new ArrayList<>();
                tmp.add("" + rowKey);
                for (int i = 0; i < l.size(); ++i) {
                    tmp.add("" + dataset.getValue((Comparable) rowKey, (Comparable) l.get(i)));
                }
                String rowStr = String.join(", ", tmp);

                // String rowStr = Stream.concat(
                // Stream.of(rowKey.toString()))
                //// dataset.getColumnKeys().stream()
                //// .map(colKey -> dataset.getValue(rowKey,
                // colKey).toString()))
                // .collect(Collectors.joining(", "));

                System.out.println(rowStr);
            });
        }
        //
        // for(int i = 0; i < dataset.getRowCount(); ++i) {
        // dataset.getK
        // String str = IntStream.range(0, dataset.getColumnCount())
        // .mapToObj(j -> "" + dataset.getValue(i, j))
        // .collect(Collectors.joining(", "));
        //
        // System.out.println(str);
        // }
        // System.out.println(dataset);
        // dataset.
        // CategoryDataset dataset = createTestDataset();

        JFreeChart chart = IguanaDatasetProcessors.createStatisticalBarChart(dataset);
        ChartUtilities2.saveChartAsPDF(new File("/home/raven/tmp/test.pdf"), chart, 1000, 500);

        System.out.println("Done.");
    }
}
