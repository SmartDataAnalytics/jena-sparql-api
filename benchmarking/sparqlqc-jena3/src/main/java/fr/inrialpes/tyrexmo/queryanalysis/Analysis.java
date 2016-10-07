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

package fr.inrialpes.tyrexmo.queryanalysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.IOException;
import java.io.File;
import java.net.URI;

import org.apache.jena.sparql.core.Var;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;

public class Analysis {
    final static Logger logger = LoggerFactory.getLogger( Analysis.class );

    protected Options options = null;

    protected String queryDir = null;

    protected String outputType = "html";

    protected String outputFile = null;

    protected PrintStream stream = null;

    static final int NONE = 0; // means not even a and... (triple pattern)
    static final int AND = 1; // means only and (basic graph pattern)
    static final int UNION = 2; // means UNION and AND (UCQ)
    static final int OPT = 3;
    static final int FILTER = 4;
    static final int UNION_OPT = 5;
    static final int OPT_FILTER = 6;
    static final int FILTER_UNION = 7;
    static final int UNION_OPT_FILTER = 8;

    static final int CYCLE = 0;
    static final int DAG = 1;
    static final int TREE = 2;

    static final int PROJ = 0;
    static final int NOPROJ = 1;

    int totalNumber = 0;
    int correctNumber = 0;
    int failure = 0;
    int ndVarCycles = 0;
    int[][][] resultArray = null;

    public Analysis() {
	resultArray = new int[NOPROJ+1][TREE+1][UNION_OPT_FILTER+1];
	for( int i = 0; i <= NOPROJ; i++ ) {
	    for( int j = 0; j <= TREE; j++ ) {
		for( int k = 0; k <= UNION_OPT_FILTER; k++ ) {
		    resultArray[i][j][k] = 0;
		}
	    }
	}
	options = new Options();
	options.addOption( "h", "help", false, "Print this page" );
	options.addOption( OptionBuilder.withLongOpt( "output" ).hasArg().withDescription( "Result FILE" ).withArgName("FILE").create( 'o' ) );
	options.addOption( OptionBuilder.withLongOpt( "format" ).hasArg().withDescription( "Output format [NIY]" ).withArgName("TYPE (asc|plot)").create( 'f' ) );
    }

    public static void main( String[] args ) throws Exception {
	new Analysis().run( args );
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
	queryDir = argList[0];

	// Set output file
	if ( outputFile == null ) {
	    stream = System.out;
	} else {
	    stream = new PrintStream( new FileOutputStream( outputFile ) );
	}

	File [] subdir = ( new File( queryDir ) ).listFiles();
	int size = subdir.length;
	for ( int i=0 ; i < size; i++ ) {
	    File queryFile = subdir[i];
	    if( queryFile.isFile() ) {
		logger.trace( queryFile.toString() );
		totalNumber++;
		try {
		    // ANALYSE
		    analyse( queryFile  );
		    correctNumber++;
		} catch ( Exception ex ) {
		    //System.err.println( queryFile.toString() );
		    //System.err.println( ex );
		    failure++;
		}
	    }
	}
	// RENDER
	render();
    }

    // Classical, in particular dbpedia, prefix that are often forgotten
    String missingPrefixes = "PREFIX dbpedia: <http://dbpedia.org/resource/>\nPREFIX dbpprop: <http://dbpedia.org/property/>\nPREFIX dbpedia-owl: <http://dbpedia.org/ontology/>\nPREFIX geo: <http://www.example.com/>\nPREFIX foaf: <http://xmlns.com/foaf/0.1/>\n\n";

    /**
     *  Read a query from a file
     * @param fname a file containing a SPARQL query
     * @return returns the query as a string
     * @throws IOException
     */
    public String read( String filename ) throws IOException {
	String qry = "";
	try {
	    BufferedReader in = new BufferedReader(new FileReader(filename));
	    String str;
	    while ((str = in.readLine()) != null) {
		qry += " "+str;
	    }
	    in.close();
	} catch ( IOException e ) {}
	return qry;
    }
    
