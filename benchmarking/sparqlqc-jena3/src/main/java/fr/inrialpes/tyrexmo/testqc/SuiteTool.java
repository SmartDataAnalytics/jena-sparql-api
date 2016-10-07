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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutorService;
import java.lang.Boolean;
import java.lang.InstantiationException;
import java.lang.IllegalAccessException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;

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
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.Container;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.impl.RDFDefaultErrorHandler;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.DC;

public class SuiteTool {
    final static Logger logger = LoggerFactory.getLogger( SuiteTool.class );

    protected Options options = null;

    protected String testSuiteFile = null;

    protected String outputType = "html";

    protected String outputFile = null;

    protected PrintStream stream = null;

    public SuiteTool() {
	options = new Options();
	options.addOption( "h", "help", false, "Print this page" );
	options.addOption( OptionBuilder.withLongOpt( "output" ).hasArg().withDescription( "Result FILE" ).withArgName("FILE").create( 'o' ) );
	options.addOption( OptionBuilder.withLongOpt( "format" ).hasArg().withDescription( "Output format [NIY]" ).withArgName("TYPE (asc|plot)").create( 'f' ) );
	// xml|csv|html
    }

    /**
     * Usage TestContain SolverClass Q1 Q2
     * Usage TestContain SolverClass -s Schema Q1 Q2
     * Usage TestContain SolverClass -d testDirectory -t outputType (csv|html|xml)
     * Corrently implemented:
     * TestContain SolverClass Q1 Q2
     */
    public static void main( String[] args ) throws Exception {
	new SuiteTool().run( args );
    }

    public void run ( String [] args ) throws Exception, IOException {
	// Read parameters
	String[] argList = null;
	try { 
	    CommandLineParser parser = new PosixParser();
	    CommandLine line = parser.parse( options, args );
	    if ( line.hasOption( 'h' ) ) { usage(); System.exit( 0 ); }
	    if ( line.hasOption( 'f' ) ) outputType = line.getOptionValue( 'f' );
	    if ( line.hasOption( 'o' ) ) outputFile = line.getOptionValue( 'o' );
	    argList = line.getArgs();
	    if ( argList.length < 1 ) {
		logger.error( "Usage: TestContain SolverClass Q1 Q2" );
		usage();
		System.exit( -1 );
	    }
	} catch( ParseException exp ) {
	    logger.error( exp.getMessage() );
	    usage();
	    System.exit(-1);
	}
	testSuiteFile = argList[0];

	// Set output file
	if ( outputFile == null ) {
	    stream = System.out;
	} else {
	    stream = new PrintStream( new FileOutputStream( outputFile ) );
	}

	render( testSuiteFile );
    }

    public void usage() {
	Package pkg = this.getClass().getPackage();
	new HelpFormatter().printHelp( 80, pkg+" [options] testSuiteFile\nRenders a test suite", "\nOptions:", options, "" );
    }

    File directory = null;

    /**
     * Display the test suite in HTML
     */
    public void render( String suiteFile ) throws Exception {
	directory = new File( suiteFile ).getParentFile();
	//System.err.println( "DIR: "+sf );
	Model suite = ModelFactory.createDefaultModel();
	try {
	    suite.read( new FileInputStream( suiteFile ), null );
	} catch (Exception ex) {
	    throw new Exception( "Cannot parse suite file", ex );
	}
	printHTMLHeader();
	printHTMLContent( suite );
	printHTMLFooter();
	suite.close(); // JE: I am not sure that I will not have trouble with initSyntax
    }

