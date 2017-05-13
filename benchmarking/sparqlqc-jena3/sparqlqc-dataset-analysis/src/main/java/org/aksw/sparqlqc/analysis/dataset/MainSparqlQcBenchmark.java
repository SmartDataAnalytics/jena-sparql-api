package org.aksw.sparqlqc.analysis.dataset;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.beast.rdfstream.RdfGroupBy;
import org.aksw.beast.viz.xchart.XChartStatBarChartProcessor;
import org.aksw.beast.vocabs.CV;
import org.aksw.beast.vocabs.IV;
import org.aksw.beast.vocabs.OWLTIME;
import org.aksw.beast.vocabs.QB;
import org.aksw.iguana.vocab.IguanaVocab;
import org.aksw.jena_sparql_api.resources.sparqlqc.SparqlQcReader;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.expr.aggregate.AggAvg;
import org.apache.jena.sparql.expr.aggregate.lib.AccStatStdDevPopulation;
import org.apache.jena.vocabulary.RDF;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.VectorGraphicsEncoder;
import org.knowm.xchart.VectorGraphicsEncoder.VectorGraphicsFormat;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inrialpes.tyrexmo.testqc.MainTestContain;
import fr.inrialpes.tyrexmo.testqc.SparqlQcPreparation;
import fr.inrialpes.tyrexmo.testqc.SparqlQcTools;
import fr.inrialpes.tyrexmo.testqc.Task;
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
        
        
        
        String methodLabel = "JSAC";
        Object solver = SparqlQcTools.solvers.get(methodLabel);
        
        
        logger.info("Got solver " + methodLabel + " " + solver);
        
        BiFunction<Resource, Object, Task> taskParser = (r, _solver) -> {
            Task task = SparqlQcPreparation.prepareTask(r, _solver, false);
            return task;
        };

        Stream<Resource> observationStream = MainTestContain.run(testCases, methodLabel, solver, taskParser);
        
//        observationStream.forEach(r -> {
//        	RDFDataMgr.write(System.out, r.getModel(), RDFFormat.TURTLE);
//        });
        
        

        List<Resource> observations = observationStream.filter(r -> r.hasProperty(RDF.type, QB.Observation)).collect(Collectors.toList());

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

        
        //Set<Resource> observations = overall.listResourcesWithProperty(RDF.type, QB.Observation).toSet();

        
        SparqlQcTools.destroy();
        logger.info("Done.");
        System.exit(0);
	}
}