    public void analyse( File qFile ) throws Exception {
	int projected;
	int cyclic;
	int constr;
	String queryString = read( qFile.toString() );
	queryString = missingPrefixes + queryString;
	Query query = QueryFactory.create( queryString );
	TransformAlgebra ta = new TransformAlgebra( queryString );
	/* Would be better if prefixes could be added
	Query query = QueryFactory.read( qFile.toString() );
	TransformAlgebra ta = new TransformAlgebra( query ); */
	CycleAnalysis cq = new CycleAnalysis( ta.getTriples() );
	// Projections
	if ( ta.getNonDistVars().size() != 0 ) projected = PROJ;
	else projected = NOPROJ;
	// Cycles
	if ( cq.isCyclic() ) cyclic = CYCLE;
	else if ( cq.isDAGATree() ) cyclic = TREE;
	else cyclic = DAG;
	// Constructors
	if ( ta.containsOpt() ) {
	    if ( ta.hasUnion() ) {
		if ( ta.hasFilter() ) {
		    constr = UNION_OPT_FILTER;
		} else {
		    constr = UNION_OPT;
		}
	    } else {
		if ( ta.hasFilter() ) {
		    constr = OPT_FILTER;
		} else {
		    constr = OPT;
		}
	    }
	} else {
	    if ( ta.hasUnion() ) {
		if ( ta.hasFilter() ) {
		    constr = FILTER_UNION;
		} else {
		    constr = UNION;
		}
	    } else {
		if ( ta.hasFilter() ) {
		    constr = FILTER;
		} else {
		    constr = NONE;
		}
	    }
	}
	/*
	System.err.println( queryString );
	System.err.print( "\nDistVars: ");
	for( Var v : ta.getResultVars() ) System.err.print( v+" " );
	System.err.println( "\nAllVars: ");
	for( Var v : ta.getAllVariables() ) System.err.print( v+" " );
	System.err.print( "\nDiff: ");
	for( Var v : ta.getNonDistVars() ) System.err.print( v+" " );
	System.err.println( "\nCyclic: "+cyclic+" Projection: "+projected+" Constructor: "+constr );
	*/
	// Cycles among non distinguished variables   			
	cq.constantsAndDvars.addAll( cq.convertFromVarToString( ta.getResultVars() ) );
	if ( cq.isThereAcycleAmongNDvars( ta.getNonDistVars() ) ) ndVarCycles++;
	(resultArray[projected][cyclic][constr])++;
    }

