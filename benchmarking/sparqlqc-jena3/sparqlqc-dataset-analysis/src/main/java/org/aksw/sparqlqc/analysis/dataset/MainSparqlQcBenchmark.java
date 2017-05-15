package org.aksw.sparqlqc.analysis.dataset;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.aksw.beast.rdfstream.RdfGroupBy;
import org.aksw.beast.viz.xchart.XChartStatBarChartBuilder;
import org.aksw.beast.vocabs.CV;
import org.aksw.beast.vocabs.IV;
import org.aksw.beast.vocabs.OWLTIME;
import org.aksw.jena_sparql_api.resources.sparqlqc.SparqlQcReader;
import org.aksw.jena_sparql_api.resources.sparqlqc.SparqlQcVocab;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.expr.aggregate.AggAvg;
import org.apache.jena.sparql.expr.aggregate.AggSum;
import org.apache.jena.sparql.expr.aggregate.lib.AccStatStdDevPopulation;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.VectorGraphicsEncoder;
import org.knowm.xchart.VectorGraphicsEncoder.VectorGraphicsFormat;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inrialpes.tyrexmo.testqc.FactoryBeanRdfBenchmarkRunner;
import fr.inrialpes.tyrexmo.testqc.SparqlQcPreparation;
import fr.inrialpes.tyrexmo.testqc.SparqlQcTools;
import fr.inrialpes.tyrexmo.testqc.TaskImpl;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;



public class MainSparqlQcBenchmark {
	
