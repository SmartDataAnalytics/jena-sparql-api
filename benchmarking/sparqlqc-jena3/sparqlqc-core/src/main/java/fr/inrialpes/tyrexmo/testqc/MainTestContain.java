package fr.inrialpes.tyrexmo.testqc;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.beast.benchmark.performance.BenchmarkTime;
import org.aksw.beast.benchmark.performance.PerformanceBenchmark;
import org.aksw.beast.enhanced.ResourceEnh;
import org.aksw.beast.rdfstream.RdfGroupBy;
import org.aksw.beast.rdfstream.RdfStream;
import org.aksw.beast.viz.xchart.XChartStatBarChartBuilder;
import org.aksw.beast.viz.xchart.XChartStatBarChartProcessor;
import org.aksw.beast.vocabs.CV;
import org.aksw.beast.vocabs.IV;
import org.aksw.beast.vocabs.OWLTIME;
import org.aksw.beast.vocabs.QB;
import org.aksw.iguana.vocab.IguanaVocab;
import org.aksw.jena_sparql_api.resources.sparqlqc.SparqlQcReader;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.expr.aggregate.AggAvg;
import org.apache.jena.sparql.expr.aggregate.lib.AccStatStdDevPopulation;
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
            "org.apache.jena.atlas.io;version=\"1.0.0\"",
            "org.apache.jena.ext.com.google.common.collect;version=\"1.0.0\"",
            "org.apache.jena.graph;version=\"1.0.0\"",
            "org.apache.jena.graph.impl;version=\"1.0.0\"",
            "org.apache.jena.rdf.model;version=\"1.0.0\"",
            "org.apache.jena.shared;version=\"1.0.0\"",
            "org.apache.jena.query;version=\"1.0.0\"",
            "org.apache.jena.sparql.algebra;version=\"1.0.0\"",
            "org.apache.jena.sparql.algebra.optimize;version=\"1.0.0\"",
            "org.apache.jena.sparql.algebra.op;version=\"1.0.0\"",
            "org.apache.jena.sparql.algebra.expr;version=\"1.0.0\"",
            "org.apache.jena.sparql.core;version=\"1.0.0\"",
            "org.apache.jena.sparql.graph;version=\"1.0.0\"",
            "org.apache.jena.sparql.engine.binding;version=\"1.0.0\"",
            "org.apache.jena.sparql.expr;version=\"1.0.0\"",
            "org.apache.jena.sparql.syntax;version=\"1.0.0\"",
            "org.apache.jena.sparql.util;version=\"1.0.0\"",
            "org.apache.jena.util.iterator;version=\"1.0.0\"",
            "org.apache.jena.vocabulary;version=\"1.0.0\"",

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

        List<String> jarFileNames = Arrays.asList("jsa", "sparqlalgebra", "afmu", "treesolver");
        //List<String> jarFileNames = Arrays.asList("jsa");

        List<String> algos = Arrays.asList();
        boolean isBlacklist = true;

//        List<String> algos = Arrays.asList("JSAC", "AFMU", "TS");
//        boolean isBlacklist = true;

        List<File> jarFiles = jarFileNames.stream().map(implStr -> {
        //List<File> jarFiles = Arrays.asList("treesolver").stream().map(implStr -> {
            String jarPathStr = String
                    .format("../sparqlqc-impl-%1$s/target/sparqlqc-impl-%1$s-1.0.0-SNAPSHOT.jar", implStr);
            File jarFile = new File(jarPathStr);
            return jarFile;
        }).collect(Collectors.toList());

        // TODO Ideally have the blacklist in the data
        Map<String, Predicate<String>> blackLists = new HashMap<>();
        blackLists.put("AFMU", r -> Arrays.asList("#nop3", "#nop4", "#nop15", "#nop16", "#p3", "#p4", "#p15", "#p16", "#p23", "#p24", "#p25", "#p26").stream().anyMatch(r::contains));
        blackLists.put("SA", r -> Arrays.asList("UCQProj").stream().anyMatch(r::contains));
        blackLists.put("TS", r -> Arrays.asList("#p23", "#p24", "#p15", "#p25", "#p26").stream().anyMatch(r::contains));         // slow p15, p25, p26

        //blackLists.put("JSAC", (r) -> true);

        Set<String> jsaOverrides = new HashSet<>(Arrays.asList(
            "http://sparql-qc-bench.inrialpes.fr/UCQProj#p24", // This is not the type of query we want to use for caching (the view is a union which partially matches into the user query)
            // TODO Fix the test case below:
            "http://sparql-qc-bench.inrialpes.fr/UCQProj#p26", // CARE! A view must not have more quad patterns than the query ; so the benchmark is correct - This consideration was WRONG: I think this is a bug in the benchmark; the expected result is wrong
            "http://sparql-qc-bench.inrialpes.fr/UCQProj#p27"  // Like p24; we require exact match of all of the views union members
        ));

        Map<String, Predicate<String>> overrides = new HashMap<>();
        overrides.put("JSAI", jsaOverrides::contains);
        overrides.put("JSAC", jsaOverrides::contains);
        overrides.put("JSAG", jsaOverrides::contains);



        List<Resource> allTasks = new ArrayList<>();
        //RDFDataMgr.read(model, new ClassPathResource("tree-matcher-queries.ttl").getInputStream(), Lang.TURTLE);
        //allTasks.addAll(model.listSubjectsWithProperty(RDF.type, SparqlQcVocab.ContainmentTest).toSet());

        allTasks.addAll(SparqlQcReader.loadTasks("sparqlqc/1.4/benchmark/cqnoproj.rdf"));
        allTasks.addAll(SparqlQcReader.loadTasks("sparqlqc/1.4/benchmark/ucqproj.rdf"));


        boolean debugASpecificTask = false;
        if(debugASpecificTask) {
	        allTasks = allTasks.stream()
	        		.filter(t -> t.getURI().equals("http://sparql-qc-bench.inrialpes.fr/CQNoProj#nop16"))
	        		.collect(Collectors.toList());
        }