    public void renderOldStyle() {
	int number = resultArray[PROJ][TREE][UNION] + resultArray[PROJ][TREE][UNION_OPT] + resultArray[PROJ][TREE][FILTER_UNION] + resultArray[PROJ][TREE][UNION_OPT_FILTER] + resultArray[NOPROJ][TREE][UNION] + resultArray[NOPROJ][TREE][UNION_OPT] + resultArray[NOPROJ][TREE][FILTER_UNION] + resultArray[NOPROJ][TREE][UNION_OPT_FILTER];
	int perc = number*100/correctNumber;
	int total = number;
	stream.println("Tree UCQ\t"+number+"\t"+number*100/correctNumber );
	number = resultArray[PROJ][TREE][NONE] + resultArray[PROJ][TREE][OPT] + resultArray[PROJ][TREE][FILTER] + resultArray[PROJ][TREE][OPT_FILTER] + resultArray[NOPROJ][TREE][NONE] + resultArray[NOPROJ][TREE][OPT] + resultArray[NOPROJ][TREE][FILTER] + resultArray[NOPROJ][TREE][OPT_FILTER] ;
	perc += number*100/correctNumber;
	total += number;
	stream.println("Tree Others\t"+number+"\t"+number*100/correctNumber );
	number = resultArray[PROJ][DAG][UNION] + resultArray[PROJ][DAG][UNION_OPT] + resultArray[PROJ][DAG][FILTER_UNION] + resultArray[PROJ][DAG][UNION_OPT_FILTER] + resultArray[NOPROJ][DAG][UNION] + resultArray[NOPROJ][DAG][UNION_OPT] + resultArray[NOPROJ][DAG][FILTER_UNION] + resultArray[NOPROJ][DAG][UNION_OPT_FILTER];
	perc += number*100/correctNumber;
	total += number;
	stream.println("DAG UCQ\t\t"+number+"\t"+number*100/correctNumber );
	number = resultArray[PROJ][DAG][NONE] + resultArray[PROJ][DAG][OPT] + resultArray[PROJ][DAG][FILTER] + resultArray[PROJ][DAG][OPT_FILTER] + resultArray[NOPROJ][DAG][NONE] + resultArray[NOPROJ][DAG][OPT] + resultArray[NOPROJ][DAG][FILTER] + resultArray[NOPROJ][DAG][OPT_FILTER] ;
	stream.println("DAG Others\t"+number+"\t"+number*100/correctNumber );
	perc += number*100/correctNumber;
	total += number;
	number = resultArray[PROJ][CYCLE][NONE] + resultArray[PROJ][CYCLE][UNION] + resultArray[PROJ][CYCLE][OPT] + resultArray[PROJ][CYCLE][FILTER] + resultArray[PROJ][CYCLE][UNION_OPT] + resultArray[PROJ][CYCLE][OPT_FILTER] + resultArray[PROJ][CYCLE][FILTER_UNION] + resultArray[PROJ][CYCLE][UNION_OPT_FILTER] + resultArray[NOPROJ][CYCLE][NONE] + resultArray[NOPROJ][CYCLE][UNION] + resultArray[NOPROJ][CYCLE][OPT] + resultArray[NOPROJ][CYCLE][FILTER] + resultArray[NOPROJ][CYCLE][UNION_OPT] + resultArray[NOPROJ][CYCLE][OPT_FILTER] + resultArray[NOPROJ][CYCLE][FILTER_UNION] + resultArray[NOPROJ][CYCLE][UNION_OPT_FILTER];
	perc += number*100/correctNumber;
	total += number;
	stream.println("Cyclic\t\t"+number+"\t"+number*100/correctNumber );
	stream.println("TOTAL\t\t"+total+"\t"+perc+"\n" );
	stream.println( "\nNumber of queries with cycles using only ndvariables: "+ndVarCycles+"\n" );
    }

