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

package fr.inrialpes.tyrexmo.qcwrapper.afmu;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;

import afreemu.formula.BDDManager;
import afreemu.formula.Formula;
import afreemu.formula.SatCheck;
import afreemu.formula.UniqManager;
import afreemu.parser.AFEParser;
import afreemu.parser.ASTroot;
import afreemu.parser.ParseException;
import afreemu.util.TimeHistory;
import fr.inrialpes.tyrexmo.queryanalysis.CycleAnalysis;
import fr.inrialpes.tyrexmo.queryanalysis.TransformAlgebra;
import fr.inrialpes.tyrexmo.testqc.ContainmentTestException;
import fr.inrialpes.tyrexmo.testqc.LegacyContainmentSolver;
import net.sf.javabdd.BuDDyFactory;

public class AFMUContainmentWrapper implements LegacyContainmentSolver {
    final static Logger logger = LoggerFactory.getLogger( AFMUContainmentWrapper.class );

    /* This is to turn around the ugly implementation of reentrance */
    protected static boolean firstTime = true;
    protected static AFEParser parser;

    /*kripke restriction \varphi_r */
    private String theta = "<-s>spr & <p>spr & <o>spr & !<s>true & !<-p>true & !<-o>true";
    private String eta = "!<-s>true & !<o>true & !<p>true & !<d>true & !<-d>true & !<s>spr & !<-p>spr & !<-o>spr";
    private String kappa = "[-s]("+eta+") & [p]("+eta+") & [o]("+eta+")";
    // This is necessary for reentrance of the program
    //private final String phiR = "(let x1 = nu "+theta+" & "+kappa+" & (!<d>true | <d>x1) in x1 end)";
    private static int recVarRank = 0;
    protected String getPhiR() {
	recVarRank++;
	return "(let xp"+recVarRank+" = nu "+theta+" & "+kappa+" & (!<d>true | <d>xp"+recVarRank+") in xp"+recVarRank+" end)";
    }

    private String WarmupFormula = "((let v1 = mu ((<-s>varx & <p>takescourse & <o>course10))  | <d>v1 in v1 end) & (let v0 = mu ((<-s>varx & <p>takescourse & <o>course20))  | <d>v0 in v0 end)) & !((let v2 = mu ((<-s>varx & <p>takescourse & <o>course10))  | <d>v2 in v2 end))";
    public void warmup() {
	try {
	    checkSAT( WarmupFormula );
	} catch ( Exception ex ) {
	    logger.warn( "Warm-up problem", ex );
	};
    };

    public boolean entailed( Query q1, Query q2 ) throws ContainmentTestException {
	return entailedUnderSchema( (String)null, q1, q2 );
    }

    public boolean entailedUnderSchema( Model schema, Query q1, Query q2 ) throws ContainmentTestException {
	throw new ContainmentTestException( "Cannot deal with schema" );
    };

    public boolean entailedUnderSchema( String schema, Query q1, Query q2 ) throws ContainmentTestException {
	// Encode formula
	String formulaLeft, formulaRight;
	if ( supportedTest( q1, q2 ) ) {
	    if ( useSameEncoding( q1, q2 ) ){
		formulaLeft = new EncodeLHSQuery( q1 ).getFormula();
		// JE : ??????? Fortunately, given the coding of useSameEncoding we barely go here!
		formulaRight = new EncodeLHSQuery( q1 ).getFormula();
	    } else {
		formulaLeft = new EncodeLHSQuery( q1 ).getFormula();
		formulaRight = new EncodeRHSQuery( q2 ).getFormula();
	    }
	} else {
	    throw new ContainmentTestException( "Cannot deal with such a test" );
	}
	//System.out.println(formulaLeft);
	//System.out.println(formulaRight);
	String formula = getPhiR() + " & (" + formulaLeft + ") & !("+ formulaRight + ")";
	//String formula = "(" + formulaLeft + ") & !("+ formulaRight + ")";
	if ( schema != null ) {
	    formula = new AxiomEncoder().encodeSchema( schema )+" & ("+formula+")";
	}
	//System.err.println(formula);
	// Create checker
	// Test it
	try {
	    return checkSAT( formula );
	} catch ( Exception ex ) {
	    throw new ContainmentTestException( "Error during Parsing formula", ex );
	}
    };

