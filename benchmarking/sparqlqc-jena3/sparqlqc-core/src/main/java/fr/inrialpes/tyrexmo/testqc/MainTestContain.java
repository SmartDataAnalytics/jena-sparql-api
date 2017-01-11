package fr.inrialpes.tyrexmo.testqc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.aksw.beast.benchmark.performance.BenchmarkTime;
import org.aksw.beast.benchmark.performance.PerformanceBenchmark;
import org.aksw.beast.enhanced.ResourceEnh;
import org.aksw.beast.rdfstream.RdfGroupBy;
import org.aksw.beast.rdfstream.RdfStream;
import org.aksw.beast.viz.xchart.XChartStatBarChartProcessor;
import org.aksw.beast.vocabs.CV;
import org.aksw.beast.vocabs.IV;
import org.aksw.beast.vocabs.OWLTIME;
import org.aksw.beast.vocabs.QB;
import org.aksw.iguana.vocab.IguanaVocab;
import org.aksw.jena_sparql_api.resources.sparqlqc.SparqlQcReader;
import org.aksw.jena_sparql_api.resources.sparqlqc.SparqlQcVocab;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.expr.aggregate.AggAvg;
import org.apache.jena.sparql.expr.aggregate.lib.AccStatStdDevPopulation;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.VectorGraphicsEncoder;
import org.knowm.xchart.VectorGraphicsEncoder.VectorGraphicsFormat;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class MainTestContain {
    private static final Logger logger = LoggerFactory.getLogger(MainTestContain.class);

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

    public static Task prepareTaskJena2(TestCase testCase, LegacyContainmentSolver solver) {

//        Query _viewQuery = SparqlQueryContainmentUtils.queryParser.apply("" + testCase.getSource());
//        Query _userQuery = SparqlQueryContainmentUtils.queryParser.apply("" + testCase.getTarget());

        com.hp.hpl.jena.query.Query viewQuery = com.hp.hpl.jena.query.QueryFactory.create(testCase.getSource().toString());
        com.hp.hpl.jena.query.Query userQuery = com.hp.hpl.jena.query.QueryFactory.create(testCase.getTarget().toString());

        return new Task(testCase,
        	() -> {
            try {
                boolean actual = solver.entailed(viewQuery, userQuery);
                //String str = actual == testCase.getExpectedResult() ? "CORRECT" : "WRONG";
                return actual;
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

    public static TestCase parseTestCase(Resource t) {
        String srcQueryStr = t.getRequiredProperty(SparqlQcVocab.sourceQuery).getObject().asResource()
                .getRequiredProperty(LSQ.text).getObject().asLiteral().getString();
        String tgtQueryStr = t.getRequiredProperty(SparqlQcVocab.targetQuery).getObject().asResource()
                .getRequiredProperty(LSQ.text).getObject().asLiteral().getString();
        boolean expected = Boolean
                .parseBoolean(t.getRequiredProperty(SparqlQcVocab.result).getObject().asLiteral().getString());

        Query viewQuery = QueryFactory.create(srcQueryStr); //SparqlQueryContainmentUtils.queryParser.apply(srcQueryStr);
        Query userQuery = QueryFactory.create(tgtQueryStr); //SparqlQueryContainmentUtils.queryParser.apply(tgtQueryStr);

        TestCase result = new TestCase(viewQuery, userQuery, expected);
        return result;
    }

    public static Task prepareTask(Resource w, Object o) {
        //Resource w = r.getRequiredProperty(IguanaVocab.workload).getObject().asResource();
        TestCase testCase = parseTestCase(w);

        Task result;
        if(o instanceof ContainmentSolver) {
            result = prepareTaskJena3(testCase, (ContainmentSolver)o);
        } else if(o instanceof LegacyContainmentSolver) {
            result = prepareTaskJena2(testCase, (LegacyContainmentSolver) o);
        } else {
            throw new RuntimeException("Unknown task type: " + o);
        }

        return result;
    }

    public static Task prepareTaskJena3(TestCase testCase, ContainmentSolver solver) {

//        Query _viewQuery = QueryTransformOps.transform(viewQuery, QueryUtils.createRandomVarMap(_viewQuery, "x"));
//        Query _userQuery = QueryTransformOps.transform(userQuery, QueryUtils.createRandomVarMap(_userQuery, "y"));

        return new Task(
        	testCase,
        	() -> { // try {
            boolean actual = solver.entailed(testCase.getSource(), testCase.getTarget());
        	//boolean actual = solver.entailed(testCase.getTarget(), testCase.getSource());
            //String str = actual == testCase.getExpectedResult() ? "CORRECT" : "WRONG";
            //System.out.println(str);
            return actual;
            //r.addLiteral(RDFS.label, str);
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

        Model overall = ModelFactory.createDefaultModel();

        FrameworkFactory frameworkFactory = ServiceLoader.load(FrameworkFactory.class).iterator().next();

        Map<String, String> config = new HashMap<String, String>();
        config.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
        config.put(Constants.FRAMEWORK_BOOTDELEGATION, "*");

        config.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, String.join(",",
            // API
            "fr.inrialpes.tyrexmo.testqc.simple;version=\"1.0.0\"",
            "fr.inrialpes.tyrexmo.testqc;version=\"1.0.0\"",

            // Dirty API packages (probably should go elsewhere)
            "fr.inrialpes.tyrexmo.queryanalysis;version=\"1.0.0\"",

            // Jena 3
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
            "org.apache.jena.atlas.io;version=\"1.0.0\"",

            // Jena 2 (legacy)
            "com.hp.hpl.jena.sparql;version=\"1.0.0\"",
            "com.hp.hpl.jena.sparql.algebra;version=\"1.0.0\"",
            "com.hp.hpl.jena.sparql.algebra.optimize;version=\"1.0.0\"",
            "com.hp.hpl.jena.sparql.algebra.op;version=\"1.0.0\"",
            "com.hp.hpl.jena.sparql.algebra.expr;version=\"1.0.0\"",
            "com.hp.hpl.jena.sparql.engine;version=\"1.0.0\"",
            "com.hp.hpl.jena.sparql.core;version=\"1.0.0\"",
            "com.hp.hpl.jena.sparql.syntax;version=\"1.0.0\"",
            "com.hp.hpl.jena.sparql.expr;version=\"1.0.0\"",
            "com.hp.hpl.jena.sparql.expr.nodevalue;version=\"1.0.0\"",
            "com.hp.hpl.jena.sparql.graph;version=\"1.0.0\"",
            "com.hp.hpl.jena.sparql.util.graph;version=\"1.0.0\"",
            "com.hp.hpl.jena.query;version=\"1.0.0\"",
            "com.hp.hpl.jena.graph;version=\"1.0.0\"",
            "com.hp.hpl.jena.ext.com.google.common.collect;version=\"1.0.0\"",
            "com.hp.hpl.jena.sparql.engine.binding;version=\"1.0.0\"",

            "org.apache.xerces.util;version=\"1.0.0\"",
            "org.apache.xerces.impl.dv;version=\"1.0.0\"",
            "org.apache.xerces.xs;version=\"1.0.0\"",
            "org.apache.xerces.impl.dv.xs;version=\"1.0.0\"",
            "org.apache.xerces.impl.validation;version=\"1.0.0\"",

            "com.ibm.icu.text;version=\"1.0.0\"",

            // Logging
            "org.slf4j;version=\"1.7.0\""
//                "org.slf4j.impl;version=\"1.0.0\"",
//                "org.apache.log4j;version=\"1.0.0\""

            // ??? What packages are that?
            //"java_cup.runtime;version=\"1.0.0\""
        ));

        //List<String> jarFileNames = Arrays.asList("jsa", "sparqlalgebra", "afmu", "treesolver");
        List<String> jarFileNames = Arrays.asList("jsa");

        List<File> jarFiles = jarFileNames.stream().map(implStr -> {
        //List<File> jarFiles = Arrays.asList("treesolver").stream().map(implStr -> {
            String jarPathStr = String
                    .format("../sparqlqc-impl-%1$s/target/sparqlqc-impl-%1$s-1.0.0-SNAPSHOT.jar", implStr);
            File jarFile = new File(jarPathStr);
            return jarFile;
        }).collect(Collectors.toList());

        // TODO Ideally have the blacklist in the data
        Map<String, Predicate<String>> blackLists = new HashMap<>();
        blackLists.put("AFMU", (r) -> Arrays.asList("#nop3", "#nop4", "#nop15", "#nop16", "#p3", "#p4", "#p15", "#p16", "#p23", "#p24", "#p25", "#p26").stream().anyMatch(r::contains));
        blackLists.put("SA", (r) -> Arrays.asList("UCQProj").stream().anyMatch(r::contains));
        blackLists.put("TS", (r) -> Arrays.asList("#p23", "#p24", "#p15", "#p25", "#p26").stream().anyMatch(r::contains));         // slow p15, p25, p26

        List<Resource> allTasks = new ArrayList<>();
        //RDFDataMgr.read(model, new ClassPathResource("tree-matcher-queries.ttl").getInputStream(), Lang.TURTLE);
        //allTasks.addAll(model.listSubjectsWithProperty(RDF.type, SparqlQcVocab.ContainmentTest).toSet());

        allTasks.addAll(SparqlQcReader.loadTasks("sparqlqc/1.4/benchmark/cqnoproj.rdf", "sparqlqc/1.4/benchmark/noprojection/*"));
        allTasks.addAll(SparqlQcReader.loadTasks("sparqlqc/1.4/benchmark/ucqproj.rdf", "sparqlqc/1.4/benchmark/projection/*"));


//        params.addAll(createTestParams("sparqlqc/1.4/benchmark/cqnoproj.rdf", "sparqlqc/1.4/benchmark/noprojection/*"));
//        params.addAll(createTestParams("sparqlqc/1.4/benchmark/ucqproj.rdf", "sparqlqc/1.4/benchmark/projection/*"));


//          Bundle bundle = context.installBundle("reference:file:/home/raven/Projects/Eclipse/jena-sparql-api-parent/benchmarking/sparqlqc-jena3/sparqlqc-impl-jsa/target/sparqlqc-impl-jsa-1.0.0-SNAPSHOT.jar");
//          Bundle bundle = context.installBundle("reference:file:/home/raven/Projects/Eclipse/jena-sparql-api-parent/benchmarking/sparqlqc-jena3/sparqlqc-impl-afmu/target/sparqlqc-impl-afmu-1.0.0-SNAPSHOT.jar");
//          Bundle bundle = context.installBundle("reference:file:/home/raven/Projects/Eclipse/jena-sparql-api-parent/benchmarking/sparqlqc-jena3/sparqlqc-impl-treesolver/target/sparqlqc-impl-treesolver-1.0.0-SNAPSHOT.jar");
//          Bundle bundle = context.installBundle("reference:file:/home/raven/Projects/Eclipse/jena-sparql-api-parent/benchmarking/sparqlqc-jena3/sparqlqc-impl-sparqlalgebra/target/sparqlqc-impl-sparqlalgebra-1.0.0-SNAPSHOT.jar");



        int anonId = 0;

        Framework framework = frameworkFactory.newFramework(config);
        try {
            framework.init();
            framework.start();


            BundleContext context = framework.getBundleContext();

            for (File jarFile : jarFiles) {
                String jarFileStr = jarFile.getAbsolutePath();
                logger.info("Loading: " + jarFileStr);

                Bundle bundle = context.installBundle("reference:file:" + jarFileStr);
                try {
                    bundle.start();

                    // Get the distinct set of service instances
                    Multimap<String, Object> nameToService = HashMultimap.create();

                    for(ServiceReference<?> sr : bundle.getRegisteredServices()) {

                        String shortLabel = (String)sr.getProperty("SHORT_LABEL");
                        if(shortLabel == null) {
                            shortLabel = "ANON" + ++anonId;
                        }

                        Object service = context.getService(sr);

                        if(service instanceof ContainmentSolver || service instanceof LegacyContainmentSolver) {
                            nameToService.put(shortLabel, service);
                        }
                    }

                    logger.info("Found " + nameToService.size() + " appropriate service(s): ");
                    nameToService.entries().forEach(e ->
                        logger.info("  Service: label=" + e.getKey() + ", class=" + e.getValue().getClass().getName() + ", instance=" + e.getValue())
                    );


                    for(Entry<String, Object> e : nameToService.entries()) {
                        String shortLabel = e.getKey();
                        Object service = e.getValue();

                        logger.info("Benchmarking service: label=" + shortLabel + ", class=" + service.getClass().getName() + ", instance=" + service);

                        Predicate<String> p = blackLists.get(shortLabel);

                        List<Resource> tasks = allTasks.stream()
                                .filter(r -> p == null ? true : !p.apply(r.getURI()))
                                .collect(Collectors.toList());


                        Model serviceResults = run(tasks, shortLabel, service);
                        overall.add(serviceResults);
                    }

                } finally {
                    try {
                        bundle.stop();
                        bundle.uninstall();
                        logger.info("Uninstalled " + jarFileStr);
                    } catch(Exception e) {
                        logger.info("Failed to uninstall " + jarFileStr, e);
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            framework.stop();
            framework.waitForStop(0);
        }


//        File rdfOut = File.createTempFile("sparqlqc-", ".ttl");
//        overall.write(new FileOutputStream(rdfOut), "TTL");
//
//        CategoryDataset categoryDataset = IguanaDatasetProcessors.createDataset(overall);
//
//        JFreeChart chart = IguanaDatasetProcessors.createStatisticalBarChart(categoryDataset);
//        ChartUtilities2.saveChartAsPDF(new File("/home/raven/tmp/test.pdf"), chart, 1000, 500);

        Set<Resource> observations = overall.listResourcesWithProperty(RDF.type, QB.Observation).toSet();

        //observations.forEach(r -> RDFDataMgr.write(System.out, r.getModel(), RDFFormat.TURTLE_BLOCKS));

        // Chart specific post processing
        List<Resource> avgs =
        RdfGroupBy.enh()
            .on(IguanaVocab.workload)
            .on(IV.job) // This is just the local name of the workload
            .on(IV.method)
            .agg(CV.value, OWLTIME.numericDuration, AggAvg.class)
            .agg(CV.stDev, OWLTIME.numericDuration, AccStatStdDevPopulation.class)
            .apply(observations.stream())
            //.map(g -> g.rename("http://ex.org/avg/query{0}-user{1}", IV.job, IV.thread, IV.thread))
            .map(g -> g.rename("http://ex.org/avg/query{0}-user{1}", IV.job, IV.method))
            .collect(Collectors.toList());


        // Augment the observation resources with chart information
        // Note: This is a hack, as the chart information is highly context dependent and 2 resources with these annotations cannot be fused consistently
        avgs.forEach(g -> g
                .addProperty(CV.category, g.getProperty(IguanaVocab.workload).getObject())
                .addLiteral(CV.series, g.getProperty(IV.method).getString()) // g.getProperty(IV.job).getObject()
        );

//        avgs.forEach(r -> RDFDataMgr.write(System.out, r.getModel(), RDFFormat.TURTLE_BLOCKS));

//      File outFile = File.createTempFile("beast-", ".pdf").getAbsoluteFile();
      CategoryChart xChart = new CategoryChartBuilder()
              .width(1650)
              .height(1050)
              .title("Performance Histogram")
              .xAxisTitle("Workload")
              .yAxisTitle("Time (s)")
              .build();

      XChartStatBarChartProcessor.addSeries(xChart, avgs, null, null, null, null, true);

      xChart.getStyler().setLegendPosition(LegendPosition.InsideNW);

      xChart.getStyler().setYAxisLogarithmic(true);
      //xChart.getStyler().setYAxisDecimalPattern(yAxisDecimalPattern)
      xChart.getStyler().setYAxisTicksVisible(true);
      xChart.getStyler().setXAxisLabelRotation(45);
      //System.out.println(xChart.getStyler().getYAxisDecimalPattern());
      xChart.getStyler().setYAxisDecimalPattern("###,###,###,###,###.#####");

      //xChart.getStyler().setYAxisTickMarkSpacingHint(yAxisTickMarkSpacingHint)

      VectorGraphicsEncoder.saveVectorGraphic(xChart, "/tmp/Sample_Chart", VectorGraphicsFormat.SVG);
//SSystem.out.println("exp: " + Math.pow(10, Math.floor(Math.log10(0.0123))));
      new SwingWrapper<CategoryChart>(xChart).displayChart();







        logger.info("Done.");


        // Force exit due to threads of the osgi framework that stay alive for another minute
        //System.exit(0);

        // tasks = tasks.stream()
        // .filter(t -> !t.getURI().contains("19") && !t.getURI().contains("6"))
        // .collect(Collectors.toList());
    }


    public static Model run(Collection<Resource> tasks, String methodLabel, Object solver) throws Exception {

        int warmUpRuns = 1;
        int evalRuns = 2;

        Consumer<Resource> postProcess = (r) -> {
                Task task = r.as(ResourceEnh.class).getTag(Task.class).get();
                task.cleanup.run();

                  if(!r.getRequiredProperty(IV.assessment).getString().equals("CORRECT")) {
                      logger.warn("Incorrect test result for task " + r + "(" + task + "): " + toString(r, RDFFormat.TURTLE_BLOCKS));

                  }
              };

        RdfStream<Resource, ResourceEnh> workflow = PerformanceBenchmark.createQueryPerformanceEvaluationWorkflow(
        		Task.class,
        		r -> MainTestContain.prepareTask(r, solver),
        		(r, t) -> {
        			boolean actual;
					try {
			        	actual = BenchmarkTime.benchmark(r, () -> t.run.call());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					boolean expected = t.getTestCase().getExpectedResult();

                    String str = actual == expected ? "CORRECT" : "WRONG";
                    r
                    	.addLiteral(IV.value, actual)
                    	.addLiteral(IV.assessment, str);
        			postProcess.accept(r);
        		},
        		warmUpRuns, evalRuns);

        Model result = ModelFactory.createDefaultModel();

        String uriPattern = "http://ex.org/observation-{0}-{1}-{2}";
        //"http://example.org/query-" + runName + "-" + workloadLabel + "-run" + runId
        workflow
        	.apply(tasks).get()
        	.peek(r -> r.addProperty(RDFS.comment, r.getProperty(IguanaVocab.workload).getProperty(RDFS.label).getString()))
        	.peek(r -> r.addProperty(IV.method, methodLabel))
            .map(r -> r.as(ResourceEnh.class).rename(uriPattern, IV.method, IV.run, RDFS.comment))
        	//.forEach(r -> r.getModel().write(System.out, "TURTLE"));
            .forEach(r -> result.add(r.getModel()));

        return result;


        //workload.getRequiredProperty(RDFS.label).getObject().asLiteral().getString()

        // dataset, suite, run

//
//
//        RdfStream.startWithCopy()
//            // Parse the task resource
//            // Allocate a new observation resource, and copy the traits from the workload
//            .map(w -> w.getModel().createResource().as(ResourceEnh.class)
//                    .copyTagsFrom(w)
//                    .addProperty(RDF.type, Observation)
//                    .addProperty(IguanaVocab.workload, w)
//                    .addProperty(RDFS.comment, w.getProperty(RDFS.label).getString()))
//            .map(o -> o.as(ResourceEnh.class).addTag(prepareTask(o, solver)))
////			.peek(o -> PerformanceAnalyzer.start()
////					.setReportConsumer(postProcess)
////					.create()
////						.accept(o, o.as(ResourceEnh.class).getTrait(Task.class).get().run ) ))
//            .peek(r -> BenchmarkTime.benchmark(r, () -> Thread.sleep(500)))
//            //.withIndex(IguanaVocab.run))
//        .repeat(2, IguanaVocab.run, 1)
//        .peek(r -> { if (r.getProperty(IguanaVocab.run).getInt() < warmUpRuns) { r.addLiteral(WARMUP, true); }})
//        .map(r -> r.as(ResourceEnh.class).rename(uriPattern, dataset, IguanaVocab.run, RDFS.comment))
//        //.peek(r -> r.addLiteral(RDFS.comment, r.as(ResourceEnh.class).getTrait(Query.class).get().toString()))
//        .apply(() -> tasks.stream()).get()
//        .forEach(r -> r.getModel().write(System.out, "TURTLE"))
//        ;


        //Stream<Resource> taskExecs = prepareTaskExecutions(tasks, dataset, 1, 1);//.iterator();




        //function<Resource, ITask> taskParser;

//        taskExecs
//        	.peek(r -> r.as(EnhResource).addTrait(parseLiteral(null, SparqlQueryContainmentUtils.queryParser)))

//        Consumer<Resource> eval = SparqlPerformanceEvaluatorBuilder
//        	.setTaskParser(parseLiteral(null, SparqlQueryContainmentUtils.queryParser))
//			.setTaskExecutor()
//			.setProperty()
//			.create();

//
//
//        PrintStream out = System.out;
//
//
//        List<Runnable> runnables = Collections.singletonList(taskDispatcher);
//
//        List<Callable<Object>> callables = runnables.stream().map(Executors::callable).collect(Collectors.toList());
//
//        int workers = 1;
//        ExecutorService executorService = (workers == 1 ? MoreExecutors.newDirectExecutorService()
//                : Executors.newFixedThreadPool(workers));
//
//        List<Future<Object>> futures = executorService.invokeAll(callables);
//
//        executorService.shutdown();
//        executorService.awaitTermination(5, TimeUnit.SECONDS);
//
//        if (out != System.out) {
//            out.close();
//        }
//
//        for (Future<?> future : futures) {
//            try {
//                future.get();
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//
//
//        QueryExecutionFactory qef = IguanaDatasetProcessors.createQef(strategy);
//        qef.createQueryExecution("CONSTRUCT { ex:" + dataset + " rdfs:label \"" + dataset + "\" } { }")
//                .execConstruct(strategy);
//        qef.createQueryExecution("CONSTRUCT { ?x qb:dataset ex:" + dataset + " } { ?x ig:run ?r }")
//                .execConstruct(strategy);
//
//        IguanaDatasetProcessors.enrichWithAvgAndStdDeviation(strategy);
//        //overall.add(strategy);
//
//        return strategy;
    }
}



//    if (false) {
//        List l = dataset.getColumnKeys();
//        // String headings = dataset.getColumnKeys().stream()
//        // .map(x -> x.toString())
//        // .collect(Collectors.joining(", "));
//        //
//        // System.out.println(headings);
//
//        dataset.getRowKeys().stream().forEach(rowKey -> {
//            List<String> tmp = new ArrayList<>();
//            tmp.add("" + rowKey);
//            for (int i = 0; i < l.size(); ++i) {
//                tmp.add("" + dataset.getValue((Comparable) rowKey, (Comparable) l.get(i)));
//            }
//            String rowStr = String.join(", ", tmp);prepare
//
//            // String rowStr = Stream.concat(
//            // Stream.of(rowKey.toString()))
//            //// dataset.getColumnKeys().stream()
//            //// .map(colKey -> dataset.getValue(rowKey,
//            // colKey).toString()))
//            // .collect(Collectors.joining(", "));
//
//            System.out.println(rowStr);
//        });
//    }

//public static Stream<Resource> prepareTaskExecutions(Collection<Resource> workloads, String runName, int warmUp,
//int runs) {
//int totalRuns = warmUp + runs;
//
//Stream<Resource> result = IntStream.range(0, totalRuns).boxed()
//  .flatMap(runId -> workloads.stream().map(workload -> new SimpleEntry<>(runId, workload))).map(exec -> {
//      int runId = exec.getKey();
//      Model m = ModelFactory.createDefaultModel();
//      Resource workload = exec.getValue();
//      Model n = ResourceUtils.reachableClosure(workload);
//      m.add(n);
//      workload = workload.inModel(m);
//
//      // workload.getModel().write(System.out, "TURTLE");
//
//      // long queryId =
//      // x.getRequiredProperty(IguanaVocab.queryId).getObject().asLiteral().getLong();
//      String workloadLabel = workload.getRequiredProperty(RDFS.label).getObject().asLiteral().getString();
//      Resource r = m.createResource("http://example.org/query-" + runName + "-" + workloadLabel + "-run" + runId);
//
//      if (runId < warmUp) {
//          r.addLiteral(WARMUP, true);
//      }
//
//      r
//          .addProperty(IguanaVocab.workload, workload)
//          .addLiteral(IguanaVocab.run, exec.getKey());
//
////      StringWriter tmp = new StringWriter();
////      ResourceUtils.reachableClosure(r).write(tmp, "TTL");
////      System.out.println("Created run resource: " + r + " with data " + tmp);
//      return r;
//  });
//return result;
//}