//        allTasks.addAll(SparqlQcReader.loadTasksSqcf("saleem-swdf-benchmark.ttl"));

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

                        boolean contained = algos.contains(shortLabel);
                        boolean skip = isBlacklist ? contained : !contained;
                        if(skip) {
                        	continue;
                        }
                        
//                        System.out.println("[HACK] Remove this line eventually!");
//                        if(!shortLabel.equals("JSAG")) {
//                        	continue;
//                        }
                        
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


                        Predicate<String> overridden = overrides.get(shortLabel);

                        BiFunction<Resource, Object, TaskImpl> taskParser = (r, solver) -> {
                            boolean invertExpected = overridden != null && overridden.apply(r.getURI());

                            TaskImpl task = SparqlQcPreparation.prepareTask(r, solver, invertExpected);
                            return task;
                        };

                        run(tasks, shortLabel, service, taskParser)
                        	.forEach(r -> overall.add(r.getModel()));
                        //overall.add(serviceResults);
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

      XChartStatBarChartBuilder
      	.from(xChart)
      	.setErrorBarsEnabled(true)
      	.setAutoRange(true)
      	.processSeries(avgs);
      
      //XChartStatBarChartProcessor.addSeries(xChart, avgs, null, null, null, null, true);

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


    public static Stream<Resource> run(Collection<Resource> tasks, String methodLabel, Object solver, BiFunction<Resource, Object, TaskImpl> taskParser) throws Exception {

        int warmUpRuns = 10;
        int evalRuns = 10;

        Consumer<Resource> postProcess = (r) -> {
                TaskImpl task = r.as(ResourceEnh.class).getTag(TaskImpl.class).get();
                task.cleanup.run();

                  if(!r.getRequiredProperty(IV.assessment).getString().equals("CORRECT")) {
                      logger.warn("Incorrect test result for task " + r + "(" + task + "): " + FactoryBeanRdfBenchmarkRunner.toString(r, RDFFormat.TURTLE_BLOCKS));
                  } else {
                	  logger.debug("Correct result for task " + r);
                  }
              };

        RdfStream<Resource, ResourceEnh> workflow = PerformanceBenchmark.createQueryPerformanceEvaluationWorkflow(
                TaskImpl.class,
                r -> taskParser.apply(r, solver),
                (r, t) -> {
                    boolean actual;
                    actual = BenchmarkTime.benchmark(r, () -> t.run.call());
                    boolean expected = t.getTestCase().getExpectedResult();

                    String str = actual == expected ? "CORRECT" : "WRONG";
                    r
                        .addLiteral(IV.value, actual)
                        .addLiteral(IV.assessment, str);
                    postProcess.accept(r);
                },
                warmUpRuns, evalRuns);

        //workflow = workflow.peek(foo -> { System.out.println("still alive"); });
        //Model result = ModelFactory.createDefaultModel();

        String uriPattern = "http://ex.org/observation-{0}-{1}-{2}";
        //"http://example.org/query-" + runName + "-" + workloadLabel + "-run" + runId
        Stream<Resource> result = workflow
            .apply(tasks).get()
            .peek(r -> r.addProperty(IV.job, r.getProperty(IguanaVocab.workload).getProperty(RDFS.label).getString()))
            //.peek(r -> System.out.println(r.getProperty(RDFS.comment)))
            .peek(r -> r.addProperty(IV.method, methodLabel))
            .map(r -> r.as(ResourceEnh.class).rename(uriPattern, IV.method, IV.run, IV.job));
            //.peek(r -> r.getModel().write(System.out, "TURTLE"));
            //.forEach(r -> result.add(r.getModel()));

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