	private static final Logger logger = LoggerFactory.getLogger(MainSparqlQcBenchmark.class);
	
	
	public static void main(String[] args) throws Exception {

        OptionParser parser = new OptionParser();

        SparqlQcTools.init();

        OptionSpec<String> fileOs = parser
                .acceptsAll(Arrays.asList("f", "file"), "The benchmark file")
                .withRequiredArg()
                //.defaultsTo(null)
                ;

        OptionSpec<String> q1Os = parser
                .acceptsAll(Arrays.asList("q1", "query1"), "First query.")
                .availableUnless(fileOs)
                .withRequiredArg()
                //.defaultsTo(null)
                ;

        OptionSpec<String> q2Os = parser
                .acceptsAll(Arrays.asList("q2", "query2"), "First query.")
                .availableUnless(fileOs)
                .requiredIf(q1Os)
                .withRequiredArg()
                //.defaultsTo(null)
                ;

        OptionSpec<Boolean> expectedResultOs = parser
                .acceptsAll(Arrays.asList("e", "expected"), "Expected result")
                .availableUnless(fileOs)
                .withRequiredArg()
                .ofType(Boolean.class)
                .defaultsTo(false)
                ;

        
        OptionSpec<String> solverOs = parser
                .acceptsAll(Arrays.asList("s", "solver"), "The solver to use, one of: " + SparqlQcTools.solvers.keySet())
                .withRequiredArg()
                //.defaultsTo(null)
                ;

        OptionSet options = parser.parse(args);
	
        
        
        //filename = "saleem-swdf-benchmark.ttl";
        List<Resource> testCases;
        if(options.has(fileOs)) {
            String filename = fileOs.value(options);
            logger.info("Loading: " + filename);
        	testCases = SparqlQcReader.loadTasksSqcf(filename);
        } else if(options.has(q1Os)) {
            String q1 = q1Os.value(options);
            String q2 = q2Os.value(options);
            boolean expectedResult = expectedResultOs.value(options);

            Model m = ModelFactory.createDefaultModel();
            Resource q1Res = m.createResource()
            	.addProperty(RDF.type, LSQ.Query)
            	.addProperty(RDFS.label, "q1")
            	.addProperty(LSQ.text, q1);

            Resource q2Res = m.createResource()
                	.addProperty(RDF.type, LSQ.Query)
                	.addProperty(RDFS.label, "q2")
                	.addProperty(LSQ.text, q2);
            
            Resource testCaseRes = m.createResource()
            	.addProperty(RDF.type, SparqlQcVocab.ContainmentTest)
            	.addProperty(RDFS.label, "custom-q1-q2")
            	.addLiteral(SparqlQcVocab.result, expectedResult)
            	.addProperty(SparqlQcVocab.sourceQuery, q1)
            	.addProperty(SparqlQcVocab.targetQuery, q2);
            
            testCases = Collections.singletonList(testCaseRes);
        } else {
        	parser.printHelpOn(System.err);
        	//throw new RuntimeException("No a");
        	System.exit(1);
        	return;
        }

        
        //testCases = testCases.subList(0, 10);
        
        
        
        String solverLabel = solverOs.value(options);
        
        //String methodLabel = "JSAC";
        Object solver = SparqlQcTools.solvers.get(solverLabel);
        
        
        Model metaModel = ModelFactory.createDefaultModel();
        Resource jsac = metaModel.createResource("http://ex.org/series/" + solverLabel);
        metaModel.add(jsac, RDFS.label, "JSAC");
        
        logger.info("Got solver " + solverLabel + " " + solver);
        
//        Function<Resource, Callable<Boolean>> taskParser = (r) -> {
//            Callable<Boolean> task = SparqlQcPreparation.prepareTask(r, solver, false);
//            return task;
//        };
        
        int evalRuns = 1;
        
        List<Resource> observations =
        		FactoryBeanRdfBenchmarkRunner.create(TaskImpl.class)
        			.setMetaModel(metaModel)
        			.setMethodLabel((r) -> jsac) // Note: this could be a supplier which derives it from the workload
        			.setNumEvalRuns(evalRuns)
        			.setNumWarmupRuns(0)
        			.setTaskParser(r -> SparqlQcPreparation.prepareTask(r, solver, false))
        			.setTaskExecutor((o, t) -> t.call())
        			.setExpectedValueSupplier((r, t) -> t.getTestCase().getExpectedResult())
        			.setWorkload(testCases)
        			.run()
        			.collect(Collectors.toList());
            
        // Chart specific post processing
        List<Resource> avgs =
        RdfGroupBy.enh()
            .on(CV.category) // Rename workload to category
            .on(CV.categoryLabel)
            .on(CV.series)
            .on(CV.seriesLabel)
            .agg(CV.value, OWLTIME.numericDuration, AggAvg.class)
            .agg(CV.stDev, OWLTIME.numericDuration, AccStatStdDevPopulation.class)
            .apply(observations.stream())
//            .peek(g -> {
//            	g
//            		.addLiteral(CV.seriesLabel, o)
//            })
            //.map(g -> g.rename("http://ex.org/avg/query{0}-user{1}", IV.job, IV.thread, IV.thread))
            .map(g -> g.rename("http://ex.org/avg/query{0}-user{1}", CV.seriesLabel, CV.categoryLabel))
            .collect(Collectors.toList());


        System.out.println("Individually observed performance measures: ");
        for(Resource avg : avgs) {
        	System.out.println(avg.getURI() + "\t" + avg.getProperty(CV.value).getDouble() + "\t" + avg.getProperty(CV.stDev).getDouble());
        }
        

        // Total times for each run - base for deriving the query mix per hour (qmph) standard metric
        List<Resource> totalRunTimePerRun =
	        RdfGroupBy.enh()
	        .on(IV.run) // Rename workload to category
	        //.on(IV.experiment)
	        .agg(CV.value, OWLTIME.numericDuration, AggSum.class)
	        .apply(observations.stream())
            .map(g -> g.rename("http://ex.org/totalRunTime-{0}", IV.run))
            .collect(Collectors.toList());

        System.out.println("Total time per run ");
        for(Resource r : totalRunTimePerRun) {
        	System.out.println(r.getURI() + "\t" + r.getProperty(CV.value).getDouble());
        }

        
        // Hack to add a constant property to group on
        totalRunTimePerRun.forEach(r -> r.addProperty(IV.experiment, "default"));

        
        List<Resource> avgRunTimeAcrossAllRuns =
	        RdfGroupBy.enh()
	            .on(IV.experiment) // Rename workload to category
	            .agg(CV.value, CV.value, AggAvg.class)
	            .agg(CV.stDev, CV.value, AccStatStdDevPopulation.class)
	            .apply(totalRunTimePerRun.stream())
	            .map(g -> g.rename("http://ex.org/avgRunTime"))
	            .collect(Collectors.toList());

        System.out.println("Average run runtime");
        for(Resource r : avgRunTimeAcrossAllRuns) {
        	System.out.println(r.getURI() + "\t" + r.getProperty(CV.value).getDouble() + "\t" + r.getProperty(CV.stDev).getDouble());
        }

        
        
        // Compute the qmph time from the overall time:
        // qmph = (overallTimeInS / queryMixSize) / 60 * 60 seconds
        avgRunTimeAcrossAllRuns.forEach(r -> {
        	double avgRunTime = r.getRequiredProperty(CV.value).getDouble();
        	double qmph = (60.0 * 60.0) / avgRunTime;
        	System.out.println("QMPH: " + qmph);
        	//System.out.println("Overall time");
        	System.out.println(FactoryBeanRdfBenchmarkRunner.toString(r, RDFFormat.TURTLE_PRETTY));
        });

        
        
        

        // Augment the observation resources with chart information
        // Note: This is a hack, as the chart information is highly context dependent and 2 resources with these annotations cannot be fused consistently
//        avgs.forEach(g -> g
//                .addProperty(CV.category, g.getProperty(IguanaVocab.workload).getObject())
//                .addLiteral(CV.series, g.getProperty(IV.method).getString()) // g.getProperty(IV.job).getObject()
//        );

//        avgs.forEach(r -> RDFDataMgr.write(System.out, r.getModel(), RDFFormat.TURTLE_BLOCKS));

//      File outFile = File.createTempFile("beast-", ".pdf").getAbsoluteFile();
      CategoryChart xChart = new CategoryChartBuilder()
              .width(1650)
              .height(1050)
              .title("Performance Histogram")
              .xAxisTitle("Workload")
              .yAxisTitle("Time (s)")
              .build();

      XChartStatBarChartBuilder.from(xChart)
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

        
        //Set<Resource> observations = overall.listResourcesWithProperty(RDF.type, QB.Observation).toSet();

        
        SparqlQcTools.destroy();
        logger.info("Done.");
        System.exit(0);
	}
}


