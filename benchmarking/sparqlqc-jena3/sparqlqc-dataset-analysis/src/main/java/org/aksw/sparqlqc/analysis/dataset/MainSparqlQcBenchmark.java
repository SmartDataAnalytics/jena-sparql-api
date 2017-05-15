package org.aksw.sparqlqc.analysis.dataset;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.beast.rdfstream.RdfGroupBy;
import org.aksw.beast.viz.xchart.XChartStatBarChartProcessor;
import org.aksw.beast.vocabs.CV;
import org.aksw.beast.vocabs.OWLTIME;
import org.aksw.iguana.vocab.IguanaVocab;
import org.aksw.jena_sparql_api.resources.sparqlqc.SparqlQcReader;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.expr.aggregate.AggAvg;
import org.apache.jena.sparql.expr.aggregate.lib.AccStatStdDevPopulation;
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

        OptionSpec<String> solverOs = parser
                .acceptsAll(Arrays.asList("s", "solver"), "The solver to use, one of: " + SparqlQcTools.solvers.keySet())
                .withRequiredArg()
                //.defaultsTo(null)
                ;

        OptionSet options = parser.parse(args);
	
        
        String filename = fileOs.value(options);
        
        filename = "saleem-swdf-benchmark.ttl";
        logger.info("Loading: " + filename);
        List<Resource> testCases = SparqlQcReader.loadTasksSqcf(filename);

        
        testCases = testCases.subList(0, 10);
        
        
        String methodLabel = "JSAC";
        Object solver = SparqlQcTools.solvers.get(methodLabel);
        
        
        Model metaModel = ModelFactory.createDefaultModel();
        Resource jsac = metaModel.createResource("http://ex.org/series/JSAC");
        metaModel.add(jsac, RDFS.label, "JSAC");
        
        logger.info("Got solver " + methodLabel + " " + solver);
        
//        Function<Resource, Callable<Boolean>> taskParser = (r) -> {
//            Callable<Boolean> task = SparqlQcPreparation.prepareTask(r, solver, false);
//            return task;
//        };
        
        Stream<Resource> observationStream =
        		FactoryBeanRdfBenchmarkRunner.create(TaskImpl.class)
        			.setMetaModel(metaModel)
        			.setMethodLabel((r) -> jsac) // Note: this could be a supplier which derives it from the workload
        			.setNumEvalRuns(1)
        			.setNumWarmupRuns(1)
        			.setTaskParser(r -> SparqlQcPreparation.prepareTask(r, solver, false))
        			.setTaskExecutor((o, t) -> t.call())
        			.setExpectedValueSupplier((r, t) -> t.getTestCase().getExpectedResult())
        			.setWorkload(testCases)
        			.run();
    
        // Chart specific post processing
        List<Resource> avgs =
        RdfGroupBy.enh()
            .on(CV.category) // Rename workload to category
            .on(CV.categoryLabel)
            .on(CV.series)
            .on(CV.seriesLabel)
            .agg(CV.value, OWLTIME.numericDuration, AggAvg.class)
            .agg(CV.stDev, OWLTIME.numericDuration, AccStatStdDevPopulation.class)
            .apply(observationStream)
//            .peek(g -> {
//            	g
//            		.addLiteral(CV.seriesLabel, o)
//            })
            //.map(g -> g.rename("http://ex.org/avg/query{0}-user{1}", IV.job, IV.thread, IV.thread))
            .map(g -> g.rename("http://ex.org/avg/query{0}-user{1}", CV.seriesLabel, CV.categoryLabel))
            .collect(Collectors.toList());

        avgs.forEach(o -> RDFDataMgr.write(System.out, o.getModel(), RDFFormat.TURTLE));


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