    public void render() {
	stream.println( "\n"+failure+" errors over "+totalNumber+" queries (residu: "+correctNumber+")\n" );
	renderOldStyle();
	int proj = resultArray[PROJ][CYCLE][NONE] + resultArray[PROJ][CYCLE][UNION] + resultArray[PROJ][CYCLE][OPT] + resultArray[PROJ][CYCLE][FILTER] + resultArray[PROJ][CYCLE][UNION_OPT] + resultArray[PROJ][CYCLE][OPT_FILTER] + resultArray[PROJ][CYCLE][FILTER_UNION] + resultArray[PROJ][CYCLE][UNION_OPT_FILTER] + resultArray[PROJ][DAG][NONE] + resultArray[PROJ][DAG][UNION] + resultArray[PROJ][DAG][OPT] + resultArray[PROJ][DAG][FILTER] + resultArray[PROJ][DAG][UNION_OPT] + resultArray[PROJ][DAG][OPT_FILTER] + resultArray[PROJ][DAG][FILTER_UNION] + resultArray[PROJ][DAG][UNION_OPT_FILTER] + resultArray[PROJ][TREE][NONE] + resultArray[PROJ][TREE][UNION] + resultArray[PROJ][TREE][OPT] + resultArray[PROJ][TREE][FILTER] + resultArray[PROJ][TREE][UNION_OPT] + resultArray[PROJ][TREE][OPT_FILTER] + resultArray[PROJ][TREE][FILTER_UNION] + resultArray[PROJ][TREE][UNION_OPT_FILTER];
	int noproj = resultArray[NOPROJ][CYCLE][NONE] + resultArray[NOPROJ][CYCLE][UNION] + resultArray[NOPROJ][CYCLE][OPT] + resultArray[NOPROJ][CYCLE][FILTER] + resultArray[NOPROJ][CYCLE][UNION_OPT] + resultArray[NOPROJ][CYCLE][OPT_FILTER] + resultArray[NOPROJ][CYCLE][FILTER_UNION] + resultArray[NOPROJ][CYCLE][UNION_OPT_FILTER] + resultArray[NOPROJ][DAG][NONE] + resultArray[NOPROJ][DAG][UNION] + resultArray[NOPROJ][DAG][OPT] + resultArray[NOPROJ][DAG][FILTER] + resultArray[NOPROJ][DAG][UNION_OPT] + resultArray[NOPROJ][DAG][OPT_FILTER] + resultArray[NOPROJ][DAG][FILTER_UNION] + resultArray[NOPROJ][DAG][UNION_OPT_FILTER] + resultArray[NOPROJ][TREE][NONE] + resultArray[NOPROJ][TREE][UNION] + resultArray[NOPROJ][TREE][OPT] + resultArray[NOPROJ][TREE][FILTER] + resultArray[NOPROJ][TREE][UNION_OPT] + resultArray[NOPROJ][TREE][OPT_FILTER] + resultArray[NOPROJ][TREE][FILTER_UNION] + resultArray[NOPROJ][TREE][UNION_OPT_FILTER];
	stream.println( "\t\tproj ("+proj+")\t\t\tnoproj ("+noproj+")" );
	stream.println( "\t\ttree\tdag\tcycle\ttree\tdag\tcycle" );
	boolean perc = false;
	printOneLine( "none\t", NONE, perc );
	//printOneLine( "and\t", NONE, perc );
	printOneLine( "union\t", UNION, perc );
	printOneLine( "opt\t", OPT, perc );
	printOneLine( "filter\t", FILTER, perc );
	printOneLine( "un-opt\t", UNION_OPT, perc );
	printOneLine( "opt-filt", OPT_FILTER, perc );
	printOneLine( "filt-un\t", FILTER_UNION, perc );
	printOneLine( "un-opt-filt", UNION_OPT_FILTER, perc );
	perc = true;
	stream.println( "\t\tproj ("+proj*100/correctNumber+")\t\t\tnoproj ("+noproj*100/correctNumber+")" );
	stream.println( "\t\ttree\tdag\tcycle\ttree\tdag\tcycle" );
	printOneLine( "none\t", NONE, perc );
	//printOneLine( "and\t", NONE, perc );
	printOneLine( "union\t", UNION, perc );
	printOneLine( "opt\t", OPT, perc );
	printOneLine( "filter\t", FILTER, perc );
	printOneLine( "un-opt\t", UNION_OPT, perc );
	printOneLine( "opt-filt", OPT_FILTER, perc );
	printOneLine( "filt-un\t", FILTER_UNION, perc );
	printOneLine( "un-opt-filt", UNION_OPT_FILTER, perc );
    }

    public void printOneLine ( String header, int line, boolean perc ) {
	if ( perc ) {
	    stream.println( header+"\t"+resultArray[PROJ][TREE][line]*100/correctNumber+"\t"+resultArray[PROJ][DAG][line]*100/correctNumber+"\t"+resultArray[PROJ][CYCLE][line]*100/correctNumber+"\t"+resultArray[NOPROJ][TREE][line]*100/correctNumber+"\t"+resultArray[NOPROJ][DAG][line]*100/correctNumber+"\t"+resultArray[NOPROJ][CYCLE][line]*100/correctNumber );
	} else {
	    stream.println( header+"\t"+resultArray[PROJ][TREE][line]+"\t"+resultArray[PROJ][DAG][line]+"\t"+resultArray[PROJ][CYCLE][line]+"\t"+resultArray[NOPROJ][TREE][line]+"\t"+resultArray[NOPROJ][DAG][line]+"\t"+resultArray[NOPROJ][CYCLE][line] );
	}
    }

    public void usage() {
	Package pkg = this.getClass().getPackage();
	new HelpFormatter().printHelp( 80, pkg+" [options] queryDir\nAnalyses the queries contained in queryDir", "\nOptions:", options, "" );
    }

}