//IRIFactory iriFactory = IRIFactory.iriImplementation();
//IRI iri = iriFactory.create("http://sqc-framework.aksw.org/test#1023");
//
//URI uri = new URI("http://sqc-framework.aksw.org/test#1023");
//System.out.println("fragment: " + uri.getFragment());




//System.out.println("Local name: " + ResourceFactory.createResource("http://sqc-framework.aksw.org/test#1023").getLocalName());
//
//
//Stream<Resource> observationStream = MainTestContain.run(testCases, methodLabel, solver, taskParser);
//
////observationStream.forEach(r -> {
////	RDFDataMgr.write(System.out, r.getModel(), RDFFormat.TURTLE);
////});
//
//
//
//List<Resource> observations = observationStream.filter(r -> r.hasProperty(RDF.type, QB.Observation)).collect(Collectors.toList());
//
//observations.forEach(r -> RDFDataMgr.write(System.out, r.getModel(), RDFFormat.TURTLE_BLOCKS));


//for(Resource testCase : testCases) {
//	testCase.addProperty(RDFS.label, testCase.getLocalName());
//	
//	System.out.println(testCase);
//	RDFDataMgr.write(System.out, ResourceUtils.reachableClosure(testCase), RDFFormat.TURTLE_PRETTY);
//}