    public void cleanup() {};


    public static void reset() {

		try {
			Field field = Formula.class.getDeclaredField("instances");
			field.setAccessible(true);
			field.set(null, new UniqManager());
		} catch(Exception e) {
			throw new RuntimeException(e);
		}

		try {
			Field field = BDDManager.class.getDeclaredField("factory");
			field.setAccessible(true);
			field.set(null, null);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		try {
			Field field = BuDDyFactory.class.getDeclaredField("INSTANCE");
			field.setAccessible(true);
			BuDDyFactory f = (BuDDyFactory)field.get(null);
			if(f != null) {
				f.done();
			}

			//field.set(null, null);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}

    }


    /*
     * JE: Here I comment many things because I think that they are useless
     */
    public boolean checkSAT( String phi ) throws ParseException {
		TimeHistory.start();
	if ( firstTime ) {
	    parser = new AFEParser( new StringReader( phi ) );
	    firstTime = false;
	} else {
		reset();
		AFEParser.ReInit( new StringReader( phi ) );
	}

	ASTroot n = (ASTroot)AFEParser.root();
//	n.dump("" + recVarRank + ": ");

	Formula f = n.toFormula();
	//Closure c = new Closure( f );
	/*
	int leansize = 0;
	ArrayList<Scc> scc = c.getSccs();
	ArrayList<Object> lean = new ArrayList<Object>();
	for (Scc sccd: scc) {
	    if(!sccd.getLean().isEmpty()) {
		for (int i = 0; i < sccd.getLean().size(); i++)
		    lean.add(sccd.getLean().get(i));
	    	}
	    //lean.add(sccd.getLean());
	    leansize += sccd.getLean().size();
	}
	//System.out.println("L E A N");
	//System.out.println(lean.toString());
	//System.out.println("L E A N SIZE = ");
	//System.out.println(leansize);
	*/
	SatCheck satcheck = new SatCheck( f );
	boolean result = !satcheck.satisfiable();
	reset();
	return result;

    }

    protected boolean useSameEncoding( Query leftQuery, Query rightQuery ) {
	return leftQuery.equals( rightQuery );
    }

    private boolean supportedTest( Query q1, Query q2 ) {
	TransformAlgebra ta1 = new TransformAlgebra( q1 );
	TransformAlgebra ta2 = new TransformAlgebra( q2 );
	if ( containsOptional( ta1, ta2 ) || isValidQueryType( q1, q2 ) || isCyclic( ta1, ta2 ) || haveSameDistVar( q1, q2 ) )
	    return false;
	else
	    return true;
    }

    private boolean containsOptional( TransformAlgebra left, TransformAlgebra right ) {
	return ( left.containsOpt() || right.containsOpt() );
    }

    // This is the same...
    private boolean isValidQueryType( Query leftQuery, Query rightQuery ) {
	return (leftQuery.isConstructType() || rightQuery.isConstructType() ||
		leftQuery.isDescribeType() || rightQuery.isDescribeType());
    }

    private boolean isCyclic( TransformAlgebra left, TransformAlgebra right ) {
	CycleAnalysis l = new CycleAnalysis( left.getTriples() );
	CycleAnalysis r = new CycleAnalysis( right.getTriples() );
	return ( l.isCyclic() || r.isCyclic() );
    }

    // This is the same...
    private boolean haveSameDistVar( Query leftQuery, Query rightQuery ) {
	List<String> rightQueryDistVars = rightQuery.getResultVars();
	Collections.sort(rightQueryDistVars);
	List<String> leftQueryDistVars = leftQuery.getResultVars();
	Collections.sort(leftQueryDistVars);
	return !rightQueryDistVars.equals(leftQueryDistVars);
    }

}


