/*
 * Copyright (C) INRIA, 2012-2013
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

package fr.inrialpes.tyrexmo.testqc;

import java.io.IOException;
import java.io.StringReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutorService;
import java.lang.Boolean;
import java.lang.InstantiationException;
import java.lang.IllegalAccessException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;

// Yes we are relying on Jena for parsing RDF
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.Container;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.impl.RDFDefaultErrorHandler;
import com.hp.hpl.jena.vocabulary.RDF;

public class TestContain {
    final static Logger logger = LoggerFactory.getLogger( TestContain.class );

	private String axiom;
	private Collection<String> axioms;
	private boolean test_under_axioms = false;

    protected Class<?> solverClass = null;
    protected Constructor solverConstructor = null;

    protected Options options = null;

    protected String testDir = null;

    protected String schemaFile = null;

    protected String testSuiteFile = null;

    protected String testName = null;

    protected boolean warmup = false;

    protected int timeOut = 5000; // milliseconds

    protected String outputType = "asc";

    protected String outputFile = null;

    protected PrintStream stream = null;

    protected ContainmentSolver solver = null;

    public TestContain() {
	options = new Options();
	options.addOption( "h", "help", false, "Print this page" );
	options.addOption( "w", "warmup", false, "Run the test with a warmup test" );
	options.addOption( OptionBuilder.withLongOpt( "directory" ).hasArg().withDescription( "Use the content of the DIRectory as test suite" ).withArgName("DIR").create( 'd' ) );
	options.addOption( OptionBuilder.withLongOpt( "schema" ).hasArg().withDescription( "RDF Schema FILE" ).withArgName("FILE").create( 's' ) );
	options.addOption( OptionBuilder.withLongOpt( "test-suite" ).hasArg().withDescription( "Test suite description FILE" ).withArgName("FILE").create( 'x' ) );
	options.addOption( OptionBuilder.withLongOpt( "output" ).hasArg().withDescription( "Result FILE" ).withArgName("FILE").create( 'o' ) );
	options.addOption( OptionBuilder.withLongOpt( "test-name" ).hasArg().withDescription( "One test to evaluate" ).withArgName("NAME").create( 'n' ) );
	options.addOption( OptionBuilder.withLongOpt( "format" ).hasArg().withDescription( "Output format" ).withArgName("TYPE (asc|plot)").create( 'f' ) );
	// xml|csv|html
	options.addOption( OptionBuilder.withLongOpt( "timeout" ).hasArg().withDescription( "Timeout" ).withArgName("MILLISECONDS").create( 't' ) );
    }

    /**
     * Usage TestContain SolverClass Q1 Q2
     * Usage TestContain SolverClass -s Schema Q1 Q2
     * Usage TestContain SolverClass -d testDirectory -t outputType (csv|html|xml)
     * Corrently implemented:
     * TestContain SolverClass Q1 Q2
     */
    public static void main( String[] args ) throws Exception {
/*
        JenaSystem.init();
        InitJenaCore.init();
        ARQ.init();
*/

        new TestContain().run( args );
    }

    public void run ( String [] args ) throws Exception, IOException {
	// Read parameters
	String[] argList = null;
	try {
	    CommandLineParser parser = new PosixParser();
	    CommandLine line = parser.parse( options, args );
	    if ( line.hasOption( 'h' ) ) { usage(); System.exit( 0 ); }
	    if ( line.hasOption( 'w' ) ) { warmup = true; }
	    if ( line.hasOption( 'd' ) ) testDir = line.getOptionValue( 'd' );
	    if ( line.hasOption( 's' ) ) schemaFile = line.getOptionValue( 's' );
	    if ( line.hasOption( 'x' ) ) testSuiteFile = line.getOptionValue( 'x' );
	    if ( line.hasOption( 'n' ) ) testName = line.getOptionValue( 'n' );
	    if ( line.hasOption( 'f' ) ) outputType = line.getOptionValue( 'f' );
	    if ( line.hasOption( 'o' ) ) outputFile = line.getOptionValue( 'o' );
	    if ( line.hasOption( 't' ) ) timeOut = Integer.parseInt( line.getOptionValue( 't' ) );
	    argList = line.getArgs();
	    if ( ( argList.length < 3 && testSuiteFile == null ) || argList.length < 1 ) {
		logger.error( "Usage: TestContain SolverClass Q1 Q2" );
		usage();
		System.exit( -1 );
	    }
	} catch( ParseException exp ) {
	    logger.error( exp.getMessage() );
	    usage();
	    System.exit(-1);
	}

	String solverClassName = argList[0];
	// Gather solver class
	try {
	    solverClass = Class.forName( solverClassName );
	    Class[] cparams = {};
	    solverConstructor = solverClass.getConstructor(cparams);
	} catch ( Exception ex ) {
	    throw ex;
	}

	// Set output file
	if ( outputFile == null ) {
	    stream = System.out;
	} else {
	    stream = new PrintStream( new FileOutputStream( outputFile, true ) );
	}

	if ( testSuiteFile != null && outputType.equals("shell") ) {
	    generateShell( testSuiteFile, solverClassName );
	    return;
	}

	// Instantiate solver (does not work for reentrance reasons)
	/*
	try {
	    Object[] mparams = {};
	    solver = (ContainmentSolver)solverConstructor.newInstance( mparams );
	    logger.info( "Solver created: {}", solver );
	    if ( warmup ) solver.warmup();
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit( -1 );
	}
	*/


	Vector<Result> results = null;
	if ( testSuiteFile != null ) {
	    results = testSuite( testSuiteFile );
	    displayResults( results ); //later use the format
	} else if ( argList.length > 2 ) {
	    String f1 = argList[1];
	    String f2 = argList[2];
	    Result r = testOneContainment( (String)null, f1, f2 );
	    logger.debug( "Answer : {} [Time: {}ms]", r.answer, r.time );
	    if ( r.status == 0 ) {
		if ( r.answer ) System.err.println( "    CONTAINED ["+r.time+"ms]" );
		else System.err.println( "NON CONTAINED ["+r.time+"ms]" );
	    } else if ( r.status == -2 ) {
		System.err.println( "** TIMEOUT **" );
		System.exit(-1);
	    } else {
		System.err.println( "** ERROR **" );
	    }
	} else { // error
	    logger.warn( "Something went wrong" );
	}
	System.exit(0);
    }

    public void usage() {
	    Package pkg = this.getClass().getPackage();
	    new HelpFormatter().printHelp( 80, pkg+" [options] solverClass query1 query2\nTests query containment", "\nOptions:", options, "" );
    }

    public Vector<Result> testSuite( String suiteFile ) throws Exception {
		int n = 100;


	String dir = null;
	File sf = new File( suiteFile ).getParentFile();
	//System.err.println( "DIR: "+sf );
	Vector<Result> results = new Vector<Result>();
	Model suite = ModelFactory.createDefaultModel();
	Resource TESTSUITE = suite.createResource( "http://sparql-qc-bench.inrialpes.fr/testsuite#TestSuite" );
	Resource DIR = suite.createProperty( "http://sparql-qc-bench.inrialpes.fr/testsuite#sourceDir" );
	Resource HASTEST = suite.createProperty( "http://sparql-qc-bench.inrialpes.fr/testsuite#hasTest" );
	Resource CTNTTEST = suite.createResource( "http://sparql-qc-bench.inrialpes.fr/testsuite#ContainmentTest" );
	Resource WARMTEST = suite.createResource( "http://sparql-qc-bench.inrialpes.fr/testsuite#WarmupContainmentTest" );
	Resource SRQ = suite.createProperty( "http://sparql-qc-bench.inrialpes.fr/testsuite#sourceQuery" );
	Resource TAR = suite.createProperty( "http://sparql-qc-bench.inrialpes.fr/testsuite#targetQuery" );
	Resource SCH = suite.createProperty( "http://sparql-qc-bench.inrialpes.fr/testsuite#rdfSchema" );
	Resource RES = suite.createProperty( "http://sparql-qc-bench.inrialpes.fr/testsuite#result" );
	try {
	    suite.read( new FileInputStream( suiteFile ), null );
	} catch (Exception ex) {
	    throw new Exception( "Cannot parse suite file", ex );
	}
	// Suite node
	StmtIterator stmtIt = suite.listStatements(null, RDF.type, TESTSUITE);
	// Take the first one if it exists
	if ( !stmtIt.hasNext() ) throw new Exception("Bad test suite specification");
	Resource node = stmtIt.nextStatement().getSubject();
	//System.err.println( " I got the suite "+node );
	if ( node.hasProperty( (Property)DIR ) ) {
	    RDFNode dd = node.getProperty( (Property)DIR ).getObject();
	    if ( !dd.isLiteral() ) throw new Exception( "Source directory must be a directory" );
	    dir = new File( sf, ((Literal)dd).getString() ).toString()+File.separator;
	    //System.err.println( " SRCDR="+dir );
	}
	if ( node.hasProperty( (Property)HASTEST ) ) {
	    Object o = node.getProperty( (Property)HASTEST ).getObject();
	    //System.err.println( " Got HASTEST statement" );
	    if ( o instanceof Resource) {
		Resource coll = (Resource)o;
		if ( coll != null ) {
		    while ( !RDF.nil.getURI().equals( coll.getURI() ) ) {
			// Do something with
			Resource rr = coll.getProperty( RDF.first ).getResource();
			//System.err.println( rr );
			if ( WARMTEST.equals(rr.getProperty(RDF.type).getResource())
			     || testName == null
			     || rr.getURI().toString().endsWith( testName ) ) {
				//int m = 0;
				double totalTime = 0.0;
				Result r = null;

				for(int i = 0; i < n; ++i) {
//					if(n == 100){
//						n = 0;
//						break;
//					}
//
			    try { //sourceQuery - targetQuery - result
				Statement st = rr.getProperty((Property)SRQ);
				if ( st == null ) throw new Exception("Test must contain a source query");
				final String src = dir+st.getString();
				st = rr.getProperty((Property)TAR);
				if ( st == null ) throw new Exception("Test must contain a target query");
				final String tgt = dir+st.getString();
				st = rr.getProperty((Property)SCH);
				String sch = null;
				if ( st != null ) sch = dir+st.getString();
				st = rr.getProperty((Property)RES);
				if ( st == null ) throw new Exception("Test must contain an expected result");
				final boolean exp = Boolean.parseBoolean( st.getString() );
//				logger.info( "Test : "+src+" < "+tgt+" ("+exp+")" );
				r = testOneContainment( sch, src, tgt );
				//System.out.println(src + " - " + tgt);
				//System.out.println("answer: " + r.answer + " - expected: " + exp);
				totalTime += r.time;

				//Result r = testContainmentWithTimeOut( src, tgt, 1 );
				r.name = rr.toString();
				r.expected = exp;
//				logger.info( "Answer({}) : {} [Time: {}ms]", r.status, r.answer, r.time );
				Resource tp = rr.getProperty(RDF.type).getResource();
			    } catch ( Exception ae ) {
			    	ae.printStackTrace();
				logger.warn( "IGNORED Exception", ae );
				break;
			    };

				}
				double avgTime = totalTime / (double)n;
				if(r != null) {
				r.time = avgTime;
				System.out.println("Avg: " + avgTime);
				if ( CTNTTEST.equals(rr.getProperty(RDF.type).getResource()) ) results.add( r );
				}

		    }


			coll = coll.getProperty( RDF.rest ).getResource();

		    }
		}
	    }
	}
	suite.close(); // JE: I am not sure that I will not have trouble with initSyntax
	return results;
    }

    private ExecutorService executor = null;

    public Result testOneContainment( String schema, String qFile1, String qFile2 ) throws Exception {
	if ( qFile1 != null && qFile2 != null ) {
	    //try {
		Query query1 = QueryFactory.read( qFile1 );
		Query query2 = QueryFactory.read( qFile2 );
		System.gc(); // run garbage collector before
		executor = Executors.newFixedThreadPool( 1 );
		//long start = System.currentTimeMillis();
		Result r = testContainmentWithTimeOut( schema, query1, query2, timeOut );
		//Result r = testContainment( query1, query2 );
		//long end = System.currentTimeMillis();
		//r.time = (end - start);
		return r;
//	    } catch ( Exception ex ) {
//		logger.warn( "Incorrect execution", ex );
//		return new Result( -1 );
//	    }
	} else throw new Exception( "Need two queries to test" );
    }

    public synchronized Result testContainmentWithTimeOut( final String schema, final Query q1, final Query q2, int timeOutMS ) throws InterruptedException, ExecutionException, TimeoutException {
	final Future<Result> future =
	    executor.submit( new Callable<Result>() {
		    // There is no way to shut down such a thread...
		    //public void stop() { };
		    public void interrupt() { return; }
		    public Result call() {
//		    	try {
//					Thread.sleep(10000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			return testContainment( schema, q1, q2 );
		    }
		});
	try {
	    return future.get( timeOutMS, TimeUnit.MILLISECONDS );
		//return future.get();
//	} catch ( Exception ex ) {
	    //throw new RuntimeException(ex);
//	    executor.shutdownNow();
//	    return new Result( -2 );
//	} catch ( Throwable ex ) {
//	    logger.warn( "Incorrect execution", ex );
//	    future.cancel( true );
//	    executor.shutdownNow();
//	    return new Result( -1 );
	} catch (InterruptedException | ExecutionException | TimeoutException e) {
	    future.cancel( true ); // this should interrupt the call
	    throw e;
	} finally {
	    executor.shutdownNow();
	    try {
		solver.cleanup();
	    } catch ( ContainmentTestException ctex ) {};
	}
    }

    public Result testContainment( String schema, Query q1, Query q2 ) {
	try {
	    Object[] mparams = {};
	    solver = (ContainmentSolver)solverConstructor.newInstance(mparams);
	    //logger.info( "Solver created: {}", solver );

		long startTime = System.nanoTime();

	    boolean verdict = schema != null
	    		? solver.entailedUnderSchema( schema, q1, q2 )
				: solver.entailed( q1, q2 );

	    long endTime = System.nanoTime();
	    double elapsedTime = (endTime - startTime) / 1000000.0;
	    //System.out.println("ela: " + elapsedTime);

	    return new Result(verdict, elapsedTime);

	} catch ( InstantiationException iex ) { // We should distinguish between error types
	    return new Result( -1 );
	} catch ( IllegalAccessException iaex ) {
	    return new Result( -1 );
	} catch ( InvocationTargetException itex ) {
	    return new Result( -1 );
	} catch ( ContainmentTestException ctex ) {
	    logger.debug( "Raised CTEX", ctex );
	    throw new RuntimeException(ctex);
	    //return new Result( -1 );
	} catch ( Throwable ex ) {
	    //System.err.println( ex.getMessage() );
	    //ex.printStackTrace();
	    logger.error( "Got that error", ex );
	    return new Result( -1 );
	    //throw new RuntimeException( ex );
	}
    }

    public void displayResults( Vector<Result> results ) {
	if ( outputType.equals( "asc" ) ) displayResultsAsc( results );
	else if ( outputType.equals( "plot" ) ) displayResultsPlot( results );
    }
    public void displayResultsAsc( Vector<Result> results ) {
	double time = 0.;
	int score = 0;
	//for ( Iterator<Result> it = results.iterator() ; it.hasNext(); ) {
	for ( Result rr : results ) {
	    //Result rr = it.next();
	    String display = rr.name.substring( rr.name.lastIndexOf('#')+1 )+"\t";
	    if ( rr.status == 0 ) {
		time += rr.time;
		if ( rr.expected == rr.answer ) { score++; display += "CORRECT";}
		else { display += "INCORRECT"; }
		display += "\t"+rr.time;
	    } else if ( rr.status == -1 ) {
		display += "*ERROR*";
	    } else if ( rr.status == -2 ) {
		display += "*TIMEOUT ("+timeOut+"ms)*";
	    }
	    stream.println( display );
	}
	System.out.println( "Total score : "+score+"/"+results.size() );
	System.out.println( "Total time : "+time+"ms" );
    }
    public void displayResultsPlot( Vector<Result> results ) {
	//for ( Iterator<Result> it = results.iterator() ; it.hasNext(); ) {
	for ( Result rr : results ) {
	    String display = rr.name.substring( rr.name.lastIndexOf('#')+1 )+"\t";
	    if ( rr.status == 0 ) {
		display += rr.time;
	    }
	    stream.println( display );
	}
    }

    public void generateShell( String suiteFile, String solverClassName ) throws Exception {
	String CommandCall = "java -Xms1512m -Xmx2024m -Djava.library.path=lib -jar lib/containmenttester.jar "+solverClassName+" ";
	if ( warmup ) CommandCall += "-w ";
	String dir = null;
	File sf = new File( suiteFile ).getParentFile();
	//System.err.println( "DIR: "+sf );
	Vector<Result> results = new Vector<Result>();
	Model suite = ModelFactory.createDefaultModel();
	Resource TESTSUITE = suite.createResource( "http://sparql-qc-bench.inrialpes.fr/testsuite#TestSuite" );
	Resource DIR = suite.createProperty( "http://sparql-qc-bench.inrialpes.fr/testsuite#sourceDir" );
	Resource HASTEST = suite.createProperty( "http://sparql-qc-bench.inrialpes.fr/testsuite#hasTest" );
	Resource CTNTTEST = suite.createResource( "http://sparql-qc-bench.inrialpes.fr/testsuite#ContainmentTest" );
	Resource WARMTEST = suite.createResource( "http://sparql-qc-bench.inrialpes.fr/testsuite#WarmupContainmentTest" );
	Resource SRQ = suite.createProperty( "http://sparql-qc-bench.inrialpes.fr/testsuite#sourceQuery" );
	Resource TAR = suite.createProperty( "http://sparql-qc-bench.inrialpes.fr/testsuite#targetQuery" );
	Resource RES = suite.createProperty( "http://sparql-qc-bench.inrialpes.fr/testsuite#result" );
	try {
	    suite.read( new FileInputStream( suiteFile ), null );
	} catch (Exception ex) {
	    throw new Exception( "Cannot parse suite file", ex );
	}
	// Suite node
	StmtIterator stmtIt = suite.listStatements(null, RDF.type, TESTSUITE);
	// Take the first one if it exists
	if ( !stmtIt.hasNext() ) throw new Exception("Bad test suite specification");
	Resource node = stmtIt.nextStatement().getSubject();
	//System.err.println( " I got the suite "+node );
	if ( node.hasProperty( (Property)DIR ) ) {
	    RDFNode dd = node.getProperty( (Property)DIR ).getObject();
	    if ( !dd.isLiteral() ) throw new Exception( "Source directory must be a directory" );
	    dir = new File( sf, ((Literal)dd).getString() ).toString()+File.separator;
	    //System.err.println( " SRCDR="+dir );
	}
	if ( node.hasProperty( (Property)HASTEST ) ) {
	    Object o = node.getProperty( (Property)HASTEST ).getObject();
	    //System.err.println( " Got HASTEST statement" );
	    if ( o instanceof Resource) {
		Resource coll = (Resource)o;
		if ( coll != null ) {
		    while ( !RDF.nil.getURI().equals( coll.getURI() ) ) {
			// Do something with
			Resource rr = coll.getProperty( RDF.first ).getResource();
			//System.err.println( rr );
			if ( !WARMTEST.equals(rr.getProperty(RDF.type).getResource()) )
			    try { //sourceQuery - targetQuery - result
				Statement st = rr.getProperty((Property)SRQ);
				if ( st == null ) throw new Exception("Test must contain a source query");
				final String src = dir+st.getString();
				st = rr.getProperty((Property)TAR);
				if ( st == null ) throw new Exception("Test must contain a target query");
				final String tgt = dir+st.getString();
				st = rr.getProperty((Property)RES);
				if ( st == null ) throw new Exception("Test must contain an expected result");
				final boolean exp = Boolean.parseBoolean( st.getString() );
				stream.println( "echo '--------------------------------------------'");
				stream.println( "echo "+rr.toString());
				stream.println( "echo 'Test : "+src+" < "+tgt+" =======> "+exp+"'" );
				//System.out.println( CommandCall+src+" "+tgt );
				String name = rr.toString();
				stream.println( CommandCall+"-x "+suiteFile+" -n "+ name.substring( name.lastIndexOf('#')+1 )+" -f plot -o results.tsv");
			    }
			    catch ( Exception ae ) {
				logger.debug( "IGNORED Exception", ae );
			    }
			coll = coll.getProperty( RDF.rest ).getResource();
		    }
		}
	    }
	}
	stream.println( "echo 'Results are in results.tsv'" );
	suite.close(); // JE: I am not sure that I will not have trouble with initSyntax
    }

    class Result {
	public String name;
	public int status = -1;
	public double time;
	public boolean answer;
	public boolean expected;

	public Result( int s ) { status = s; }
	public Result( boolean r, double t ) { answer = r; time = t; status = 0; }
    }

}

