package org.aksw.sparqlqc.analysis.dataset;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.resources.sparqlqc.SparqlQcReader;
import org.aksw.jena_sparql_api.resources.sparqlqc.SparqlQcVocab;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inrialpes.tyrexmo.testqc.SparqlQcTools;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

public class MainSparqlQcGenerateBlacklist {
	
	private static final Logger logger = LoggerFactory.getLogger(MainSparqlQcGenerateBlacklist.class);
	
	/**
	 * Pass each task of the benchmark to the solver and see if it can be solved within the given time limit
	 * @throws IOException 
	 * @throws InvalidSyntaxException 
	 * @throws BundleException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, BundleException, InvalidSyntaxException, InterruptedException {
		
        OptionParser parser = new OptionParser();

        SparqlQcTools.init();

        OptionSpec<String> cmdOs = parser
                .acceptsAll(Arrays.asList("c", "cmd"), "The benchmark command")
                .withRequiredArg()
                ;

        OptionSpec<String> fileOs = parser
                .acceptsAll(Arrays.asList("f", "file"), "The benchmark file")
                .withRequiredArg()
                ;

        OptionSpec<String> solverOs = parser
                .acceptsAll(Arrays.asList("s", "solver"), "The solver to use, one of: " + SparqlQcTools.solvers.keySet())
                .withRequiredArg()
                ;

        OptionSpec<Long> timeoutInMsOs = parser
                .acceptsAll(Arrays.asList("t", "timeout"), "The timeout in milliseconds")
                .withRequiredArg()
                .ofType(Long.class)
                //.defaultsTo(null)
                ;
        
        OptionSet options = parser.parse(args);
	
        
        String filename = fileOs.value(options);

        String cmd = cmdOs.value(options);
        
        logger.info("Loading: " + filename);
        List<Resource> testCases = SparqlQcReader.loadTasksSqcf(filename);

        
        //testCases = testCases.subList(0, 10);
        
        
        String solverLabel = solverOs.value(options);
        //Object solver = SparqlQcTools.solvers.get(solverLabel);

        long timeoutInMs = timeoutInMsOs.value(options);
    
        
        
        for(Resource testCase : testCases) {
        	Statement s1 = testCase.getProperty(SparqlQcVocab.sourceQuery);
        	Statement s2 = testCase.getProperty(SparqlQcVocab.targetQuery);
        	Statement expectedResultStmt = testCase.getProperty(SparqlQcVocab.result);
        	
        	if(s1 == null || s2 == null) {
        		continue;
        	}
        	
        	String q1 = s1.getString();
        	String q2 = s2.getString();
        	
        	boolean expectedResult = expectedResultStmt != null ? expectedResultStmt.getBoolean() : false;
        	
        	try {
	        	ProcessBuilder pb = new ProcessBuilder(cmd, "-s", solverLabel, "-q1", q1, "-q2", q2, "-e", "" + expectedResult);
	        	Process p = pb.start();
	        	boolean isDone = p.waitFor(timeoutInMs, TimeUnit.SECONDS);
	        	if(isDone) {
	        		logger.info("Test case " + testCase + " finished in time");
	        	} else {	        	
	        		logger.info("Blacklisting: " + testCase);
	        		p.destroy();
	        		p.waitFor(1, TimeUnit.SECONDS);
	        		if(p.isAlive()) {
	        			p.destroyForcibly();
	        		}
	        	}
        	} catch(Exception e) {
        		logger.warn("Exception encountered", e);
        	}
        }
        
        SparqlQcTools.destroy();
	}

}