    public void printHTMLHeader() {
	stream.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
	stream.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">");
	stream.println("<head>");
	stream.println("<title>SPARQL Containment Benchmark</title>");
	stream.println("<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />");
	stream.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" media=\"screen\" />");
	stream.println();
	stream.println("    <link href=\"bootstrap/css/bootstrap.css\" rel=\"stylesheet\">");
	stream.println("    <link href=\"bootstrap/css/bootstrap-responsive.css\" rel=\"stylesheet\">");
	stream.println("    <link href=\"bootstrap/css/prettify.css\" rel=\"stylesheet\">");
	stream.println("    <link href=\"bootstrap/css/docs.css\" rel=\"stylesheet\">");
	stream.println();
	stream.println("</head>");
	stream.println("<body>");
	stream.println("<div id=\"centerColumn\">");
	stream.println("  <div id=\"header\">");
	stream.println("    <h1>SPARQL Query Containment Benchmark</h1>");
	stream.println("        <h2><a href=\"http://exmo.inrialpes.fr/\">Exmo</a> &#38; <a href=\"http://tyrex.inria.fr/\">Tyrex</a>, <a href=\"http://www.inria.fr\">INRIA</a></h2>");
	stream.println("  </div>");
	stream.println("  <br />");
	stream.println();
	stream.println("  <hr />");
	stream.println("  <div id=\"nav\">");
	stream.println("    <ul>");
	stream.println("      <li><a href=\"index.html\"><b>Home</b></a></li>");
	stream.println("      <li><a href=\"benchmark.html\"><b>Benchmark</b></a></li>");
	stream.println("      <li><a href=\"download.html\"><b>Download</b></a></li>");
	stream.println("      <li><a href=\"about.html\"><b>About</b></a></li>");
	stream.println("    </ul>");
	stream.println("  </div>");
	stream.println("  <hr/>");
	stream.println();
    }

    public void printHTMLContent( Model suite ) throws Exception {
	Resource TESTSUITE = suite.createResource( "http://sparql-qc-bench.inrialpes.fr/testsuite#TestSuite" );
	Resource DIR = suite.createProperty( "http://sparql-qc-bench.inrialpes.fr/testsuite#sourceDir" );
	Resource HASTEST = suite.createProperty( "http://sparql-qc-bench.inrialpes.fr/testsuite#hasTest" );
	Resource CTNTTEST = suite.createResource( "http://sparql-qc-bench.inrialpes.fr/testsuite#ContainmentTest" );
	Resource WARMTEST = suite.createResource( "http://sparql-qc-bench.inrialpes.fr/testsuite#WarmupContainmentTest" );
	Resource SRQ = suite.createProperty( "http://sparql-qc-bench.inrialpes.fr/testsuite#sourceQuery" );
	Resource TAR = suite.createProperty( "http://sparql-qc-bench.inrialpes.fr/testsuite#targetQuery" );
	Resource SCH = suite.createProperty( "http://sparql-qc-bench.inrialpes.fr/testsuite#rdfSchema" );
	Resource RES = suite.createProperty( "http://sparql-qc-bench.inrialpes.fr/testsuite#result" );
	// Suite node
	StmtIterator stmtIt = suite.listStatements(null, RDF.type, TESTSUITE);
	// Take the first one if it exists
	if ( !stmtIt.hasNext() ) throw new Exception("Bad test suite specification");
	Resource node = stmtIt.nextStatement().getSubject();
	//System.err.println( " I got the suite "+node );
	String dir = directory.toString()+File.separator;
	if ( node.hasProperty( (Property)DIR ) ) {
	    RDFNode dd = node.getProperty( (Property)DIR ).getObject();
	    if ( !dd.isLiteral() ) throw new Exception( "Source directory must be a directory" );
	    dir += ((Literal)dd).getString()+File.separator;
	    //System.err.println( " SRCDR="+directory );
	}
	String name = node.getLocalName();
	String label = node.getProperty( RDFS.label ).getString();
	String comment = node.getProperty( RDFS.comment ).getString();
	stream.println("  <h2>"+name+" : "+label+"</h2>");
	stream.println("  <p>"+comment+"</p>");
	stream.println("  <div class=\"accordion\" id=\"accordion2\">" );

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
			if ( !WARMTEST.equals( rr.getProperty(RDF.type).getResource() ) )
			    try { //sourceQuery - targetQuery - result
				String tname = rr.getURI().toString();
				tname = tname.substring( tname.lastIndexOf( '#' )+1 );
				Statement st = rr.getProperty((Property)SRQ);
				if ( st == null ) throw new Exception("Test must contain a source query");
				String q1 = st.getString();
				final String src = dir+q1;
				st = rr.getProperty((Property)TAR);
				if ( st == null ) throw new Exception("Test must contain a target query");
				final String q2 = st.getString();
				final String tgt = dir+q2;
				st = rr.getProperty((Property)SCH);
				String sch = null;
				String schid = null;
				if ( st != null ) {
				    schid = st.getString();
				    sch = dir+schid+".rdfs";
				}
				st = rr.getProperty((Property)RES);
				if ( st == null ) throw new Exception("Test must contain an expected result");
				final String exp = st.getString();
				stream.println("            <div class=\"accordion-group\">");
				stream.println("              <div class=\"accordion-heading\">");
				stream.println("                <a class=\"accordion-toggle\" data-toggle=\"collapse\" data-parent=\"#accordion2\" href=\"#"+tname+"\">");
				stream.print("                  "+tname+" : ");
				if ( sch != null ) {
				    // Problem with the stylesheet
				    //stream.print("<a href=\""+sch+"\">"+schid+"</a> &models; ");
				    stream.print(schid+" &models; ");
				} else {
				    stream.print("&nbsp;&nbsp;&nbsp;&nbsp; ");
				}
				stream.println( q1+" &sqsube;  "+q2+" : "+exp+"</a><br />");
				stream.println("        </div>");
				stream.println("        <div id=\""+tname+"\" class=\"accordion-body in collapse\" style=\"height: auto;\">");
				stream.println("          <div class=\"accordion-inner\">");
				if ( sch != null ) {
				    stream.print("<center><a href=\""+sch+"\">"+schid+"</a> &models;</center>");
				}
				stream.println("            <table>");
				stream.println("              <thead>");
				stream.println("                <tr>");
				stream.println("                  <th style=\"width: 400px;\">"+q1+"</th>");
				stream.println("                  <th></th>");
				stream.println("                  <th style=\"width: 400px;\">"+q2+"</th>");
				stream.println("                </tr>");
				stream.println("              </thead>");
				stream.println("              <tbody>");
				stream.println("                <tr>");
				stream.println("                  <td>");
				printHTMLQuery( src );
				stream.println("                  </td>");
				stream.println("                  <td></td>");
				stream.println("                  <td>");
				printHTMLQuery( tgt );
				stream.println("                  </td>");
				stream.println("                </tr>");
				stream.println("              </tbody>");
				stream.println("            </table>");
				stream.println("          </div>");
				stream.println("        </div>");
				stream.println("      </div>");
			    } catch ( Exception ae ) {
				logger.debug( "IGNORED Exception", ae );
			    };
			coll = coll.getProperty( RDF.rest ).getResource();
		    }
		    stream.println("    </div>");
		}
	    }
	}
    }

    public void printHTMLQuery( String filename) {
	stream.println("                        <pre>");
	try {
	    FileInputStream in = new FileInputStream( filename );
	    BufferedReader reader = new BufferedReader( new InputStreamReader(in) );
	    String line = null;
	    while ((line = reader.readLine()) != null) {
		stream.println(line);
	    }
	} catch (IOException ex) {
	    stream.println( "Cannot open query "+filename );
	    logger.warn( "Cannot open file", ex );
	}
	stream.println("                        </pre>");
    }

    public void printHTMLFooter() {
	stream.println("    <!-- footer -->");
	stream.println("   <div id=\"footer\" style=\"font-size: +2\"></div>");
	stream.println();
	stream.println("   Generated on "+(new Date()).toString()+" with SuiteTool." );
	stream.println("    <script src=\"bootstrap/js/jquery.js\" type=\"text/javascript\"></script>");
	stream.println("    <script src=\"bootstrap/js/bootstrap-collapse.js\" type=\"text/javascript\"></script>");
	stream.println("    <script src=\"bootstrap/js/bootstrap.min.js\" type=\"text/javascript\"></script>");
	stream.println("    <script type=\"text/javascript\">");
	stream.println("       $(document).ready(function() {");
	stream.println("          // popover demo");
	stream.println("        $(\"a[rel=popover]\")");
	stream.println("                .popover()");
	stream.println("                .click(function(e) {");
	stream.println("                e.preventDefault()");
	stream.println("        });");
	stream.println("        });");
	stream.println("    </script>");
	stream.println();
	stream.println("</body></html>");
    }

}

